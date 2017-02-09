package uwc.util.trace;

import java.util.HashMap;
import java.util.Stack;

import uwc.util.Logger;

/**
 * Created by steven on 1/27/16.
 */
public class WatchAnchor implements Logger.Anchor{
    private static HashMap<String, Long> sTimestamp = new HashMap<String, Long>();

    public WatchAnchor(String name){
        final long curTime =  System.currentTimeMillis();
        mWname = (null == name)? Logger.unclassified:name;
        if (sTimestamp.containsKey(mWname))
            mDuration = curTime - sTimestamp.get(mWname);
        sTimestamp.put(mWname, curTime);
    }

    private String mWname = Logger.unclassified;
    private long mDuration = 0L;

    public void packTo(Logger.Level level, Stack<String> workflows, Logger.ServiceMediator mediator){
        if (workflows.empty()){
            return;
        }
        mediator.invoke(level, getTag(workflows.firstElement()), getBody(workflows.lastElement()));
    }

    public String getBody(String workflow){
        return String.format("-> %sms/ctx", mDuration);
    }

    private String getTag(String workflow){
        return mWname;
    }


    private class WatchManager{

    }
}
