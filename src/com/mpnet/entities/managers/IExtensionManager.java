package com.mpnet.entities.managers;

import com.mpnet.bitswarm.service.ISimpleService;
import com.mpnet.core.IMPEvent;
import com.mpnet.core.IMPEventListener;
import com.mpnet.core.MPEventType;
import com.mpnet.extensions.IMPExtension;

/**
 * 
 * @ClassName: IExtensionManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:21:53 
 *
 */
public interface IExtensionManager {
	/**
	 * 
	 * @return
	 */
	public IMPExtension getExtension();

	/**
	 * 
	 */
	public void init();

	/**
	 * 
	 */
	public void destroy();

	/**
	 * 
	 */
	public void reloadExtension();
	
	/**
	 * 
	 * @param key
	 * @param service
	 */
	public void addService(String key,ISimpleService service);
	
	/**
	 * 
	 * @param key
	 * @param service
	 */
	public void removeService(String key,ISimpleService service);
	
	/**
	 * 
	 * @param key
	 * @return
	 */
	public ISimpleService getService(String key);
	
	/**
	 * 
	 * @param eventType
	 * @param listener
	 */
	public void addEventListener(MPEventType eventType, IMPEventListener listener);

	/**
	 * 
	 * @param eventType
	 * @param listener
	 */
	public void removeEventListener(MPEventType eventType, IMPEventListener listener);

	/**
	 * 
	 * @param listener
	 */
	public void removeListener(IMPEventListener listener);

	/**
	 * 
	 * @param event
	 */
	public void dispatchEvent(IMPEvent event);

}