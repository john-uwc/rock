package uwc.util;


import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Created by steven on 18/01/2018.
 */

public final class Ring<T> {

    public final static class Reader<T> extends Slot<T> {
        private final static LinkedList<WeakReference<Reader>> sReaders = new LinkedList<>();
        protected Ring<T> mHooked = null;

        public T next() throws InterruptedException {
            T element = null;
            while (null != mHooked) {
                Map.Entry<T, Long> entry = mHooked.travel(mTs);
                if (null != entry) {
                    mTs = entry.getValue();
                    element = entry.getKey();
                    break;
                }
                synchronized (mHooked) {
                    mHooked.wait();
                }
            }
            return element;
        }

        public void attach(Ring<T> ring) {
            synchronized (sReaders) {
                if (sReaders.contains(new WeakReference<Reader>(this)))
                    return;
                sReaders.addLast(new WeakReference<Reader>(
                        this.duplicate(
                                !sReaders.isEmpty() ? sReaders.getLast().get() : null)));
                mHooked = ring;
            }
        }
    }

    private class Element<T> extends Slot<T> {
        private T mPass;

        boolean isObsolete(long ts) {
            return ts >= mTs;
        }

        Element(T element) {
            mPass = element;
        }
    }

    private short mCapacity = 260;
    private LinkedList<Element<T>> mElements = new LinkedList<>();

    public Ring(short capacity) {
        if (0 >= capacity)
            return;
        mCapacity = capacity;
    }

    public synchronized Map.Entry<T, Long> travel(long ts) {
        for (Element<T> elem : mElements) {
            if (elem.isObsolete(ts))
                continue;
            return new AbstractMap.SimpleEntry<T, Long>(elem.mPass, elem.mTs);
        }
        return null;
    }

    public synchronized void hang(T... elements) {
        if (null == elements)
            return;

        for (T element : elements) {
            mElements.addLast(new Element<T>(element));
            if (mCapacity < mElements.size())
                mElements.removeFirst();
        }
        notifyAll();
    }
}
