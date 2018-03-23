package uwc.api;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by steven on 16/6/1.
 */
public abstract class XAcceptor<T extends XResult> implements Callback<T> {

    public static final int R_OK = 200;
    public static final int R_Network_Error = 500;
    public static final int R_Unexpected_Error = 501;
    public static final int R_Unauthenticated_Error = 502;
    public static final int R_Client_Error = 503;
    public static final int R_Server_Error = 504;


    @Override
    public void onResponse(Call<T> call, Response<T> response) {

        String message = "网络或服务器错误";
        //String message = response.message();
        int code = response.code();
        if (code == 200) {
            code = R_OK;
        } else if (code == 401) {
            code = R_Unauthenticated_Error;
        } else if (code >= 400 && code < 500) {
            code = R_Client_Error;
        } else if (code >= 500 && code < 600) {
            code = R_Server_Error;
        } else {
            code = R_Unexpected_Error;
        }

        if (R_OK != code) {
            onFail(code, message);
            return;
        }

        if (!response.body().verify()) {
            onFail(response.body().code(), response.body().message());
            return;
        }
        onSuccess(response.body());
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        onFail((t instanceof IOException) ? R_Network_Error : R_Unexpected_Error, t.getMessage());
    }


    protected abstract void onSuccess(T result);

    protected abstract void onFail(int errorCode, String errorInfo);

}
