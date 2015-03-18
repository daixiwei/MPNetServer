package com.mpnet.bitswarm.core;

/**
 * 
 * @ClassName: IMethodDispatcher
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:03:24
 *
 */
public interface IMethodDispatcher {
	
	/**
	 * 
	 * @param key
	 * @param methodName
	 */
	public void registerMethod(String key, String methodName);
	
	/**
	 * 
	 * @param key
	 */
	public void unregisterKey(String key);
	
	/**
	 * 
	 * @param key
	 * @param params
	 * @throws Exception
	 */
	public void callMethod(String key, Object... params) throws Exception;
}