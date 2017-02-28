package uwc.xmvc;

import uwc.util.funnel.AnchorFunnel;
import uwc.util.funnel.StackTraceAnchor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Stack;

/**
 * XController xmvc控制器定义.
 * 维护xmodel与xview的绑定关系,并提供服务请求,模型填充与数据视口刷新机制
 * @author steven
 *
 * @see XModel
 * @see XView
 */
public final class XController {

    static {
        StackTraceAnchor.appendFilteEntry("uwc.xmvc", "xmvc");
    }

    private static XController sInstance = null;
    public static XController instance(){
        if(null == sInstance)
            sInstance = new XController();
        return sInstance;
    }
    private XController(){
    }

    private static final HashMap<XView<? extends XModel>, XModel<? extends XModel>> sModelPool = new HashMap<>();

    private <T extends XModel> Class<T> smartType(XView<T> unit){
        Class<?> smartType, defType = null;
        Stack<Class<?>> clsPool = new Stack<>();
        Type varType = null;
        Class<?> cls = unit.getClass();
        while (null != cls){
            clsPool.push(cls);
            for (Type iType:cls.getGenericInterfaces()){
                if (!((iType instanceof ParameterizedType)
                        && XView.class.equals(((ParameterizedType) iType).getRawType()))){
                    continue;
                }
                varType = ((ParameterizedType)iType).getActualTypeArguments()[0];
            }
            cls = (null != varType)? null : cls.getSuperclass();
        }

        while (!(varType instanceof Class) && !clsPool.isEmpty()){
            TypeVariable[] typeParams = clsPool.pop().getTypeParameters();
            int index = typeParams.length;
            while (0 < index--){
                if (varType.toString().equals(typeParams[index].getName())){
                    Type[] bounds = typeParams[index].getBounds();
                    if (0 != bounds.length
                            && XModel.class.isAssignableFrom((Class<?>)bounds[0])){
                        defType = (Class<?>)bounds[0];
                    }
                    break;
                }
            }

            if(clsPool.isEmpty()){
                break;
            }
            Type sType = clsPool.peek().getGenericSuperclass();
            if(!(sType instanceof ParameterizedType)){
                continue;
            }
            varType = ((ParameterizedType) sType).getActualTypeArguments()[index];
        }

        smartType = (varType instanceof Class)? (Class<?>)varType : defType;

        return (Class<T>)smartType;
    }
    /**
     * 获取窗口单元对应的model实例
     * @param unit
     * @return
     */
    public final <T extends XModel> XModel<T> fetch(XView<T> unit){
        XModel<T> model = (XModel<T>)sModelPool.get(unit);
        if (null == model) {
            try{
                sModelPool.put(unit, model = smartType(unit).newInstance());
            } catch (Exception e){
                new StackTraceAnchor("xmodel for " + unit + " fail: " + e.getMessage())
                        .setLevel(StackTraceAnchor.Level.error)
                        .stick();
            }
        }
        return model;
    }

    /**
     * 释放窗口单元对应的model
     * @param unit
     */
    public final void abandon(XView<? extends XModel> unit){
        sModelPool.remove(unit);
    }

    /**
     * 执行服务请求, 请求由参数model和参数service共同标示
     * @param model
     * @param parameters
     * @param service
     */
    public final void request(XModel<? extends XModel> model, HashMap<String, ?> parameters, String service){
        Transaction.doRequest(model, parameters, service);
    }

    /**
     * 取消服务请求, 当参数service为空时,取消基于参数model的所有服务请求
     * @param model
     * @param service
     */
    public final void cancelRequest(XModel<? extends XModel> model, String service){
        Transaction.requestCancel(model, service, null);
    }
}
