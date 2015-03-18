package com.mpnet.bitswarm.events;

/**
 * 
 * @ClassName: IEventListener
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:23:20
 *
 */
public interface IEventListener {
	
	/**
	 * 
	 * @param paramIEvent
	 */
	public void handleEvent(IEvent event);
}