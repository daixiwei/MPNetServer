package com.mpnet.bitswarm.sessions;

import com.mpnet.bitswarm.core.BitSwarmEngine;
import com.mpnet.bitswarm.events.BitSwarmEvents;
import com.mpnet.bitswarm.events.Event;
import com.mpnet.bitswarm.service.IService;
import com.mpnet.config.DefaultConstants;
import com.mpnet.exceptions.BitSwarmEngineRuntimeException;
import com.mpnet.exceptions.SessionReconnectionException;
import com.mpnet.util.ITaskHandler;
import com.mpnet.util.Logging;
import com.mpnet.util.Scheduler;
import com.mpnet.util.Task;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: DefaultSessionManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:18:36
 *
 */
public final class DefaultSessionManager implements ISessionManager {
	private static final String								SESSION_CLEANING_TASK_ID			= "SessionCleanerTask";
	public static final int									SESSION_CLEANING_INTERVAL_SECONDS	= 10;
	private static ISessionManager							__instance__;
	private Logger											logger;
	private final ConcurrentMap<String, List<ISession>>		sessionsByNode;
	private final ConcurrentMap<Integer, ISession>			sessionsById;
	private BitSwarmEngine									engine								= null;
	
	private final List<ISession>							localSessions;
	private final ConcurrentMap<Integer, ISession>			localSessionsById;
	private final ConcurrentMap<SocketChannel, ISession>	localSessionsByConnection;
	private String											serviceName							= "DefaultSessionManager";
	private Task											sessionCleanTask;
	private Scheduler										systemScheduler;
	IReconnectionManager									reconnectionManager;
	private int												highestCCS							= 0;
	private IPacketQueuePolicy								packetQueuePolicy;
	
	public static ISessionManager getInstance() {
		if (__instance__ == null) {
			__instance__ = new DefaultSessionManager();
		}
		return __instance__;
	}
	
	private DefaultSessionManager() {
		
		sessionsByNode = new ConcurrentHashMap<String, List<ISession>>();
		sessionsById = new ConcurrentHashMap<Integer, ISession>();
		
		localSessions = new ArrayList<ISession>();
		localSessionsById = new ConcurrentHashMap<Integer, ISession>();
		localSessionsByConnection = new ConcurrentHashMap<SocketChannel, ISession>();
		reconnectionManager = new DefaultReconnectionManager(this);
	}
	
	public void init(Object o) {
		this.engine = BitSwarmEngine.getInstance();
		
		this.logger = LoggerFactory.getLogger(DefaultSessionManager.class);
		
		this.systemScheduler = ((Scheduler) engine.getServiceByName(DefaultConstants.SERVICE_SCHEDULER));
		this.sessionCleanTask = new Task(SESSION_CLEANING_TASK_ID);
		this.systemScheduler.addScheduledTask(this.sessionCleanTask, 10, true, new SessionCleaner());
		
		((IService) this.reconnectionManager).init(this.systemScheduler);
		try {
			this.packetQueuePolicy = new DefaultPacketQueuePolicy();
		} catch (Exception e) {
			logger.warn("SessionManager could not load a valid PacketQueuePolicy. Reason: " + e);
			Logging.logStackTrace(logger, e);
		}
	}
	
	public void destroy(Object o) {
		this.sessionCleanTask.setActive(false);
		
		((IService) this.reconnectionManager).destroy(null);
		
		shutDownLocalSessions();
		
		this.localSessionsById.clear();
		this.localSessionsByConnection.clear();
	}
	
	public void publishLocalNode(String nodeId) {
		if (this.sessionsByNode.get(nodeId) != null) {
			throw new IllegalStateException("NodeID already exists in the cluster: " + nodeId);
		}
		this.sessionsByNode.put(nodeId, this.localSessions);
	}
	
	public void addSession(ISession session) {
		synchronized (this.localSessions) {
			this.localSessions.add(session);
		}
		
		this.localSessionsById.put(Integer.valueOf(session.getId()), session);
		
		if (session.getType() == SessionType.DEFAULT) {
			this.localSessionsByConnection.put(session.getConnection(), session);
		}
		
		// if (this.config.isClustered()) {
		// this.sessionsById.put(Integer.valueOf(session.getId()), session);
		// }
		if (this.localSessions.size() > this.highestCCS) {
			this.highestCCS = this.localSessions.size();
		}
		this.logger.info("Session created: " + session + " on Server linkPort: " + session.getServerPort() + " <---> " + session.getClientPort());
	}
	
	public boolean containsSession(ISession session) {
		return this.localSessionsById.containsValue(session);
	}
	
