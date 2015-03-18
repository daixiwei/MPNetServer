package com.mpnet.bitswarm.service;

/**
 * 
 * @ClassName: ISimpleService
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:36:59
 *
 */
public interface ISimpleService {
	
	/**
	 * init service
	 * 
	 * @param o
	 */
	public void init(Object o);
	
	/**
	 * destroy service
	 * 
	 * @param o
	 */
	public void destroy(Object o);
}