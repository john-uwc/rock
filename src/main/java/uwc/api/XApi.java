package uwc.api;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by steven on 02/05/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XApi {

    interface Declare {

        interface Mock {
            String pullMock(String iPath, String iMethod);
        }

        String namespace();

        Map<String, String> computeStub();

        Response postCall(Response response) throws IOException;

        Response preCall(Request request);
    }

    Class<? extends Declare> value() default Declare.class;
}
