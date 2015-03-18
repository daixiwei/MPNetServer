package com.mpnet.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.io.IResponse;
import com.mpnet.bitswarm.io.Response;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.config.DefaultConstants;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.controllers.system.Login;
import com.mpnet.core.IMPEventParam;
import com.mpnet.core.MPConstants;
import com.mpnet.core.MPEvent;
import com.mpnet.core.MPEventParam;
import com.mpnet.core.MPEventType;
import com.mpnet.entities.User;
import com.mpnet.entities.managers.IUserManager;
import com.mpnet.exceptions.MPLoginException;
import com.mpnet.exceptions.MPLoginInterruptedException;
import com.mpnet.exceptions.MPRuntimeException;
import com.mpnet.util.ClientDisconnectionReason;
import com.mpnet.util.IDisconnectionReason;

/**
 * 
 * @ClassName: MPApi
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:05:09
 *
 */
public class MPApi implements IMPApi {
	protected final MPNetServer		mpnet;
	protected final Logger			log;
	protected IUserManager			userManager;
	private final LoginErrorHandler	loginErrorHandler;
	
	public MPApi(MPNetServer mpnet) {
		this.log = LoggerFactory.getLogger(getClass());
		this.mpnet = mpnet;
		this.userManager = mpnet.getUserManager();
		this.loginErrorHandler = new LoginErrorHandler();
	}
	
	public User getUserById(int userId) {
		return this.userManager.getUserById(userId);
	}
	
	public User getUserBySession(ISession session) {
		return this.userManager.getUserBySession(session);
	}
	
	public void disconnect(ISession session) {
		if (session == null) {
			throw new MPRuntimeException("Unexpected, cannot disconnect session. Session object is null.");
		}
		User lostUser = userManager.getUserBySession(session);
		if (lostUser != null) {
			disconnectUser(lostUser);
		} else if (session.isConnected()) {
			try {
				session.close();
			} catch (IOException err) {
				throw new MPRuntimeException(err);
			}
		}
	}
	
	public void disconnectUser(User user, IDisconnectionReason reason) {
		user.getSession().setSystemProperty("disconnectionReason", reason);
		IMPObject resObj = MPObject.newInstance();
		
		resObj.putByte("dr", reason.getByteValue());
		
		IResponse response = new Response();
		response.setId(SystemRequest.OnClientDisconnection.getId());
		response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
		response.setContent(resObj);
		response.setRecipients(user.getSession());
		
		response.write();
	}
	
	public void disconnectUser(User user) {
		if (user == null) {
			throw new MPRuntimeException("Cannot disconnect user, User object is null.");
		}
		ISession session = user.getSession();
		
		try {
			if (session.isConnected()) {
				session.close();
			}
			user.setConnected(false);
		} catch (IOException err) {
			throw new MPRuntimeException(err);
		} finally {
			userManager.removeUser(user);
			notifyUserLost(user);
			user.setConnected(false);
			
			Map<IMPEventParam, Object> evtParams = new HashMap<IMPEventParam, Object>();
			evtParams.put(MPEventParam.USER, user);
			IDisconnectionReason disconnectionReason = (IDisconnectionReason) user.getSession().getSystemProperty("disconnectionReason");
			evtParams.put(MPEventParam.DISCONNECTION_REASON, disconnectionReason == null ? ClientDisconnectionReason.UNKNOWN : disconnectionReason);
			mpnet.getEventManager().dispatchEvent(new MPEvent(MPEventType.USER_DISCONNECT, evtParams));
			
			log.info(String.format("User disconnected: %s, SessionLen: %s, Type: %s", user.toString(), System.currentTimeMillis() - user.getLastLoginTime(), user.getSession().getSystemProperty(MPConstants.SESSION_CLIENT_TYPE)));
		}
	}
	
	private final void notifyUserLost(User user) {
		Set<ISession> recipients = new HashSet<ISession>();
		
		if (recipients.size() > 0) {
			IMPObject resObj = MPObject.newInstance();
			Object response = new Response();
			((IResponse) response).setId(SystemRequest.OnUserLost.getId());
			((IResponse) response).setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
			((IResponse) response).setContent(resObj);
			((IResponse) response).setRecipients(recipients);
			resObj.putInt("u", user.getId());
			((IResponse) response).write();
		}
	}
	
