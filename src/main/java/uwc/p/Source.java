package uwc.p;


public interface Source {

    class Holder {

        private static Source sInstance = new Source() {
            @Override
            public void erase(String key) {

            }

            @Override
            public void putString(String key, String value) {

            }

            @Override
            public String getString(String key, String def) {
                return def;
            }

            @Override
            public void putInt(String key, int value) {

            }

            @Override
            public int getInt(String key, int def) {
                return def;
            }

            @Override
            public void putLong(String key, long value) {

            }

            @Override
            public long getLong(String key, long def) {
                return def;
            }

            @Override
            public void putBoolean(String key, boolean value) {

            }

            @Override
            public boolean getBoolean(String key, boolean def) {
                return def;
            }
        };

        public static Source obtain() {
            synchronized (Source.class) {
                return sInstance;
            }
        }

        public static void inject(Source source){
            synchronized (Source.class) {
                sInstance = source;
            }
        }
    }



    void erase(String key);

    void putString(String key, String value);

    String getString(String key, String def);

    void putInt(String key, int value);

    int getInt(String key, int def);

    void putLong(String key, long value);

    long getLong(String key, long def);

    void putBoolean(String key, boolean value);

    boolean getBoolean(String key, boolean def);
}
