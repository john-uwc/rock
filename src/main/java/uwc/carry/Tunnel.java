package uwc.carry;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author steven
 */
public final class Tunnel extends Membrane implements Runnable {
    /**
     * @author steven
     */
    public interface Coordinator {
        void refresh(Collection<SoftReference<Membrane>> membranes);

        void schedule(Object obj, Collection<SoftReference<Membrane>> membranes);
    }

    private static Tunnel sTunnel = null;

    public static Tunnel obtain() {
        synchronized (Tunnel.class) {
            if (null == sTunnel) sTunnel = new Tunnel(null);
            return sTunnel;
        }
    }

    private BlockingQueue<Object> mObjQueue = new LinkedBlockingDeque<>();
    private Collection<SoftReference<Membrane>> mMembranes = new LinkedList<>();
    private Coordinator mCoordinator = new Distributor();

    /**
     * start a thread to generate a loop
     */
    private Tunnel(Coordinator coordinator) {
        super(null); if (null != coordinator) this.mCoordinator = coordinator; new Thread(this, "carry").start();
    }

    /**
     * @param membrane
     * @param reverse implant's flag
     */
    synchronized Tunnel implant(Membrane membrane, boolean reverse) {
        if ((reverse && mMembranes.remove(membrane))
                || (!reverse && mMembranes.add(new SoftReference<>(membrane)))) {
            mCoordinator.refresh(mMembranes); return reverse ? null : this;
        }
        return reverse ? this : null;
    }

    @Override
    protected void permeate(Object obj) {
        try {
            if (null != obj) mObjQueue.put(obj);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public void run() {
        do {
            try {
                synchronized (this) { mCoordinator.schedule(mObjQueue.take(), mMembranes); }
            } catch (InterruptedException e) {
            }
        } while (true);
    }
}
