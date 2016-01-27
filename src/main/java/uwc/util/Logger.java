package uwc.util;

import java.util.HashMap;
import java.util.Stack;

/**
 * @author steven
 * @version 1.1
 * <h1>due to log, the logger support a smart log mechanism that is based on workflow</h1>
 * <br>
 * <h2>example:</h2>
 * Logger.appendFilteEntry(packagename, tag);
 * Logger.toggle(workflow).eat(level, content);
 * Logger.reset(workflow);
 * Logger.removeFilteEntry(packagename);
 */
public class Logger {

    public enum Level{
        fatal, error, warn, verbose, debug, info
    }

    public interface ServiceMediator {
        public void invoke(Level level, String tag, String body);
    }

    public interface Anchor {
        public void packTo(Level level, Stack<String> workflows, ServiceMediator mediator);
    }

    public static void setServiceMediator(ServiceMediator mediator){
        if(null == mediator)
            return;
        sServiceMediator = mediator;
    }

    public static String matchFilter(String key){
        return Filter.match(key);
    }

    public static void appendFilteEntry(String key, String value){
        Filter.put(key, value);
    }

    public static void removeFilteEntry(String key){
        Filter.remove(key);
    }

    public static final String unclassified = "unclassified";

    private static Logger sInstance = null;

    private Logger(){
    }

    private static ServiceMediator sServiceMediator = new ServiceMediator() {
        @Override
        public void invoke(Level level, String tag, String body) {
            System.out.printf("%s:%s", tag, body);
        }
    };

    private ThreadLocal<Stack<String>> mWorkflow = new ThreadLocal<Stack<String>>();

    private static Logger toggle(String workflow, boolean inorout){
        synchronized (Logger.class) {
            if (null == sInstance)
                sInstance = new Logger();
            sInstance.onWorkflow(workflow, inorout);
        }
        return sInstance;
    }

    public static Logger toggle(){
        return toggle(null, true);
    }

    public static Logger reset(){
        return toggle(null, false);
    }

    public static Logger reset(String workflow){
        return toggle(workflow, false);
    }

    public static Logger toggle(String workflow){
        return toggle(workflow, true);
    }


    public Logger eat(Level level, Anchor anchor){
        if(null != anchor)
            anchor.packTo(level, mWorkflow.get(), sServiceMediator);
        return Logger.this;
    }

    private void onWorkflow(String workflow, boolean inorout){
        if (null == mWorkflow.get()) {
            mWorkflow.set(new Stack<String>());
        }
        if (null == workflow){
            workflow = unclassified;
        }
        if (unclassified.equals(workflow) && !inorout) {
            mWorkflow.get().clear();
        }else if (!unclassified.equals(workflow) && !inorout) {
            mWorkflow.get().remove(workflow);
        }else if (!unclassified.equals(workflow) && inorout) {
            mWorkflow.get().push(workflow);
        }
    }

    /**
     * @author steven
     * @version 1.0
     * <h1>due to manage map between package's name and tag/h1>
     */
    private static class Filter{

        private static class Entry {
            public enum LinkType{
                reference,
                real
            }
            public LinkType link;
            public String value;
            public Entry(LinkType link, String value){
                this.link = link;
                this.value = value;
            }
        }

        private final static HashMap<String, Entry> sFilterMap = new HashMap<String, Entry>();


        private static void put(String key, Entry.LinkType link, String value){
            sFilterMap.put(key, new Entry(link, value));
        }

        public static void put(String key, String value){
            put(key, Entry.LinkType.real, value);
        }

        public static void remove(String key){
            sFilterMap.remove(key);
        }

        public static String match(String key){
            String hitKey = key;
            while (!"".equals(hitKey)){
                final Entry entry = sFilterMap.get(hitKey);
                if (null != entry && Entry.LinkType.real == entry.link) {
                    if(!key.equals(hitKey)){
                        put(key, Entry.LinkType.reference, hitKey); //new reference entry
                    }
                    break;
                }
                if (null != entry && Entry.LinkType.reference == entry.link) {
                    hitKey = entry.value;
                    continue;
                }
                if (null == entry){
                    hitKey = hitKey.substring(0,
                            -1 == hitKey.lastIndexOf('.') ? 0:hitKey.lastIndexOf('.'));
                }
            }
            return (null == sFilterMap.get(hitKey))? unclassified : sFilterMap.get(hitKey).value;
        }
    }



    public static class SimpleAnchor implements Anchor{
        private String mBody = SimpleAnchor.class.getName();
        public SimpleAnchor(String body){
            this.mBody = body;
        }

        public SimpleAnchor(String format, Object... args){
            this(String.format(format, args));
        }

        public void packTo(Level level, Stack<String> workflows, ServiceMediator mediator){
            mediator.invoke(level, SimpleAnchor.class.getSimpleName(), mBody);
            if (workflows.empty()){
                return;
            }
            mediator.invoke(level, workflows.firstElement(), workflows.lastElement() + " " + mBody);
        }
    }
}