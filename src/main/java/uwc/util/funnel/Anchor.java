package uwc.util.funnel;

/**
 * Created by steven on 16/02/2017.
 */
public interface Anchor {
    default void stick(AnchorFunnel funnel) {
        funnel.eat(this);
    }

    default void stick(){
        stick(AnchorFunnel.setup());
    }
}
