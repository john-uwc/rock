package uwc.util.funnel;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * <h1>due to program working track, the anchor funnel support multi anchors</h1>
 * <h2>example:</h2>
 * <p>
 * <pre>AnchorFunnel.setup(sifter).eat(anchor);</pre>
 * </p>
 *
 * @author steven
 * @version 2.0
 */
public class AnchorFunnel {

    private static AnchorFunnel sFunnelHolder = null;

    private Thread mLooper = new Thread(new Runnable() {
        @Override
        public void run() {
            do {
                Sifter sifter = null;
                synchronized (sFunnelHolder) {
                    sifter = mSifter;
                }
                if (null == sifter)
                    continue;
                sifter.drop(mEnclosure.peek());
            } while (!mMayShut);
        }
    });
    private boolean mMayShut = true;

    private PriorityBlockingQueue<Anchor> mEnclosure = new PriorityBlockingQueue<>();

    private Sifter mSifter = new Sifter() {
        @Override
        public void drop(Anchor anchor) {
            System.out.printf(anchor.toString());
        }
    };

    public static AnchorFunnel setup(Sifter sifter) {
        synchronized (AnchorFunnel.class) {
            if (null == sFunnelHolder)
                sFunnelHolder = new AnchorFunnel();
        }
        do {
            if (null == sifter)
                break;
            synchronized (sFunnelHolder) {
                sFunnelHolder.mSifter = sifter;
            }
        } while (false);
        return sFunnelHolder;
    }

    public static AnchorFunnel setup() {
        return setup(null);
    }

    private AnchorFunnel() {
        mMayShut = false;mLooper.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();mMayShut = true;
    }

    public AnchorFunnel eat(Anchor anchor) {
        mEnclosure.put(anchor);
        return this;
    }
}