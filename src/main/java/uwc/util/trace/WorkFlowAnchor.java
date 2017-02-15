package uwc.util.trace;

import java.util.Stack;

/**
 * Created by steven on 15/02/2017.
 */

public class WorkFlowAnchor extends StackTraceAnchor {

    private static ThreadLocal<Stack<String>> sWorkflow = new ThreadLocal<Stack<String>>();

    public WorkFlowAnchor(){
        super("");
    }

    @Override
    public String getBody() {
        return sWorkflow.get().peek();
    }

    @Override
    public String getTag() {
        return super.getTag();
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

    public WorkFlowAnchor toggle(String workflow) {
        onWorkflow(workflow, true);
        return this;
    }

    public WorkFlowAnchor reset(String workflow) {
        onWorkflow(workflow, false);
        return this;
    }
}
