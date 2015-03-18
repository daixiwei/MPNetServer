package com.mpnet.bitswarm.sessions;

import com.mpnet.bitswarm.core.BitSwarmEngine;
import com.mpnet.bitswarm.events.BitSwarmEvents;
import com.mpnet.bitswarm.events.Event;
import com.mpnet.bitswarm.service.IService;
import com.mpnet.exceptions.SessionReconnectionException;
import com.mpnet.util.ITaskHandler;
import com.mpnet.util.Scheduler;
import com.mpnet.util.Task;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: DefaultReconnectionManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:18:19
 *
 */
public final class DefaultReconnectionManager implements IService, IReconnectionManager {
	private static final String			SERVICE_NAME					= "DefaultReconnectionManager";
	private static final String			RECONNETION_CLEANING_TASK_ID	= "SessionReconnectionCleanerTask";
	private final ISessionManager		sessionManager;
	private final Map<String, ISession>	frozenSessionsByHash;
	private final Logger				logger;
	private Task						sessionReconnectionCleanTask;
	private Scheduler					systemScheduler;
	private BitSwarmEngine				engine;
	
	public DefaultReconnectionManager(ISessionManager sessionManager) {
		this.sessionManager = sessionManager;
		this.logger = LoggerFactory.getLogger(getClass());
		
		this.frozenSessionsByHash = new ConcurrentHashMap<String, ISession>();
	}
	
	public void init(Object o) {
		this.engine = BitSwarmEngine.getInstance();
		
		this.systemScheduler = ((Scheduler) o);
		this.sessionReconnectionCleanTask = new Task(RECONNETION_CLEANING_TASK_ID);
		this.systemScheduler.addScheduledTask(this.sessionReconnectionCleanTask, 3, true, new ReconnectionSessionCleaner());
	}
	
	public void destroy(Object o) {
		this.sessionReconnectionCleanTask.setActive(false);
		
		this.frozenSessionsByHash.clear();
	}
	
	public String getName() {
		return SERVICE_NAME;
	}
	
	public void handleMessage(Object message) {
		throw new UnsupportedOperationException("Not supported in this class");
	}
	
	public void setName(String name) {
		throw new UnsupportedOperationException("Not supported in this class");
	}
	
	public ISessionManager getSessionManager() {
		return this.sessionManager;
	}
	
	public void onSessionLost(ISession session) {
		addSession(session);
		
		session.freeze();
	}
	
	public ISession getReconnectableSession(String token) {
		return (ISession) this.frozenSessionsByHash.get(token);
	}
	
	public ISession reconnectSession(ISession tempSession, String prevSessionToken) throws SessionReconnectionException {
		SocketChannel connection = tempSession.getConnection();
		ISession session = getReconnectableSession(prevSessionToken);
		
		if (session == null) {
			dispatchSessionReconnectionFailureEvent(tempSession);
			throw new SessionReconnectionException("Session Reconnection failure. The passed Session is not managed by the ReconnectionManager: " + connection);
		}
		
		if (!connection.isConnected()) {
			throw new SessionReconnectionException("Session Reconnection failure. The new socket is not connected: " + session.toString());
		}
		
		if (session.isReconnectionTimeExpired()) {
			throw new SessionReconnectionException("Session Reconnection failure. Time expired for Session: " + session.toString());
		}
		
		session.setConnection(connection);
		
		removeSession(session);
		
		session.unfreeze();
		
		if (!session.getPacketQueue().isEmpty()) {
			this.engine.getSocketWriter().continueWriteOp(session);
		}
		
		dispatchSessionReconnectionSuccessEvent(session);
		
		this.logger.debug("Reconnection done. Sessions remaining: " + this.frozenSessionsByHash);
		
		return session;
	}
	
	private void addSession(ISession session) {
		if (this.frozenSessionsByHash.containsKey(session.getHashId())) {
			throw new IllegalStateException("Unexpected: Session is already managed by ReconnectionManager. " + session.toString());
		}
		if (session.getReconnectionSeconds() <= 0) {
			throw new IllegalStateException("Unexpected: Session cannot be frozen. " + session.toString());
		}
		
		this.frozenSessionsByHash.put(session.getHashId(), session);
		this.logger.debug("Session added in ReconnectionManager: " + session + ", ReconnTime: " + session.getReconnectionSeconds() + "s");
	}
	
	private void removeSession(ISession session) {
		this.frozenSessionsByHash.remove(session.getHashId());
		this.logger.debug("Session removed from ReconnectionManager: " + session);
	}
	
	private void dispatchSessionReconnectionSuccessEvent(ISession session) {
		Event event = new Event(BitSwarmEvents.SESSION_RECONNECTION_SUCCESS);
		event.setParameter("session", session);
		this.engine.dispatchEvent(event);
	}
	
	private void dispatchSessionReconnectionFailureEvent(ISession incomingSession) {
		Event event = new Event(BitSwarmEvents.SESSION_RECONNECTION_FAILURE);
		event.setParameter("session", incomingSession);
		this.engine.dispatchEvent(event);
	}
	
	private void applySessionCleaning() {
		if (this.frozenSessionsByHash.size() > 0) {
			for (Iterator<ISession> iter = this.frozenSessionsByHash.values().iterator(); iter.hasNext();) {
				ISession session = (ISession) iter.next();
				
				if (!session.isReconnectionTimeExpired())
					continue;
				iter.remove();
				this.logger.debug("Removing expired reconnectable Session: " + session);
				
				session.setReconnectionSeconds(0);
				try {
					this.sessionManager.onSocketDisconnected(session);
				} catch (IOException e) {
					this.logger.warn("I/O Error while closing session: " + session);
				}
			}
		}
	}
	
	private final class ReconnectionSessionCleaner implements ITaskHandler {
		private ReconnectionSessionCleaner() {}
		
		public void doTask(Task task) throws Exception {
			DefaultReconnectionManager.this.applySessionCleaning();
		}
	}
}