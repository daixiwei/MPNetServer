package com.mpnet.core;

/**
 * 
 * @ClassName: IMPEventListener 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:21:25 
 *
 */
public interface IMPEventListener {
	
	/**
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void handleServerEvent(IMPEvent event) throws Exception;
}