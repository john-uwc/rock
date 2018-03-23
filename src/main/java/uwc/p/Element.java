package uwc.p;

import java.io.Serializable;

/**
 * Created by steven on 04/05/2017.
 */
public class Element<T extends Serializable> implements Serializable {

    public Element<T> getValue() {
        return this;
    }

    public Serializable getKey() {
        return this.toString();
    }
}
