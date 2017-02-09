/*
 *
 *  Created by: steven on 2016年2月26日
 *  Copyright (c) 2016年 车享. All rights reserved.
 */
package uwc.xmvc;

import uwc.annotation.Convention;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * 服务层:
 * 1.所有UI与服务层的交互需要调用服务层接口
 * 3.服务接口均返回XResponse对象，对返回结果进行包装
 * @author steven
 */
public class ServiceMediator extends MockService {

	private HashMap<String, Method> mJumpTable = new HashMap<String, Method>();

	private void doRegist(Class<? extends ServiceMediator> mediator) {
		while (ServiceMediator.class != mediator) {
			for (final Method iterator:mediator.getDeclaredMethods()) {
				Convention convention = iterator.getAnnotation(
						Convention.class);
				if(null == convention || mJumpTable.containsKey(convention.namespace()))
					continue;
				if(!Modifier.isPrivate(iterator.getModifiers()))
					continue;
				iterator.setAccessible(true);
				mJumpTable.put(convention.namespace(), iterator);
			}
			mediator = (Class<? extends ServiceMediator>)mediator.getSuperclass();
		}
	}

	public ServiceMediator(){
		doRegist(getClass());
	}

	public void withMock(InputStream mockStream, boolean mayMock){
		try{
			refresh(mayMock? mockStream:null);mockStream.close();
		}catch (Exception e){}
	}

	private XResponse<?> mockIfNeeded(Method method, String service, HashMap<String, ?> params) throws Exception{
		Convention annotation = method.getAnnotation(Convention.class);
		if(!(Void.class.equals(annotation.iret()) || null == findMock(service))) {
			return null;
		}
		return invoke(annotation, method, params);
	}

	private XResponse<?> invoke(Convention convention, Method method, HashMap<String,?> params)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object[] objects = new Object[0];
		if (null != convention.args()) {
			objects = new Object[convention.args().length];
			for (int i = 0; i < convention.args().length; i++) {
				objects[i] = params.get(convention.args()[i]);
			}
		}
		return (XResponse<?>)method.invoke(this, objects);
	}

	public XResponse<?> callService(String service, HashMap<String,?> params) throws Exception{
		Method method = mJumpTable.get(service);
		if (null == method) {
			throw new IllegalArgumentException("no such service port");
		}
		return mockIfNeeded(method, service, params);
	}

	public long querySvcTerm(String service){
		return Math.max(mJumpTable.get(service).getAnnotation(Convention.class).term(), 0L);
	}
}
