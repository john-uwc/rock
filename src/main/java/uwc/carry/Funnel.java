package uwc.carry;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author steven
 */
public class Funnel<T extends Funnel> extends Membrane {

    /**
     * @author steven
     */
    public interface Sifter {
        Object onSieve(Object obj);
    }

    private Tunnel mTunnel = null;
    private List<Sifter> mSifters = new LinkedList<>();

    public Funnel(Brief brief) {
        super(brief);
    }


    public T putSifter(Sifter... sifters) {
        mSifters.addAll(Arrays.asList(sifters));
        return (T) this;
    }

    /**
     * enroll this funnel to tunnel
     * @param tunnel
     */
    public final T attach(Tunnel tunnel) {
        mTunnel = tunnel.implant(this, false);
        return (T) this;
    }


    public final void drop(Object obj) {
        (null != mTunnel ? mTunnel : this).absorb(obj);
    }

    @Override
    protected void permeate(Object obj) {
        Object b = obj;
        Iterator<Sifter> it = mSifters.iterator();
        while (it.hasNext() && null != (b = it.next().onSieve(b))) ;
        drop(obj == b ? null : b);
    }


    public final void detach() {
        if (null == mTunnel)
            return;
        mTunnel = mTunnel.implant(this, true);
    }
}
