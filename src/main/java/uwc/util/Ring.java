package uwc.util;


import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;


/**
 * Created by steven on 18/01/2018.
 */
public final class Ring<E> {

    public static class Reader<E> extends Slot<E> {
        private final static LinkedList<WeakReference<Reader>> sReaders = new LinkedList<>();
        protected Ring<E> mHooked = null;

        public E next() throws InterruptedException {
            E element = null;
            while (null != mHooked) {
                Map.Entry<Long, E> entry = mHooked.travel(mTs);
                if (null != entry) {
                    element = entry.getValue(); mTs = entry.getKey();
                    break;
                }
                synchronized (mHooked) {
                    mHooked.wait();
                }
            }
            return element;
        }

        public void attach(Ring<E> ring) {
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

    private class Element<E> extends Slot<E> {
        private E mPass;

        boolean isObsolete(long ts) {
            return ts >= mTs;
        }

        Element(E element) {
            mPass = element;
        }
    }


    private LinkedList<Element<E>> mElements;
    private int mCapacity;

    public Ring(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity <= 0");
        }
        mCapacity = capacity;
        mElements = new LinkedList<>();
    }

    public synchronized Map.Entry<Long, E> travel(long ts) {
        for (Element<E> elem : mElements) {
            if (elem.isObsolete(ts))
                continue;
            return new AbstractMap.SimpleEntry<Long, E>(elem.mTs, elem.mPass);
        }
        return null;
    }

    public synchronized void hang(E... elements) {
        if (null == elements)
            return;

        for (E element : elements) {
            mElements.addLast(new Element<E>(element));
            if (mCapacity < mElements.size())
                mElements.removeFirst();
        }
        notifyAll();
    }
}
