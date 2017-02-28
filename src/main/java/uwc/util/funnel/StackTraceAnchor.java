package uwc.util.funnel;

import java.util.HashMap;
import java.util.Stack;

/**
 * Created by steven on 1/27/16.
 */
public class StackTraceAnchor extends Throwable implements Anchor {
    private static ThreadLocal<Stack<String>> sWorkflow = new ThreadLocal<Stack<String>>();

    protected static final String unknown = "unclassified";
    private static Filter sLabelFilter = new Filter();
    private String mLabel = unknown;
    public enum Level {
        fatal, error, warn, verbose, debug, info
    }
    private Level mLevel = Level.verbose;

    public static void appendFilteEntry(String key, String value) {
        sLabelFilter.put(key, value);
    }

    public static void removeFilteEntry(String key) {
        sLabelFilter.remove(key);
    }

    public StackTraceAnchor(String value) {
        super(value);
    }

    public StackTraceAnchor(String format, Object... args) {
        this(String.format(format, args));
    }

    private void onWorkflow(String wf, boolean inout) {
        if (null == sWorkflow.get()) {
            sWorkflow.set(new Stack<String>());
        }
        final int pos = sWorkflow.get().indexOf(wf);
        for (int i = 0; i <= pos; i++) sWorkflow.get().pop();
        if (null != wf && inout) {
            sWorkflow.get().push(wf);
        } else if (null == wf && !inout) {
            sWorkflow.get().clear();
        }
    }

    public StackTraceAnchor toggle(String workflow) {
        onWorkflow(workflow, true);
        return this;
    }

    public StackTraceAnchor reset(String workflow) {
        onWorkflow(workflow, false);
        return this;
    }

    public StackTraceAnchor withLabel(String label) {
        if (null != label) mLabel = label;
        return this;
    }

    public StackTraceAnchor setLevel(Level level) {
        mLevel = level;
        return this;
    }

    public String getValue() {
        StringBuilder builder = new StringBuilder(getMessage());
        if (null != sWorkflow.get() && !sWorkflow.get().isEmpty())
            builder.append(String.format(" <-((%s:%s ", sWorkflow.get().lastElement(), sWorkflow.get().firstElement()));
        return builder.toString();
    }

    public String getLabel() {
        try {
            mLabel = (unknown.equals(mLabel))
                    ? sLabelFilter.match(getStackTrace()[0].getClassName()) : mLabel;
        } catch (Exception e) {}
        return mLabel;
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
