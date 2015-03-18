package com.mpnet.bitswarm.service;

/**
 * 
 * @ClassName: IService
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午5:06:46
 *
 */
public interface IService {
	
	/**
	 * 
	 * @param o
	 */
	public void init(Object o);
	
	/**
	 * 
	 * @param o
	 */
	public void destroy(Object o);
	
	/**
	 * 
	 * @param o
	 */
	public void handleMessage(Object o);
	
	/**
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * 
	 * @param name
	 */
	public void setName(String name);
}