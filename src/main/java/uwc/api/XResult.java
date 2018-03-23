package uwc.api;

import java.io.Serializable;

/**
 * Created by steven on 01/06/2017.
 */

public interface XResult<T> extends Serializable {

    T result();

    String message();

    int code();

    boolean verify();
}
