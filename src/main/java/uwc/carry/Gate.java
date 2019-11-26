package uwc.carry;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.LinkedList;

/**
 * @author steven
 */
public final class Gate extends Funnel<Gate> implements Runnable {

    public static abstract class Channel {
        protected final String TAG = getClass().getSimpleName();

        protected abstract Object receive();

        protected abstract void send(Object object);
    }

    public static class Sentry {
        protected final String TAG = getClass().getSimpleName();

        protected Collection<WeakReference<?>> mEntrants = new LinkedList<>();

        protected synchronized boolean isRecorded(Object obj) {
            for (WeakReference ref : mEntrants) {
                if (null != ref.get() && ref.get().equals(obj)) return true;
            }
            return false;
        }

        protected synchronized boolean record(Object obj) {
            return mEntrants.add(new WeakReference<>(obj));
        }
    }

    private Sentry mSentry = null;
    private Channel mChannel = null;

    public Gate(Brief brief, Sentry sentry, Channel channel) throws IllegalArgumentException {
        super(brief);
        if (null == channel) throw new IllegalArgumentException();
        mChannel = channel;
        mSentry = null == sentry ? new Sentry() : sentry;
        new Thread(this, "carry.gate:" + hashCode()).start();
    }


    @Override
    public void run() {
        do {
            Object obj = mChannel.receive();
            if (null != obj && trap(obj) && mSentry.record(obj)) drop(obj);
        } while (true);
    }


    @Override
    protected void permeate(Object obj) {
        super.permeate(obj);
        if (mSentry.isRecorded(obj))
            return;
        mChannel.send(obj);
    }
}
