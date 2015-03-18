package com.mpnet.api;

import java.util.List;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.entities.User;
import com.mpnet.util.IDisconnectionReason;

/**
 * 
 * @ClassName: IMPApi 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月18日 上午11:47:43 
 *
 */
public interface IMPApi {
	
	/**
	 * 
	 * @param sender
	 * @param name
	 * @param pass
	 * @param paramsIn
	 * @param paramsOut
	 * @param forceLogout
	 * @return
	 */
	public User login(ISession sender, String name, String pass, IMPObject paramsIn, IMPObject paramsOut, boolean forceLogout);
	
	/**
	 * 
	 * @param user
	 */
	public void logout(User user);
	
	/**
	 * 
	 * @param user
	 */
	public void disconnectUser(User user);
	
	/**
	 * 
	 * @param user
	 * @param reason
	 */
	public void disconnectUser(User user, IDisconnectionReason reason);
	
	/**
	 * 
	 * @param session
	 */
	public void disconnect(ISession session);
	
	/**
	 * 
	 * @param session
	 * @return
	 */
	public User getUserBySession(ISession session);
	
	/**
	 * 
	 * @param cmdName
	 * @param params
	 * @param recipients
	 * @param useUDP
	 */
	public void sendExtensionResponse(String cmdName, IMPObject params, List<User> recipients, boolean useUDP);
	
	/**
	 * 
	 * @param cmdName
	 * @param params
	 * @param recipient
	 * @param useUDP
	 */
	public void sendExtensionResponse(String cmdName, IMPObject params, User recipient, boolean useUDP);
	
	/**
	 * 
	 * @param recipient
	 */
	public void notifyReconnectionFailure(ISession recipient);
	
}