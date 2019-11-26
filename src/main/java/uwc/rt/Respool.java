package uwc.rt;

import java.util.HashMap;

/**
 * Created by steven on 14/07/2017.
 * <p>
 * stash for runtime
 * 1. 基本类型读写
 * 2. 对象类型读写
 */

public class Respool {

    private static HashMap<Class, Object> sObjs = new HashMap<>();

    private static boolean sIsEngMode = true;

    public static <T> T query(Class<T> key) {
        return (T) sObjs.get(key);
    }

    public static <T> void put(Class<T> key, T value) {
        sObjs.put(key, value);
    }

    public static boolean isEngMode() {
        return sIsEngMode;
    }
}
