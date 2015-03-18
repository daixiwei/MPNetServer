package com.mpnet.extensions;

import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPLoginException;

/**   
 * @author daixiwei daixiwei15@126.com 
 * @date 2015年3月6日 上午11:53:19 
 * @version V2.9   
 */
public interface ILoginHandler {
	
	/**
	 * 
	 * @param session
	 * @param userName
	 * @param password
	 * @param paramsIn
	 * @param paramsOut
	 * @param allowGuestUsers
	 * @return
	 * @throws MPLoginException
	 */
	public User validateLogin(ISession session, String userName, String password,IMPObject paramsIn,IMPObject paramsOut,boolean allowGuestUsers) throws MPLoginException;
	
}
