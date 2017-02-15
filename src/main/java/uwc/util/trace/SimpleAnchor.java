package uwc.util.trace;

import java.util.Stack;

import uwc.util.Logger;

/**
 * Created by steven on 14/02/2017.
 */
public class SimpleAnchor implements Logger.Anchor {
    private String mBody = "";

    public SimpleAnchor(String body) {
        this.mBody = body;
    }

    public SimpleAnchor(String format, Object... args) {
        this(String.format(format, args));
    }

    public String getBody() {
        return mBody;
    }

    public String getTag() {
        return SimpleAnchor.class.getName();
    }
}
