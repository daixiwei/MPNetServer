package com.mpnet.bitswarm.controllers;

import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.bitswarm.service.IService;
import com.mpnet.exceptions.RequestQueueFullException;

/**
 * 
 * @ClassName: IController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:22:45
 *
 */
public interface IController extends IService {
	
	/**
	 * 
	 * @return
	 */
	public Object getId();
	
	/**
	 * 
	 * @param id
	 */
	public void setId(Object id);
	
	/**
	 * 
	 * @param request
	 * @throws RequestQueueFullException
	 */
	public void enqueueRequest(IRequest request) throws RequestQueueFullException;
	
	/**
	 * 
	 * @return
	 */
	public int getQueueSize();
	
	/**
	 * 
	 * @return
	 */
	public int getThreadPoolSize();
	
	/**
	 * 
	 * @param size
	 */
	public void setThreadPoolSize(int size);
}