package com.mpnet.bitswarm.events;

/**
 * 
 * @ClassName: IEvent
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:23:08
 *
 */
public interface IEvent {
	
	/**
	 * 
	 * @return
	 */
	public Object getTarget();
	
	/**
	 * 
	 * @param target
	 */
	public void setTarget(Object target);
	
	/**
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * @param param
	 */
	public void setName(String param);
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public Object getParameter(String key);
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void setParameter(String key, Object value);
}