package com.mpnet.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.controllers.SimpleController;
import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.config.DefaultConstants;
import com.mpnet.controllers.IControllerCommand;
import com.mpnet.controllers.SystemRequest;
//import com.mpnet.v2.entities.User;
import com.mpnet.exceptions.MPRuntimeException;
import com.mpnet.exceptions.RequestQueueFullException;
import com.mpnet.util.Logging;


public class SystemController extends SimpleController {
	private static final Map<Object, String> commandMap = new HashMap<Object, String> ();
	private static final String commandPackage = "com.mpnet.controllers.system.";
//	private int qSize;
	private final MPNetServer mpnet;
	private final Logger logger;
	private Map<Object, IControllerCommand> commandCache;

	static {
		commandMap.put(SystemRequest.Handshake.getId(), commandPackage + "Handshake");
		commandMap.put(SystemRequest.Login.getId(), commandPackage + "Login");
		commandMap.put(SystemRequest.Logout.getId(), commandPackage + "Logout");
		commandMap.put(SystemRequest.CallExtension.getId(), commandPackage + "CallExtension");
		commandMap.put(SystemRequest.KickUser.getId(), commandPackage + "KickUser");
		commandMap.put(SystemRequest.ManualDisconnection.getId(), commandPackage + "ManualDisconnection");
		commandMap.put(SystemRequest.PingPong.getId(), commandPackage + "PingPong");
	}

	private boolean useCache = true;
	private final ThreadPoolExecutor systemThreadPool;

	public SystemController() {
		mpnet = MPNetServer.getInstance();
		logger = LoggerFactory.getLogger(getClass());
		systemThreadPool = ((ThreadPoolExecutor) mpnet.getSystemThreadPool());
		super.setId(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
	}

	public void init(Object o) {
		super.init(o);
		this.commandCache = new ConcurrentHashMap<Object, IControllerCommand> ();
	}

	public void enqueueRequest(IRequest request) throws RequestQueueFullException {
		if (this.isActive) {
			try {
				processRequest(request);
			} catch (Throwable t) {
				Logging.logStackTrace(this.logger, t);
			}
		}
	}

	protected void processRequest(IRequest request) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug("{IN}: " + SystemRequest.fromId(request.getId()).toString());
		}
		IControllerCommand command = null;
		Object reqId = request.getId();
		if (useCache) {
			command = (IControllerCommand) this.commandCache.get(reqId);
			if (command == null) {
				command = getCommand(reqId);
			}
		} else {
			command = getCommand(reqId);
		}
		if (command != null) {
			if (command.validate(request)) {
				try {
					command.execute(request);
				} catch (MPRuntimeException re) {
					String msg = re.getMessage();
					if (msg != null) {
						logger.warn(msg);
					}
				}
			}
		}
	}

	private IControllerCommand getCommand(Object reqId) {
		IControllerCommand command = null;
		String className = (String) commandMap.get(reqId);
		if (className != null) {
			try {
				Class<?> clazz = Class.forName(className);
				command = (IControllerCommand) clazz.newInstance();
				if(useCache){
					commandCache.put(reqId, command);
				}
			} catch (Exception err) {
				this.logger.error("Could not dynamically instantiate class: " + className + ", Error: " + err);
			}
		} else {
			this.logger.error("Cannot find a controller command for request ID: " + reqId);
		}
		return command;
	}

	public int getQueueSize() {
		return systemThreadPool.getQueue().size();
	}

	public int getThreadPoolSize() {
		return systemThreadPool.getPoolSize();
	}

	public void setThreadPoolSize(int size) {}

	public void handleMessage(Object message) {}
}
