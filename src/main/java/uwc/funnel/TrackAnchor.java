package uwc.funnel;

/**
 * Created by steven on 15/02/2017.
 */

public class TrackAnchor implements Anchor {

    private String mLabel = TrackAnchor.class.getName();
    private String mValue = "";

    public TrackAnchor(String value) {
        this.mValue = value;
    }

    public TrackAnchor(String format, Object... args) {
        this(String.format(format, args));
    }

    public String getValue() {
        return mValue;
    }

    public String getLabel() {
        return mLabel;
    }
}