	public User login(final ISession sender, final String name, final String pass, final IMPObject paramsIn, final IMPObject paramsOut, final boolean forceLogout) {
		if (!mpnet.getSessionManager().containsSession(sender)) {
			log.warn("Login failed: " + name + " , session is already expired!");
			return null;
		}
		IMPObject resObj = MPObject.newInstance();
		User user = null;
		IResponse response = new Response();
		response.setId(SystemRequest.Login.getId());
		response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
		response.setContent(resObj);
		response.setRecipients(sender);
		
		try {
			user = userManager.login(sender, name, pass, paramsIn, paramsOut, forceLogout);
			user.setConnected(true);
			sender.setLoggedIn(true);
			
			log.info(String.format("User login: %s, Type: %s", user.toString(), user.getSession().getSystemProperty(MPConstants.SESSION_CLIENT_TYPE)));
			
			user.updateLastRequestTime();
			resObj.putInt(Login.KEY_ID, user.getId());
			resObj.putUtfString(Login.KEY_USERNAME, user.getName());
			resObj.putShort(Login.KEY_RECONNECTION_SECONDS, (short) userManager.getUserReconnectionSeconds());
			resObj.putShort(Login.KEY_PRIVILEGE_ID, user.getPrivilegeId());
			if ((paramsOut != null) && (paramsOut.size() > 0)) {
				resObj.putMPObject(Login.KEY_PARAMS, paramsOut);
			}
			response.write();
			Map<IMPEventParam, Object> evtParams = new HashMap<IMPEventParam, Object>();
			evtParams.put(MPEventParam.USER, user);
			mpnet.getEventManager().dispatchEvent(new MPEvent(MPEventType.USER_JOIN_SERVER, evtParams));
		} catch (MPLoginInterruptedException e) {
			mpnet.getTaskScheduler().schedule(new Runnable() {
				public void run() {
					login(sender, name, pass, paramsIn, paramsOut, false);
				}
			}, 2000, TimeUnit.MILLISECONDS);
		} catch (MPLoginException err) {
			err.printStackTrace();
			log.info("Login error: " + err.getMessage() + ". Requested by: " + sender);
			loginErrorHandler.execute(sender, err);
		}
		return user;
	}
	
	public void logout(User user) {
		if (user == null) {
			throw new MPRuntimeException("Cannot logout null user.");
		}
		user.setConnected(false);
		userManager.removeUser(user);
		user.getSession().setLoggedIn(false);
		notifyUserLost(user);
		
		notifyLogout(user.getSession());
		Map<IMPEventParam, Object> evtParams = new HashMap<IMPEventParam, Object>();
		evtParams.put(MPEventParam.USER, user);
		
		mpnet.getEventManager().dispatchEvent(new MPEvent(MPEventType.USER_LOGOUT, evtParams));
		log.info(String.format("User logout: %s, SessionLen: %s, Type: %s", user.toString(), System.currentTimeMillis() - user.getLastLoginTime(), user.getSession().getSystemProperty(MPConstants.SESSION_CLIENT_TYPE)));
	}
	
	private void notifyLogout(ISession recipient) {
		IMPObject resObj = MPObject.newInstance();
		
		IResponse response = new Response();
		response.setId(SystemRequest.Logout.getId());
		response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
		response.setContent(resObj);
		response.setRecipients(recipient);
		
		response.write();
	}
	
	public void sendExtensionResponse(String cmdName, IMPObject params, List<User> recipients, boolean useUDP) {
		List<ISession> sessions = new ArrayList<ISession>();
		for (User user : recipients) {
			sessions.add(user.getSession());
		}
		sendExtResponse(cmdName, params, sessions, useUDP);
	}
	
	public void sendExtensionResponse(String cmdName, IMPObject params, User recipient, boolean useUDP) {
		List<ISession> msgRecipients = new ArrayList<ISession>();
		msgRecipients.add(recipient.getSession());
		sendExtResponse(cmdName, params, msgRecipients, useUDP);
	}
	
	private final void sendExtResponse(String cmdName, IMPObject params, List<ISession> recipients, boolean sendUDP) {
		IMPObject resObj = MPObject.newInstance();
		resObj.putUtfString("c", cmdName);
		resObj.putMPObject("p", params != null ? params : new MPObject());
		
		IResponse response = new Response();
		response.setId(SystemRequest.CallExtension.getId());
		response.setTargetController(DefaultConstants.CORE_EXTENSIONS_CONTROLLER_ID);
		response.setContent(resObj);
		response.setRecipients(recipients);
		
		if (sendUDP) {
			response.setTransportType(TransportType.UDP);
		}
		response.write();
	}
	
	public void notifyReconnectionFailure(ISession recipient) {
		IResponse response = new Response();
		response.setId(SystemRequest.OnReconnectionFailure.getId());
		response.setRecipients(recipient);
		response.setContent(new MPObject());
		response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
		
		response.write();
		log.info("SENDING TO -------> " + recipient);
	}
	
}