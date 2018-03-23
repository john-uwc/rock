package uwc.p;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by steven on 04/05/2017.
 */
public class Cache<V extends Element> extends DSPiple<V> {
    private Map<Serializable, V> mPool = new LinkedHashMap<>();
    private long mTimeToIdleSeconds = 0;
    private long mTimeToLiveSeconds = 0;
    private boolean mEternal = false;
    private boolean mOverflowToDisk = false;
    private int mMaxElementsInMemory = 0;

    private boolean mIsInit = false;

    public Cache init(int maxElementsInMemory, boolean overflowToDisk,
                      boolean eternal, long timeToLiveSeconds, long timeToIdleSeconds) {
        if (mIsInit) return this;

        this.mIsInit = true;

        this.mMaxElementsInMemory = maxElementsInMemory;
        this.mOverflowToDisk = overflowToDisk;
        this.mEternal = eternal;
        this.mTimeToLiveSeconds = timeToLiveSeconds;
        this.mTimeToIdleSeconds = timeToIdleSeconds;
        doSync(false);
        return this;
    }

    private Cache(Class<V> type) {
        super(type);
    }

    public static <T extends Element> Cache from(Class<T> type) {
        return new Cache(type);
    }

    public String getName() {
        return type.getCanonicalName();
    }

    public Cache<V> remove(Serializable key) {
        mPool.remove(key);
        return doSync(true);
    }

    public Cache<V> put(V element) {
        mPool.put(element.getKey(), element);
        return doSync(true);
    }

    public V get(Serializable key) {
        return mPool.get(key);
    }


    private Cache<V> doSync(boolean reverse) {
        if (!mOverflowToDisk)
            return this;

        if (reverse) {
            serialize(mPool.values(), getName());
            return this;
        }
        Collection<V> collections = deserialize(getName(), new ArrayList<V>());
        mPool.clear();
        for (V element : collections)
            mPool.put(element.getKey(), element);
        return this;
    }
}
