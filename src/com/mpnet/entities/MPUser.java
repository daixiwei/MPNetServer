package com.mpnet.entities;

import java.util.concurrent.atomic.AtomicInteger;

import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.util.IDisconnectionReason;


/**
 * 
 * @ClassName: MPUser 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 下午5:24:19 
 *
 */
public class MPUser implements User {
	private static AtomicInteger autoID = new AtomicInteger(0);
	protected int id;
	protected ISession session;
	protected String name;
	protected short privilegeId = 0;
	protected int playerGuid = -1;

	protected volatile long lastLoginTime = 0L;
	protected volatile boolean connected = false;

	private static int getNewID() {
		return autoID.getAndIncrement();
	}

	public MPUser(ISession session) {
		this("", session);
	}

	public MPUser(String name, ISession session) {
		this.id = getNewID();
		this.name = name;
		this.session = session;
		updateLastRequestTime();
	}

	public int getId() {
		return id;
	}

	public short getPrivilegeId() {
		return privilegeId;
	}

	public void setPrivilegeId(short id) {
		privilegeId = id;
	}

	public boolean isConnected() {
		return connected;
	}

	public synchronized void setConnected(boolean flag) {
		connected = flag;
	}

	public String getIpAddress() {
		return session.getAddress();
	}

	public void disconnect(IDisconnectionReason reason) {
		MPNetServer.getInstance().getAPIManager().getApi().disconnectUser(this, reason);
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPlayerGuid() {
		return playerGuid;
	}

	public void setPlayerGuid(int playerId) {
		this.playerGuid = playerId;
	}

	public int getPlayerId() {
		return 0;
	}

	public ISession getSession() {
		return session;
	}

	public String toString() {
		return String.format("( User Name: %s, Id: %s, Priv: %s, Sess: %s ) ", name, id, privilegeId, session.getFullIpAddress());
	}

	public long getLastRequestTime() {
		return session.getLastLoggedInActivityTime();
	}

	public synchronized void updateLastRequestTime() {
		setLastRequestTime(System.currentTimeMillis());
	}

	public void setLastRequestTime(long lastRequestTime) {
		session.setLastLoggedInActivityTime(lastRequestTime);
	}

	public int getReconnectionSeconds() {
		return session.getReconnectionSeconds();
	}

	public void setReconnectionSeconds(int seconds) {
		session.setReconnectionSeconds(seconds);
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof User)) {
			return false;
		}
		User user = (User) obj;
		boolean isEqual = false;

		if (user.getId() == id) {
			isEqual = true;
		}
		return isEqual;
	}
}