package com.mpnet.entities;

import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.util.IDisconnectionReason;

/**
 * 
 * @ClassName: User 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:05:54 
 *
 */
public interface User {
	public int getId();

	public ISession getSession();

	public String getIpAddress();

	public String getName();

	public void setName(String name);
	
	public long getLastLoginTime();

	public void setLastLoginTime(long time);
	
	public int getPlayerGuid();

	public void setPlayerGuid(int playerId);

	public int getPlayerId();

	public boolean isConnected();

	public void setConnected(boolean flag);

	public short getPrivilegeId();

	public void setPrivilegeId(short id);

	public long getLastRequestTime();

	public void setLastRequestTime(long time);

	public void updateLastRequestTime();

	public void disconnect(IDisconnectionReason reason);

	public int getReconnectionSeconds();

	public void setReconnectionSeconds(int seconds);
}