package com.mpnet.extensions;

import com.mpnet.core.IMPEvent;
import com.mpnet.exceptions.MPException;

/**
 * 
 * @ClassName: IServerEventHandler 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:46:07 
 *
 */
public interface IServerEventHandler {
	/**
	 * 
	 * @param event
	 * @throws MPException
	 */
	public void handleServerEvent(IMPEvent event) throws MPException;

	/**
	 * 
	 * @param extension
	 */
	public void setParentExtension(MPExtension extension);

	/**
	 * 
	 * @return
	 */
	public MPExtension getParentExtension();
}