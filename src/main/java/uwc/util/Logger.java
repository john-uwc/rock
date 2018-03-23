package uwc.util;

/**
 * Created by steven on 20/12/2017.
 */
public interface Logger {

    class Holder {

        /**
         * A {@link Logger} defaults output appropriate for the current platform.
         */
        private static Logger sInstance = new Logger() {
            @Override
            public void log(String tag, String message) {
                System.out.print(tag + ":" + message);
            }
        };

        public static Logger obtain() {
            synchronized (Logger.class) {
                return sInstance;
            }
        }

        public static void inject(Logger logger){
            synchronized (Logger.class) {
                sInstance = logger;
            }
        }
    }

    void log(String tag, String message);
}
