package com.mpnet.bitswarm.events;

/**
 * 
 * @ClassName: IEventDispatcher
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:26:04
 *
 */
public interface IEventDispatcher {
	
	/**
	 * 
	 * @param paramString
	 * @param paramIEventListener
	 */
	public void addEventListener(String paramString, IEventListener listener);
	
	/**
	 * 
	 * @param paramString
	 * @return
	 */
	public boolean hasEventListener(String paramString);
	
	/**
	 * 
	 * @param paramString
	 * @param paramIEventListener
	 */
	public void removeEventListener(String paramString, IEventListener listener);
	
	/**
	 * 
	 * @param paramIEvent
	 */
	public void dispatchEvent(IEvent event);
}