package uwc.event;


import uwc.funnel.Anchor;
import uwc.funnel.AnchorFunnel;

/**
 * Created by steven on 02/05/2017.
 */
public abstract class AbsEvent implements Anchor {

    public void stick() {
        AnchorFunnel.setup().eat(this);
    }

    @Override
    public void stick(AnchorFunnel anchorFunnel) {
        anchorFunnel.eat(this);
    }
}
