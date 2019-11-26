package uwc.p;

/**
 * Created by steven on 31/01/2018.
 */

public interface VolumeValve {

    class Holder {

        /**
         * A {@link VolumeValve} defaults output appropriate for the current platform.
         */
        private static VolumeValve sInstance = new VolumeValve() {

        };

        public static VolumeValve obtain() {
            synchronized (VolumeValve.class) {
                return sInstance;
            }
        }

        public static void inject(VolumeValve valve) {
            synchronized (VolumeValve.class) {
                sInstance = valve;
            }
        }
    }
}
