package com.mpnet.bitswarm.sessions;

import com.mpnet.exceptions.SessionReconnectionException;

/**
 * 
 * @ClassName: IReconnectionManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午1:23:13 
 *
 */
public interface IReconnectionManager {
	public ISession getReconnectableSession(String token);

	public ISession reconnectSession(ISession session, String token) throws SessionReconnectionException;

	public void onSessionLost(ISession session);

	public ISessionManager getSessionManager();
}