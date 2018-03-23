package uwc.p;

import java.util.HashMap;

/**
 * Created by steven on 14/07/2017.
 *
 * mini redis system
 * 1. 基本类型读写
 * 2. 对象类型读写
 */

public class Redis {
    private static boolean sIsEngMode = true;
    private static HashMap<Class, Object> sObjs = new HashMap<>();

    public static void with(boolean isEngMode) {
        sIsEngMode = isEngMode;
    }

    public static boolean isEngMode() {
        return sIsEngMode;
    }

    public static <T> T query(Class<T> key) {
        return (T) sObjs.get(key);
    }

    public static <T> void put(Class<T> key, T value) {
        sObjs.put(key, value);
    }

    public static void erase(String key) {
        Source.Holder.obtain().erase(key);
    }

    public static void put(String key, String value){
        Source.Holder.obtain().putString(key, value);
    }

    public static void put(String key, long value){
        Source.Holder.obtain().putLong(key, value);
    }

    public static String query(String key) {
        return Source.Holder.obtain().getString(key);
    }

    public static long query(String key, long def) {
        return Source.Holder.obtain().getLong(key, def);
    }
}
