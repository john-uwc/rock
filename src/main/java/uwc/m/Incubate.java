package uwc.m;

import java.io.Serializable;

/**
 * Created by steven on 31/01/2018.
 */

public interface Incubate<bT extends Serializable> {
    int B_OK = 0;
    int B_Network_Error = 500;
    int B_Unexpected_Error = 501;
    int B_Unauthenticated_Error = 502;
    int B_Client_Error = 503;
    int B_Server_Error = 504;

    void onBorn(bT serializable, int code, String message);

    void born(bT serializable, int code, String message);
}
