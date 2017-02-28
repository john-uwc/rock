package uwc.xmvc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import uwc.util.funnel.AnchorFunnel;
import uwc.util.Taskpool;
import uwc.util.funnel.StackTraceAnchor;

/**
 * Created by steven on 2/5/16.
 */
final class Transaction {

    static {
        Taskpool.sharedInstance().doTask(new Dispatcher());
    }

    /**
     * Task调度类，主要负责监视Task的运行
     */
    static class Dispatcher implements Runnable {
        private static boolean sMaySleep = false;

        @Override
        public void run() {
            Thread.currentThread().setName("XDispatcher");

            do {
                try {
                    synchronized (Dispatcher.class) {
                        sMaySleep = true;

                        Iterator<XModel<? extends XModel>> mdlIterator = mSvcTasks.keySet().iterator();
                        while (mdlIterator.hasNext()) {
                            final XModel model = mdlIterator.next();
                            Iterator<Map.Entry<SvcTask.Token, SvcTask>> tskIterator = mSvcTasks.get(model).entrySet().iterator();
                            while (tskIterator.hasNext()) {
                                final SvcTask tsk = tskIterator.next().getValue();

                                sMaySleep = (sMaySleep && (SvcTask.Status.died == tsk.status()));

                                if (SvcTask.Status.died == tsk.status()) {
                                    Taskpool.sharedInstance().cancelTask(tsk);
                                    tskIterator.remove();
                                    if (mSvcTasks.get(model).isEmpty()) {
                                        mdlIterator.remove();
                                    }
                                    continue;
                                }
                                if (SvcTask.Status.unknown == tsk.status()) {
                                    tsk.toStatusIfNeeded(SvcTask.Status.ready, true);Taskpool.sharedInstance().doTask(tsk);continue;
                                }
                                if (SvcTask.Status.cancel == tsk.status()) {
                                    tsk.toStatusIfNeeded(SvcTask.Status.died, true);
                                } else if (SvcTask.Status.finish == tsk.status()) {
                                    tsk.toStatusIfNeeded(SvcTask.Status.died, true);
                                } else if(SvcTask.Status.running == tsk.status() || SvcTask.Status.ready == tsk.status()){
                                    tsk.toStatusIfNeeded(SvcTask.Status.died, false);
                                }
                            }
                        }

                        Dispatcher.class.wait(sMaySleep ? Long.MAX_VALUE : 2);
                    }
                } catch (Exception e) {
                    new StackTraceAnchor(e.getMessage())
                            .setLevel(StackTraceAnchor.Level.error)
                            .stick();
                }
            } while (true);
        }
    }

    /**
     * 请求Task，负责各种服务请求
     */
    static class SvcTask implements Runnable {

        private XModel<? extends XModel> mModel = null;

        static class Token {
            public XResponse<?> response = null;
            public HashMap<String, ?> params = null;
            public String service = null;
            public ServiceMediator mediator = null;

            @Override
            public boolean equals(Object o) {
                if (null != o && SvcTask.Token.class == o.getClass()) {
                    return ("" + this.service)
                            .equals(((SvcTask.Token) o).service);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return ("" + service).hashCode();
            }
        }

        enum Status {
            ready, running, cancel, finish, died, unknown
        }

        private final SvcTask.Token mToken = new SvcTask.Token();

        private SvcTask.Status mStatus = SvcTask.Status.unknown;
        private long mTimeToLive = Long.MAX_VALUE;
        private long mTimeStamp = System.currentTimeMillis();

        public static SvcTask obtain(XModel<? extends XModel> model, HashMap<String, ?> parameters, String service) {
            return new SvcTask(model, parameters, service);
        }

        private SvcTask(XModel<? extends XModel> model, HashMap<String, ?> parameters, String service){
            mToken.response = new XResponse();
            mToken.params = parameters;
            mToken.service = service;

            try{
                mTimeToLive =
                        (mToken.mediator =
                                (mModel = model).getServiceMediator()).querySvcTerm(mToken.service);
            }catch (Exception e) {
                new StackTraceAnchor("service task create failed with %s {%s:<%s}", e.getMessage(), myToken().service, myToken().params)
                        .setLevel(StackTraceAnchor.Level.error)
                        .stick();
            }
        }

