package uwc.util;

import java.util.HashMap;
import java.util.Stack;

/**
 * @author steven
 * @version 1.0
 * <h1>due to log, the logger support a smart log mechanism that is based on workflow</h1>
 * <br>
 * <h2>example:</h2>
 * Logger.appendFilteEntry(packagename, tag);
 * Logger.toggle(workflow).i(content);
 * Logger.reset(workflow);
 * Logger.removeFilteEntry(packagename);
 */
public class Logger {

    public static class Anchor extends Throwable{
        protected StackTraceElement mSte = getStackTrace()[0];

        public Anchor(String body){
            super(body);
        }

        public Anchor(String format, Object... args){
            this(String.format(format, args));
        }

        public String getBody(String workflow){
            workflow = (null == workflow)? unclassified:workflow;
            return String.format("<%s@%s>[%s:%s] %s"
                    ,workflow, mSte.getMethodName(), mSte.getFileName(), mSte.getLineNumber(), getMessage());
        }

        public String getTag(String workflow){
            if (null == workflow)
                workflow = Filter.match(mSte.getClassName());
            return workflow;
        }
    }

    public interface ServiceMediator {
        public void f(String tag, String body);
        public void e(String tag, String body);
        public void w(String tag, String body);
        public void v(String tag, String body);
        public void d(String tag, String body);
        public void i(String tag, String body);
    }

    public static void setServiceMediator(ServiceMediator mediator){
        if(null == mediator)
            return;
        sServiceMediator = mediator;
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

    private static ServiceMediator sServiceMediator = new SysoutMediator();

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


    public Logger f(Anchor anchor){
        sServiceMediator.f(anchor.getTag(null), anchor.getBody(null));
        if (!mWorkflow.get().empty()){
            sServiceMediator.f(
                    anchor.getTag(mWorkflow.get().firstElement()), anchor.getBody(mWorkflow.get().lastElement()));
        }
        return Logger.this;
    }

    public Logger e(Anchor anchor){
        sServiceMediator.e(anchor.getTag(null), anchor.getBody(null));
        if (!mWorkflow.get().empty()){
            sServiceMediator.e(
                    anchor.getTag(mWorkflow.get().firstElement()), anchor.getBody(mWorkflow.get().lastElement()));
        }
        return Logger.this;
    }

    public Logger w(Anchor anchor){
        sServiceMediator.w(anchor.getTag(null), anchor.getBody(null));
        if (!mWorkflow.get().empty()){
            sServiceMediator.w(
                    anchor.getTag(mWorkflow.get().firstElement()), anchor.getBody(mWorkflow.get().lastElement()));
        }
        return Logger.this;
    }

    public Logger v(Anchor anchor){
        sServiceMediator.v(anchor.getTag(null), anchor.getBody(null));
        if (!mWorkflow.get().empty()){
            sServiceMediator.v(
                    anchor.getTag(mWorkflow.get().firstElement()), anchor.getBody(mWorkflow.get().lastElement()));
        }
        return Logger.this;
    }

    public Logger d(Anchor anchor){
        sServiceMediator.d(anchor.getTag(null), anchor.getBody(null));
        if (!mWorkflow.get().empty()){
            sServiceMediator.d(
                    anchor.getTag(mWorkflow.get().firstElement()), anchor.getBody(mWorkflow.get().lastElement()));
        }
        return Logger.this;
    }

    public Logger i(Anchor anchor){
        sServiceMediator.i(anchor.getTag(null), anchor.getBody(null));
        if (!mWorkflow.get().empty()){
            sServiceMediator.i(
                    anchor.getTag(mWorkflow.get().firstElement()), anchor.getBody(mWorkflow.get().lastElement()));
        }
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
}

class SysoutMediator implements Logger.ServiceMediator{
    public void f(String tag, String body){
        System.out.printf("%s:%s",tag,body);
    }
    public void e(String tag, String body){
        System.out.printf("%s:%s",tag,body);
    }
    public void w(String tag, String body){
        System.out.printf("%s:%s",tag,body);
    }
    public void v(String tag, String body){
        System.out.printf("%s:%s",tag,body);
    }
    public void d(String tag, String body){
        System.out.printf("%s:%s",tag,body);
    }
    public void i(String tag, String body){
        System.out.printf("%s:%s",tag,body);
    }
}
