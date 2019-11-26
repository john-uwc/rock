package uwc.api.trace;

import uwc.util.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;

/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * {@linkplain OkHttpClient#interceptors() application interceptor} or as a {@linkplain
 * OkHttpClient#networkInterceptors() network interceptor}. <p> The format of the logs created by
 * this class should not be considered stable and may change slightly between releases. If you need
 * a stable logging format, use your own interceptor.
 */
public final class ApiTrack implements Interceptor {
    private final String TAG = getClass().getSimpleName();

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private Level mLogLevel = Level.NONE;

    private Logger mLogger = null;

    public ApiTrack() {
        this(Logger.Holder.obtain());
    }

    public ApiTrack(Logger logger) {
        this.mLogger = logger;
    }

    /**
     * Change the level at which this interceptor logs.
     */
    public ApiTrack setLevel(Level level) {
        if (level == null)
            throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.mLogLevel = level;
        return this;
    }

    private String toTag(Chain chain){
        return TAG + chain.request().url().uri().getPath();
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyEncoded(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !contentEncoding.equalsIgnoreCase("identity");
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        if (Level.NONE == mLogLevel) {
            return chain.proceed(request);
        }

        boolean logBody = mLogLevel == Level.BODY;
        boolean logHeaders = logBody || mLogLevel == Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        Connection connection = chain.connection();
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        mLogger.v(toTag(chain), requestStartMessage);

        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    mLogger.v(toTag(chain), "Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    mLogger.v(toTag(chain), "Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    mLogger.v(toTag(chain), name + ": " + headers.value(i));
                }
            }

            if (!logBody || !hasRequestBody) {
                mLogger.v(toTag(chain), "--> END " + request.method());
            } else if (bodyEncoded(request.headers())) {
                mLogger.v(toTag(chain), "--> END " + request.method() + " (encoded body omitted)");
            } else {
                Buffer buffer = new Buffer();
                requestBody.writeTo(buffer);

                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                mLogger.v(toTag(chain), "");
                if (isPlaintext(buffer)) {
                    mLogger.v(toTag(chain), buffer.readString(charset));
                    mLogger.v(toTag(chain), "--> END " + request.method()
                            + " (" + requestBody.contentLength() + "-byte body)");
                } else {
                    mLogger.v(toTag(chain), "--> END " + request.method() + " (binary "
                            + requestBody.contentLength() + "-byte body omitted)");
                }
            }
        }

        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            mLogger.v(toTag(chain), "<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        mLogger.v(toTag(chain), "<-- " + response.code() + ' ' + response.message() + ' '
                + response.request().url() + " (" + tookMs + "ms" + (!logHeaders ? ", "
                + bodySize + " body" : "") + ')');

        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                mLogger.v(toTag(chain), headers.name(i) + ": " + headers.value(i));
            }

            if (!logBody || !HttpHeaders.hasBody(response)) {
                mLogger.v(toTag(chain), "<-- END HTTP");
            } else if (bodyEncoded(response.headers())) {
                mLogger.v(toTag(chain), "<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE); // Buffer the entire body.
                Buffer buffer = source.buffer();

                Charset charset = UTF8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }

                if (!isPlaintext(buffer)) {
                    mLogger.v(toTag(chain), "");
                    mLogger.v(toTag(chain), "<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                    return response;
                }

                if (contentLength != 0) {
                    mLogger.v(toTag(chain), "");
                    mLogger.v(toTag(chain), buffer.clone().readString(charset));
                }

                mLogger.v(toTag(chain), "<-- END HTTP (" + buffer.size() + "-byte body)");
            }
        }

        return response;
    }
}
