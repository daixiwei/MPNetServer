package com.mpnet.bitswarm.sessions;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

/**
 * 
 * @ClassName: ISession 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午1:23:27 
 *
 */
public interface ISession {
	public int getId();

	public void setId(int id);

	public String getHashId();

	public void setHashId(String hashId);

	public SessionType getType();

	public void setType(SessionType type);

	public boolean isLoggedIn();

	public void setLoggedIn(boolean paramBoolean);

	public IPacketQueue getPacketQueue();

	public void setPacketQueue(IPacketQueue packetQueue);

	public SocketChannel getConnection();

	public void setConnection(SocketChannel channel);

	public DatagramChannel getDatagramChannel();

	public void setDatagrmChannel(DatagramChannel channel);

	public long getCreationTime();

	public void setCreationTime(long timestamp);

	public boolean isConnected();

	public void setConnected(boolean value);

	public long getLastActivityTime();

	public void setLastActivityTime(long timestamp);

	public long getLastLoggedInActivityTime();

	public void setLastLoggedInActivityTime(long timestamp);

	public long getLastReadTime();

	public void setLastReadTime(long timestamp);

	public long getLastWriteTime();

	public void setLastWriteTime(long timestamp);

	public long getReadBytes();

	public void addReadBytes(long readBytes);

	public long getWrittenBytes();

	public void addWrittenBytes(long writtenBytes);

	public int getDroppedMessages();

	public void addDroppedMessages(int value);

	public int getMaxIdleTime();

	public void setMaxIdleTime(int idleTime);

	public int getMaxLoggedInIdleTime();

	public void setMaxLoggedInIdleTime(int idleTime);

	public boolean isMarkedForEviction();

	public void setMarkedForEviction();

	public boolean isIdle();

	public boolean isFrozen();

	public void freeze();

	public void unfreeze();

	public long getFreezeTime();

	public boolean isReconnectionTimeExpired();

	public Object getSystemProperty(String key);

	public void setSystemProperty(String key, Object value);

	public void removeSystemProperty(String key);

	public Object getProperty(String key);

	public void setProperty(String key, Object value);

	public void removeProperty(String key);

	public String getFullIpAddress();

	public String getAddress();

	public int getClientPort();

	public String getServerAddress();

	public int getServerPort();

	public String getFullServerIpAddress();

	public ISessionManager getSessionManager();

	public void setSessionManager(ISessionManager sessionManager);

	public void close() throws IOException;

	public int getReconnectionSeconds();

	public void setReconnectionSeconds(int value);
}