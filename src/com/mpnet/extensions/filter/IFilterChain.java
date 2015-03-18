package com.mpnet.extensions.filter;

import com.mpnet.common.data.IMPObject;
import com.mpnet.core.IMPEvent;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPException;

/**
 * 
 * @ClassName: IFilterChain 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:31:28 
 *
 */
public interface IFilterChain {
	
	/**
	 * 
	 * @param filterName
	 * @param filter
	 */
	public void addFilter(String filterName, MPExtensionFilter filter);

	/**
	 * 
	 * @param filterName
	 */
	public void remove(String filterName);

	/**
	 * 
	 * @param requestId
	 * @param user
	 * @param params
	 * @return
	 */
	public FilterAction runRequestInChain(String requestId, User user, IMPObject params);

	/**
	 * 
	 * @param event
	 * @return
	 * @throws MPException
	 */
	public FilterAction runEventInChain(IMPEvent event) throws MPException;

	/**
	 * 
	 * @return
	 */
	public abstract int size();

	
	/**
	 * 
	 */
	public abstract void destroy();
}