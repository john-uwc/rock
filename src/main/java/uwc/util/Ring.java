package uwc.util;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by steven on 18/01/2018.
 */

public class Ring<T> {

    protected static abstract class Slot<T> {
        protected long mTs = System.currentTimeMillis();

        protected <z extends Slot<T>> z duplicate(z slot) {
            if (null != slot) mTs = slot.mTs;
            return (z) this;
        }
    }

    public static class Reader<T> extends Slot<T> {
        private final static LinkedList<WeakReference<Reader>> sReaders = new LinkedList<>();
        protected Ring<T> mHooked = null;

        public T next() throws InterruptedException {
            T element = null;
            while (null != mHooked) {
                Map.Entry<T, Long> elem = mHooked.travel(mTs);
                if (null != elem) {element = elem.getKey();mTs = elem.getValue();break;}
                synchronized (Reader.class) {
                    Reader.class.wait();
                }
            }
            return element;
        }

        public final void attach(Ring<T> ring) {
            synchronized (sReaders) {
                mHooked = ring;
                if (sReaders.contains(new WeakReference<Reader>(this)))
                    return;
                sReaders.addLast(new WeakReference<Reader>(
                        this.duplicate(!sReaders.isEmpty() ? sReaders.getLast().get() : null)));
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

    private final static short sCapacity = 200;
    private LinkedList<Element<T>> mElements = new LinkedList<>();

    public synchronized Map.Entry<T, Long> travel(long ts) {
        for (Element<T> elem : mElements) {
            if (elem.isObsolete(ts))
                continue;
            return new AbstractMap.SimpleEntry<T, Long>(elem.mPass, elem.mTs);
        }
        return null;
    }

    public synchronized void hang(T element) {
        mElements.addLast(new Element<T>(element));
        do {
            if (sCapacity >= mElements.size())
                break;
            mElements.removeFirst();
        } while (false);

        synchronized (Reader.class) {Reader.class.notifyAll();}
    }
}
