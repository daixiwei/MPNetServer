package com.mpnet.bitswarm.core;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @ClassName: AbstractMethodDispatcher
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:06:00
 *
 */
public abstract class AbstractMethodDispatcher implements IMethodDispatcher {
	protected Map<String, String>	methodDictionary	= new ConcurrentHashMap<String, String>();
	
	public void callMethod(String key, Object... params) throws Exception {
		String methodName = (String) methodDictionary.get(key);
		
		if (methodName == null) {
			throw new IllegalArgumentException("No method was found for key: " + key);
		}
		Class<?>[] arguments = new Class<?>[params.length];
		Class<? extends AbstractMethodDispatcher> clazz = getClass();
		
		for (int j = 0; j < params.length; j++) {
			arguments[j] = params[j].getClass();
		}
		
		Method method = clazz.getMethod(methodName, arguments);
		
		method.invoke(this, params);
	}
	
	public void registerMethod(String key, String methodName) {
		methodDictionary.put(key, methodName);
	}
	
	public void unregisterKey(String key) {
		methodDictionary.remove(key);
	}
}