package com.mpnet.extensions.filter;

import com.mpnet.common.data.IMPObject;
import com.mpnet.core.IMPEvent;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPException;
import com.mpnet.extensions.MPExtension;

/**
 * 
 * @ClassName: IFilter 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:31:19 
 *
 */
public interface IFilter {
	/**
	 * 
	 * @param extension
	 */
	public void init(MPExtension extension);

	/**
	 * 
	 */
	public void destroy();

	/**
	 * 
	 * @param requestId
	 * @param user
	 * @param params
	 * @return
	 * @throws MPException
	 */
	public FilterAction handleClientRequest(String requestId, User user, IMPObject params) throws MPException;

	/**
	 * 
	 * @param event
	 * @return
	 * @throws MPException
	 */
	public FilterAction handleServerEvent(IMPEvent event) throws MPException;
}