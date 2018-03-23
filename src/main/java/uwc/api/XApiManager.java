package uwc.api;


import com.google.gson.GsonBuilder;
import uwc.p.Redis;
import uwc.api.trace.ApiTrack;
import uwc.api.trace.Level;
import uwc.util.Logger;
import uwc.util.LruCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class XApiManager<T> {
    private static final String TAG = XApiManager.class.getSimpleName();

    private static LruCache<Class, XApiManager> sManagers = new LruCache<>(6);

    private T mApi = null;

    public static <T> XApiManager<T> sharedManager(Class<T> api) {
        synchronized (sManagers) {
            if (null == sManagers.get(api))
                sManagers.put(api, new XApiManager(api));
            return sManagers.get(api);
        }
    }

    private XApiManager(Class<T> api) {
        mApi = createApi(api);
    }

    public T invoker() {
        return mApi;
    }

    private T createApi(Class<T> api) {
        try {
            XApi.Declare declare = api.getAnnotation(XApi.class).value()
                    .newInstance();
            Retrofit.Builder builder = new Retrofit.Builder();
            builder.baseUrl(declare.namespace()).client(okHttp(declare))
                    .addConverterFactory(convertFactory());
            mApi = builder.build().create(api);
        } catch (Exception e) {
            Logger.Holder.obtain().log(TAG,""+e);
        }
        return mApi;
    }

    private OkHttpClient okHttp(final XApi.Declare declare) {
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();

        okBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                //请求前处理
                final Response result = declare.preCall(request);
                if (null != result) return result;
                //装载http头
                Request.Builder builder = request.newBuilder();
                Map<String, String> stubs = declare.computeStub();
                stubs = (null != stubs? stubs : new HashMap<String, String>());
                for (Map.Entry<String, String> entry : stubs.entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
                request = builder.build();
                //请求后处理
                return declare.postCall(chain.proceed(request));
            }
        });

        okBuilder.addInterceptor(new ApiTrack().setLevel(Redis.isEngMode() ? Level.BODY : Level.BASIC));

        return okBuilder.build();
    }

    private Converter.Factory convertFactory() {
        return GsonConverterFactory.create(
                new GsonBuilder().setLenient().create());
    }

}
