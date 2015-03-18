package com.mpnet.core;

/**
 * 
 * @ClassName: IMPEventDispatcher 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:21:20 
 *
 */
public interface IMPEventDispatcher {
	
	/**
	 * 
	 * @param eventType
	 * @param eventListener
	 */
	public void addEventListener(MPEventType eventType, IMPEventListener eventListener);

	/**
	 * 
	 * @param eventType
	 * @return
	 */
	public boolean hasEventListener(MPEventType eventType);

	/**
	 * 
	 * @param eventType
	 * @param eventListener
	 */
	public void removeEventListener(MPEventType eventType, IMPEventListener eventListener);

	/**
	 * 
	 * @param event
	 */
	public void dispatchEvent(IMPEvent event);
}