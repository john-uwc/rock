package uwc.util;

import java.util.HashMap;
import java.util.Stack;

/**
 * @author steven
 * @version 1.2
 * <h1>due to log, the logger support a smart log mechanism that is based on workflow</h1>
 * <br>
 * <h2>example:</h2>
 * Logger.appendFilteEntry(packagename, tag);
 * Logger.toggle(workflow).eat(level, anchor);
 * Logger.reset(workflow);
 * Logger.removeFilteEntry(packagename);
 */
public class Logger {

    public enum Level {
        fatal, error, warn, verbose, debug, info
    }

    public interface ServiceMediator{
        public void invoke(Level level, String tag, String body);
    }

    public interface Anchor {
        public void packTo(Level level, Stack<String> workflows, ServiceMediator mediator);
    }

    public Logger eat(Level level, Anchor anchor){
        if(null != anchor)
            anchor.packTo(level, mWorkflow.get(), sServiceMediator);
        return Logger.this;
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

    private ThreadLocal<Stack<String>> mWorkflow = new ThreadLocal<Stack<String>>();

    private static ServiceMediator sServiceMediator = new ServiceMediator() {
        @Override
        public void invoke(Level level, String tag, String body) {
            System.out.printf("%s:%s", tag, body);
        }
    };

    private static Logger sInstance = null;

    private Logger(){
    }

    private static Logger setup(String workflow, boolean inout){
        synchronized (Logger.class) {
            if (null == sInstance)
                sInstance = new Logger();
            sInstance.onWorkflow(workflow, inout);
        }
        return sInstance;
    }

    private void onWorkflow(String workflow, boolean inout){
        if (null == mWorkflow.get()) {
            mWorkflow.set(new Stack<String>());
        }
        if (null != workflow && !inout) {
            mWorkflow.get().remove(workflow);
        }else if (null != workflow && inout){
            mWorkflow.get().push(workflow);
        }else if (null == workflow && !inout) {
            mWorkflow.get().clear();
        }
    }

    public static Logger toggle(String workflow){
        return setup(workflow, true);
    }

    public static Logger toggle(){
        return toggle(null);
    }

    public static Logger reset(String workflow){
        return setup(workflow, false);
    }

    public static Logger reset(){
        return reset(null);
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
            mediator.invoke(level, SimpleAnchor.class.getSimpleName(), Logger.unclassified + " " + mBody);
            if (workflows.empty()){
                return;
            }
            mediator.invoke(level, workflows.firstElement(), workflows.lastElement() + " " + mBody);
        }
    }
}