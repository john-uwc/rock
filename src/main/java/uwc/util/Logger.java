package uwc.util;

/**
 * @author steven
 * @version 1.3
 * <h1>due to log, the logger support multi anchors</h1>
 * <br>
 * <h2>example:</h2>
 * Logger.local()
 *       .setup(provider)
 *       .eat(level, anchor);
 */
public class Logger {

    public interface Anchor {
        String unknown = "unclassified";

        default void stick(Logger logger, Logger.Level level){
            logger.eat(level, this);
        }

        public String getBody();

        public String getTag();
    }

    public enum Level {
        fatal, error, warn, verbose, debug, info
    }

    public interface ServiceProvider {

        public void invoke(Level level, String tag, String body);
    }

    private static ThreadLocal<Logger> sLoggerHolder = new ThreadLocal<Logger>();

    private ServiceProvider mServiceProvider = new ServiceProvider() {
        @Override
        public void invoke(Level level, String tag, String body) {
            System.out.printf("%s/%s: %s", tag, level, body);
        }
    };

    public static Logger local() {
        synchronized (Logger.class) {
            if (null == sLoggerHolder.get())
                sLoggerHolder.set(new Logger());
        }
        return sLoggerHolder.get();
    }

    private Logger() {
    }

    public Logger setup(ServiceProvider provider) {
        if (null != provider) mServiceProvider = provider;
        return this;
    }

    public Logger eat(Level level, Anchor anchor) {
        if (null != anchor) {
            mServiceProvider.invoke(level, anchor.getTag(), anchor.getBody());
        }
        return Logger.this;
    }
}