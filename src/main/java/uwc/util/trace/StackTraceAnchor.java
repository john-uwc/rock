package uwc.util.trace;

import uwc.util.Logger;
import java.util.Stack;

/**
 * Created by steven on 1/27/16.
 */
public class StackTraceAnchor extends Throwable implements Logger.Anchor{
    protected StackTraceElement mSte = getStackTrace()[0];

    public StackTraceAnchor(String body){
        super(body);
    }

    public StackTraceAnchor(String format, Object... args){
        this(String.format(format, args));
    }

    public void packTo(Logger.Level level, Stack<String> workflows, Logger.ServiceMediator mediator){
        mediator.invoke(level, getTag(null), getBody(null));
        if (workflows.empty()){
            return;
        }
        mediator.invoke(level, getTag(workflows.firstElement()), getBody(workflows.lastElement()));
    }


    private String getBody(String workflow){
        workflow = (null == workflow)? Logger.unclassified:workflow;
        return String.format("<%s@%s>[%s:%s] %s"
                ,workflow, mSte.getMethodName(), mSte.getFileName(), mSte.getLineNumber(), getMessage());
    }

    private String getTag(String workflow){
        if (null == workflow)
            workflow = Logger.matchFilter(mSte.getClassName());
        return workflow;
    }
}