	public void removeSession(ISession session) {
		if (session == null) {
			return;
		}
		
		synchronized (this.localSessions) {
			this.localSessions.remove(session);
		}
		
		SocketChannel connection = session.getConnection();
		int id = session.getId();
		
		this.localSessionsById.remove(Integer.valueOf(id));
		
		if (connection != null) {
			this.localSessionsByConnection.remove(connection);
		}
		
		if ((session.getType() == SessionType.DEFAULT) || (session.getType() == SessionType.WEBSOCKET)) {
			this.engine.getSocketAcceptor().getConnectionFilter().removeAddress(session.getAddress());
		}
		
		// if (this.config.isClustered()) {
		// this.sessionsById.remove(Integer.valueOf(id));
		// }
		
		logger.info("Session removed: " + session);
	}
	
	public ISession removeSession(int id) {
		ISession session = (ISession) this.localSessionsById.get(Integer.valueOf(id));
		
		if (session != null) {
			removeSession(session);
		}
		return session;
	}
	
	public ISession removeSession(String hash) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	public ISession removeSession(SocketChannel connection) {
		ISession session = getLocalSessionByConnection(connection);
		
		if (session != null) {
			removeSession(session);
		}
		return session;
	}
	
	public void onSocketDisconnected(SocketChannel connection) throws IOException {
		ISession session = (ISession) this.localSessionsByConnection.get(connection);
		
		if (session == null) {
			return;
		}
		
		this.localSessionsByConnection.remove(connection);
		session.setConnected(false);
		
		onSocketDisconnected(session);
	}
	
	public void onSocketDisconnected(ISession session) throws IOException {
		if (session.getReconnectionSeconds() > 0) {
			this.reconnectionManager.onSessionLost(session);
			dispatchSessionReconnectionTryEvent(session);
		} else {
			removeSession(session);
			dispatchLostSessionEvent(session);
		}
	}
	
	public ISession reconnectSession(ISession tempSession, String sessionToken) throws SessionReconnectionException, IOException {
		ISession resumedSession = null;
		try {
			resumedSession = this.reconnectionManager.reconnectSession(tempSession, sessionToken);
		} catch (SessionReconnectionException sre) {
			throw sre;
		}
		
		this.localSessionsByConnection.put(tempSession.getConnection(), resumedSession);
		
		tempSession.setConnection(null);
		
		this.logger.info("Session was resurrected: " + resumedSession + ", using temp Session: " + tempSession + ", " + resumedSession.getReconnectionSeconds());
		
		return resumedSession;
	}
	
	public List<ISession> getAllLocalSessions() {
		List<ISession> allSessions = null;
		
		synchronized (this.localSessions) {
			allSessions = new ArrayList<ISession>(localSessions);
		}
		
		return allSessions;
	}
	
	public List<ISession> getAllSessions() {
		List<ISession> sessions = getAllLocalSessions();
		
		// sessions = this.config.isClustered() ? new LinkedList(this.sessionsById.values()) : getAllLocalSessions();
		
		return sessions;
	}
	
	public List<ISession> getAllSessionsAtNode(String nodeName) {
		List<ISession> allSessions = null;
		
		List<ISession> theSessions = sessionsByNode.get(nodeName);
		if (theSessions != null) {
			allSessions = new ArrayList<ISession>(theSessions);
		}
		return allSessions;
	}
	
	public ISession getLocalSessionByHash(String hash) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	public ISession getLocalSessionById(int id) {
		return (ISession) this.localSessionsById.get(Integer.valueOf(id));
	}
	
