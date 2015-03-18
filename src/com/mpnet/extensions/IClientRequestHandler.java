package com.mpnet.extensions;

import com.mpnet.common.data.IMPObject;
import com.mpnet.entities.User;

/**
 * 
 * @ClassName: IClientRequestHandler 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 下午3:58:20 
 *
 */
public interface IClientRequestHandler {
	
	/**
	 * 
	 * @param user
	 * @param params
	 */
	public void handleClientRequest(User user, IMPObject params);
	
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