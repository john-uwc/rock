package uwc.p;

import java.util.HashMap;

/**
 * Created by steven on 04/05/2017.
 */
public final class CacheManager extends HashMap<String, Cache> {
    public static CacheManager create() {
        return new CacheManager();
    }

    private CacheManager() {

    }

    public Cache getCache(String name) {
        return get(name);
    }

    public synchronized void addCacheIfAbsent(Cache cache) {
        if (containsKey(cache.getName()))
            return;
        put(cache.getName(), cache);
    }
}
