package uwc.xmvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XModel xmvc框架中的model定义.
 * XModel将数据消费主体和具体数据分离，成为二者之间的媒介, 所有的数据处理和传递都通过xmodel完成。
 * @author steven
 *
 * @see XController
 * @see XView
 */	
public class XModel<T extends XModel> {

	private static final ServiceMediator sServiceMediator = new ServiceMediator();

	private List<XView<T>> mViewports = new ArrayList<XView<T>>();

	private HashMap<String, XResponse<?>> mNotifyStash = new HashMap<>();

	/**
	 * 返回对应的service mediator接口
	 */
	public ServiceMediator getServiceMediator(){
		return sServiceMediator;
	}

	/**
	 * 解析服务返回内容,重载该方法,用于结果解析成具体的实体对象
	 * @param response
	 * @param service
	 * @return
	 */
	protected boolean doPacks(XResponse<?> response, String service){
		return true;
	}

	/**
	 * 当服务返回结果数据的时候,填充模型对象数据
	 * @param service
	 * @param response
	 * @return
	 */
	final void padding(String service, XResponse<?> response){
		if (!doPacks(response, service))
			return;
		nfyPorts(service, response);
	}

	/**
	 * 通知已注册的viewport更新相关模型数据
	 * @param service
	 * @param response
	 * @return
	 */
	private void nfyPorts(String service, XResponse<?> response){
		Map.Entry<String, XResponse<?>>[] ntys = null;
		XView[] ports = null;
		synchronized (this) {
			if (null != service && null != response) {mNotifyStash.put(service, response);}
			if (0 == mViewports.size() || 0 == mNotifyStash.size()) {
				return;
			}
			mViewports.toArray(
					ports = new XView[mViewports.size()]);
			mNotifyStash.entrySet().toArray(ntys = new Map.Entry[mNotifyStash.size()]);mNotifyStash.clear();
		}

		for (Map.Entry<String, XResponse<?>> nty: ntys){
			for (XView<T> port: ports){
				if (XResponse.Success != nty.getValue().getCode()){
					port.handleAbnormal(nty.getKey()
							, nty.getValue().getCode(), nty.getValue().getDescription());
					continue;
				}
				port.jetData((T)this, nty.getKey());
			}
		}
	}

	/**
	 * 注册viewport,可同时多个
	 * @param ports
	 * @return
	 */
	public void registPort(XView<T>... ports){
		synchronized (this){
			for (XView<T> port : ports){
				if(null == port || mViewports.contains(port))
					continue;
				mViewports.add(port);
			}
		}
		nfyPorts(null, null);
	}

	/**
	 * 注销viewport
	 */
	public void unregist(XView<T>... ports){
		synchronized (this){
			for (XView<T> port : ports){
				mViewports.remove(port);
			}
		}
	}

	/**
	 * 注销所有已注册的viewport
	 */
	public void purgeViewports(){
		synchronized (this){
			mViewports.clear();
		}
	}
}
