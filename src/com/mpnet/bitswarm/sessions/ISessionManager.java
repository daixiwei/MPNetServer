package com.mpnet.bitswarm.sessions;

import com.mpnet.bitswarm.service.IService;
import com.mpnet.exceptions.SessionReconnectionException;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * 
 * @ClassName: ISessionManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午1:23:43 
 *
 */
public interface ISessionManager extends IService {
	public void addSession(ISession session);

	public void removeSession(ISession session);

	public ISession removeSession(int paramInt);

	public ISession removeSession(String hash);

	public ISession removeSession(SocketChannel channel);

	public boolean containsSession(ISession session);

	public void shutDownLocalSessions();

	public List<ISession> getAllSessions();

	public ISession getSessionById(int id);

	public ISession getSessionByHash(String hash);

	public int getNodeSessionCount(String paramString);

	public int getHighestCCS();

	public List<ISession> getAllLocalSessions();

	public ISession getLocalSessionById(int id);

	public ISession getLocalSessionByHash(String hash);

	public ISession getLocalSessionByConnection(SocketChannel channel);

	public int getLocalSessionCount();

	public ISession createSession(SocketChannel channel);

	public ISession createConnectionlessSession();


	public void onNodeLost(String paramString);

	public void onSocketDisconnected(SocketChannel channel) throws IOException;

	public void onSocketDisconnected(ISession session) throws IOException;

	public ISession reconnectSession(ISession session, String paramString) throws SessionReconnectionException, IOException;
}