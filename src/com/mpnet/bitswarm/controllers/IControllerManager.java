package com.mpnet.bitswarm.controllers;

import com.mpnet.bitswarm.service.IService;

/**
 * 
 * @ClassName: IControllerManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:15:26
 *
 */
public interface IControllerManager extends IService {
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public IController getControllerById(Object id);
	
	/**
	 * 
	 * @param id
	 * @param controller
	 */
	public void addController(Object id, IController controller);
	
	/**
	 * 
	 * @param id
	 */
	public void removeController(Object id);
}