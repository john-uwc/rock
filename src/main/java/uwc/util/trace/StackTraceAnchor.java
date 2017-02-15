package uwc.util.trace;

import uwc.util.Logger;

import java.util.HashMap;

/**
 * Created by steven on 1/27/16.
 */
public class StackTraceAnchor extends Throwable implements Logger.Anchor {
    protected StackTraceElement mSte = getStackTrace()[0];

    private static Filter sTagFilter = new Filter();

    public static void appendFilteEntry(String key, String value) {
        sTagFilter.put(key, value);
    }

    public static void removeFilteEntry(String key) {
        sTagFilter.remove(key);
    }

    public StackTraceAnchor(String body) {
        super(body);
    }

    public StackTraceAnchor(String format, Object... args) {
        this(String.format(format, args));
    }

    public String getBody() {
        return String.format("<%s@%s>[%s:%s] %s"
                , mSte.getMethodName(), mSte.getClassName(), mSte.getFileName(), mSte.getLineNumber(), getMessage());
    }

    public String getTag() {
        try {
            return sTagFilter.match(mSte.getClassName());
        } catch (NullPointerException e) {
            return Logger.Anchor.unknown;
        }
    }


    /**
     * @author steven
     * @version 1.0
     *          <h1>due to manage map between package's name and tag/h1>
     */
    private static class Filter {

        static class Entry {
            enum LinkType {
                reference,
                real
            }

            public LinkType link;
            public String value;

            public Entry(LinkType link, String value) {
                this.link = link;
                this.value = value;
            }
        }

        private final HashMap<String, Entry> mFilterMap = new HashMap<String, Entry>();

        private void put(String key, Entry.LinkType link, String value) {
            mFilterMap.put(key, new Entry(link, value));
        }

        public void put(String key, String value) {
            put(key, Entry.LinkType.real, value);
        }

        public void remove(String key) {
            mFilterMap.remove(key);
        }

        public String match(String key) throws NullPointerException {
            String hitKey = key;
            while (!"".equals(hitKey)) {
                final Entry entry = mFilterMap.get(hitKey);
                if (null != entry && Entry.LinkType.real == entry.link) {
                    if (!key.equals(hitKey)) {
                        put(key, Entry.LinkType.reference, hitKey); //new reference entry
                    }
                    break;
                }
                if (null != entry && Entry.LinkType.reference == entry.link) {
                    hitKey = entry.value;
                    continue;
                }
                if (null == entry) {
                    hitKey = hitKey.substring(0,
                            -1 == hitKey.lastIndexOf('.') ? 0 : hitKey.lastIndexOf('.'));
                }
            }
            return mFilterMap.get(hitKey).value;
        }
    }
}