	public ISession getSessionByHash(String hash) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}
	
	public ISession getLocalSessionByConnection(SocketChannel connection) {
		return (ISession) this.localSessionsByConnection.get(connection);
	}
	
	public ISession getSessionById(int id) {
		return (ISession) this.sessionsById.get(Integer.valueOf(id));
	}
	
	public int getHighestCCS() {
		return this.highestCCS;
	}
	
	public void shutDownLocalSessions() {
		synchronized (this.localSessions) {
			for (Iterator<ISession> it = this.localSessions.iterator(); it.hasNext();) {
				ISession session = (ISession) it.next();
				it.remove();
				try {
					session.close();
				} catch (IOException e) {
					logger.warn("I/O Error while closing session: " + session);
				}
			}
		}
	}
	
	public void onNodeLost(String nodeId) {
		List<ISession> nodeSessions = sessionsByNode.remove(nodeId);
		
		if (nodeSessions == null) {
			throw new IllegalStateException("Unable to remove node sessions from cluster. Lost Node ID: " + nodeId);
		}
		
		synchronized (this.sessionsById) {
			for (ISession session : nodeSessions) {
				this.sessionsById.remove(Integer.valueOf(session.getId()));
			}
		}
	}
	
	public String getName() {
		return this.serviceName;
	}
	
	public void setName(String name) {
		this.serviceName = name;
	}
	
	public void handleMessage(Object message) {
		throw new UnsupportedOperationException("Not implemented in this class!");
	}
	
	public ISession createSession(SocketChannel connection) {
		ISession session = new Session();
		session.setSessionManager(this);
		session.setConnection(connection);
		
		session.setMaxIdleTime(engine.getConfiguration().sessionMaxIdleTime);
		session.setType(SessionType.DEFAULT);
		session.setReconnectionSeconds(engine.getConfiguration().userSettings.userReconnectionSeconds);
		
		IPacketQueue packetQueue = new NonBlockingPacketQueue(engine.getConfiguration().sessionPacketQueueSize);
		packetQueue.setPacketQueuePolicy(packetQueuePolicy);
		session.setPacketQueue(packetQueue);
		
		return session;
	}
	
	public ISession createConnectionlessSession() {
		ISession session = new Session();
		session.setSessionManager(this);
		session.setType(SessionType.VOID);
		session.setConnected(true);
		
		return session;
	}
	
	public int getLocalSessionCount() {
		return this.localSessions.size();
	}
	
	public int getNodeSessionCount(String nodeId) {
		List<ISession> nodeSessionList = sessionsByNode.get(nodeId);
		
		if (nodeSessionList == null) {
			throw new BitSwarmEngineRuntimeException("Can't find session count for requested node in the cluster. Node not found: " + nodeId);
		}
		return nodeSessionList.size();
	}
	
	private void applySessionCleaning() {
		if (getLocalSessionCount() > 0) {
			for (ISession session : getAllLocalSessions()) {
				if ((session == null) || (session.isFrozen())) {
					continue;
				}
				if (session.isMarkedForEviction()) {
					terminateSession(session);
					logger.info("Terminated idle logged-in session: " + session);
				} else {
					if (!session.isIdle()) {
						continue;
					}
					if (session.isLoggedIn()) {
						if (logger.isDebugEnabled()) {
							logger.debug("Firing Client Disconnection " + session);
						}
						
						session.setMarkedForEviction();
						
						dispatchSessionIdleEvent(session);
					} else {
						terminateSession(session);
						logger.debug("Removed idle session: " + session);
					}
				}
			}
		}
		
		Event event = new Event(BitSwarmEvents.SESSION_IDLE_CHECK_COMPLETE);
		engine.dispatchEvent(event);
	}
	
	public void terminateSession(ISession session) {
		if (session.getType() == SessionType.DEFAULT) {
			SocketChannel connection = session.getConnection();
			
			session.setReconnectionSeconds(0);
			try {
				if (connection.socket() != null) {
					connection.socket().shutdownInput();
					connection.socket().shutdownOutput();
					connection.close();
				}
				
				session.setConnected(false);
			} catch (IOException err) {
				this.logger.warn("Failed closing connection while removing idle Session: " + session);
			}
			
		} else if (session.getType() == SessionType.WEBSOCKET) {
			// ChannelHandlerContext ctx = (ChannelHandlerContext) session.getSystemProperty(Session.WS_CHANNEL);
			// ctx.getChannel().close();
		}
		
		removeSession(session);
		
		dispatchLostSessionEvent(session);
	}
	
	private void dispatchLostSessionEvent(ISession closedSession) {
		Event event = new Event(BitSwarmEvents.SESSION_LOST);
		event.setParameter("session", closedSession);
		engine.dispatchEvent(event);
	}
	
	private void dispatchSessionIdleEvent(ISession idleSession) {
		Event event = new Event(BitSwarmEvents.SESSION_IDLE);
		event.setParameter("session", idleSession);
		engine.dispatchEvent(event);
	}
	
	private void dispatchSessionReconnectionTryEvent(ISession session) {
		Event event = new Event(BitSwarmEvents.SESSION_RECONNECTION_TRY);
		event.setParameter("session", session);
		engine.dispatchEvent(event);
	}
	
	/**
	 * 
	 * @ClassName: SessionCleaner
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年2月10日 下午2:41:57
	 *
	 */
	private final class SessionCleaner implements ITaskHandler {
		private SessionCleaner() {}
		
		public void doTask(Task task) throws Exception {
			applySessionCleaning();
		}
	}
}