        public SvcTask.Token myToken() {
            return mToken;
        }

        public void toStatusIfNeeded(SvcTask.Status status, boolean sure){
            if (mStatus == status || SvcTask.Status.died == mStatus){
                return;
            }

            if (!sure && SvcTask.Status.died == status
                    && mTimeToLive >= (System.currentTimeMillis() - mTimeStamp)){
                return;
            }

            if (sure && SvcTask.Status.finish == status && SvcTask.Status.running == mStatus){
                Transaction.response(mModel
                        , mToken.service, mToken.params
                        , mToken.response);
            }

            if (!sure && SvcTask.Status.died == status && SvcTask.Status.running == mStatus){
                mToken.response.setDescription(XResponse.TimeOut_Request_Description);
                mToken.response.setCode(XResponse.TimeOut_Request);
                Transaction.response(mModel
                        , mToken.service, mToken.params
                        , mToken.response);
            }

            if (sure && SvcTask.Status.died == status && SvcTask.Status.cancel == mStatus){
                mToken.response.setDescription(XResponse.Cancel_Request_Description);
                mToken.response.setCode(XResponse.Cancel_Request);
                Transaction.response(mModel
                        , mToken.service, mToken.params
                        , mToken.response);
            }

            this.mStatus = status;
        }

        public SvcTask.Status status(){
            return mStatus;
        }

        public void run() {
            Thread.currentThread().setName("SvcTask"+"@"+this.hashCode());

            toStatusIfNeeded(SvcTask.Status.running, true);

            try {
                if (null == myToken().mediator) {
                    throw new IllegalArgumentException("serviceMediator can not be null");
                }
                myToken().response = myToken().mediator.callService(myToken().service, myToken().params);
            } catch (Exception e) {
                myToken().response.setCode(XResponse.Error);
                myToken().response.setDescription(XResponse.Error_Description + "(" + e.getMessage() + ")");
            }

            toStatusIfNeeded(SvcTask.Status.finish, true);
        }
    }



    private static HashMap<XModel<? extends XModel>, HashMap<SvcTask.Token, SvcTask>> mSvcTasks = new HashMap<>();


    public static void response(XModel<? extends XModel> model, String service, HashMap<String, ?> params, XResponse<?> response) {
        new StackTraceAnchor("service %s [%s:<%s]", response.getDescription(), service, params)
                .setLevel(StackTraceAnchor.Level.info)
                .stick();
        model.padding(service, response);
    }

    public static void requestCancel(XModel<? extends XModel> model, String service, HashMap<String, ?> parameters) {
        HashMap<SvcTask.Token, SvcTask> tskmap = mSvcTasks.get(model);
        synchronized (Dispatcher.class) {
            do{
                if (null == tskmap) break;
                if (null == service) {
                    Iterator<SvcTask> tskIterator = tskmap.values().iterator();
                    while (tskIterator.hasNext()){
                        tskIterator.next().toStatusIfNeeded(SvcTask.Status.died, true);
                    }
                    break;
                }
                SvcTask task = tskmap.get(SvcTask.obtain(model, parameters, service).myToken());
                if (null == task) break;
                task.toStatusIfNeeded(SvcTask.Status.cancel, true);
            } while (false);
            Dispatcher.class.notify();
        }
    }

    public static void doRequest(XModel<? extends XModel> model, HashMap<String, ?> parameters, String service) {
        HashMap<SvcTask.Token, SvcTask> tskmap = mSvcTasks.get(model);
        synchronized (Dispatcher.class) {
            do{
                if (null == tskmap) mSvcTasks.put(model, tskmap = new HashMap<SvcTask.Token, SvcTask>());
                SvcTask task = SvcTask.obtain(model, parameters, service);
                if (null == service || tskmap.containsKey(task.myToken())) break;
                tskmap.put(task.myToken(), task);
            } while (false);
            Dispatcher.class.notify();
        }
    }
}
