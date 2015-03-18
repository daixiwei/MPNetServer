package com.mpnet.extensions;

import com.mpnet.MPNetServer;
import com.mpnet.api.IMPApi;
import com.mpnet.common.data.IMPObject;
import com.mpnet.core.IMPEvent;
import com.mpnet.core.IMPEventListener;
import com.mpnet.core.MPConstants;
import com.mpnet.core.MPEventType;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPRuntimeException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: BaseMPExtension 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 下午4:31:44 
 *
 */
public abstract class BaseMPExtension implements IMPExtension, IMPEventListener {
	private String name;
	private String className;
	private volatile boolean active;
	private final MPNetServer mpnet;
	protected volatile int lagSimulationMillis = 0;
	private final Logger logger;
	protected final IMPApi mpApi;
	protected ILoginHandler loginHandler;
	
	public BaseMPExtension() {
		logger = LoggerFactory.getLogger("Extensions");
		active = true;

		mpnet = MPNetServer.getInstance();
		mpApi = mpnet.getAPIManager().getApi();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (this.name != null) {
			throw new MPRuntimeException("Cannot redefine name of extension: " + toString());
		}
		this.name = name;
	}

	public String getExtensionClassName() {
		return className;
	}

	public void setLoginHandler(ILoginHandler loginHandler){
		this.loginHandler = loginHandler;
	}
	
	public ILoginHandler getLoginHandler(){
		return loginHandler;
	}

	public void handleServerEvent(IMPEvent event) throws Exception {}

	public Object handleInternalMessage(String cmdName, Object params) {
		return null;
	}

	public void setExtensionClassName(String fileName) {
		if (this.className != null) {
			throw new MPRuntimeException("Cannot redefine file name of an extension: " + toString());
		}
		this.className = fileName;
	}

	public void addEventListener(MPEventType eventType, IMPEventListener listener) {
		mpnet.getExtensionManager().addEventListener(eventType, listener);
	}

	public void removeEventListener(MPEventType eventType, IMPEventListener listener) {
		mpnet.getExtensionManager().removeEventListener(eventType, listener);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean flag) {
		this.active = flag;
	}

	public void send(String cmdName, IMPObject params, User recipient) {
		send(cmdName, params, recipient, false);
	}

	public void send(String cmdName, IMPObject params, User recipient, boolean useUDP) {
		if (useUDP) {
			params.removeElement(MPConstants.REQUEST_UDP_PACKET_ID);
		}
		checkLagSimulation();
		mpApi.sendExtensionResponse(cmdName, params, recipient, useUDP);
	}

	public void send(String cmdName, IMPObject params, List<User> recipients, boolean useUDP) {
		if (useUDP) {
			params.removeElement(MPConstants.REQUEST_UDP_PACKET_ID);
		}
		checkLagSimulation();
		mpApi.sendExtensionResponse(cmdName, params, recipients, useUDP);
	}

	public void send(String cmdName, IMPObject params, List<User> recipients) {
		send(cmdName, params, recipients, false);
	}

	public String toString() {
		return String.format("{ Ext: %s}", name);
	}

	public void trace(Object... args) {
		trace(ExtensionLogLevel.INFO, args);
	}

	public void trace(ExtensionLogLevel level, Object... args) {
		String traceMsg = getTraceMessage(args);

		if (level == ExtensionLogLevel.DEBUG) {
			logger.debug(traceMsg);
		} else if (level == ExtensionLogLevel.INFO) {
			logger.info(traceMsg);
		} else if (level == ExtensionLogLevel.WARN) {
			logger.warn(traceMsg);
		} else if (level == ExtensionLogLevel.ERROR) {
			logger.error(traceMsg);
		}
	}

	private String getTraceMessage(Object... args) {
		StringBuilder traceMsg = new StringBuilder().append("{").append(name).append("}: ");

		for (Object o : args) {
			traceMsg.append(o.toString()).append(" ");
		}
		return traceMsg.toString();
	}

	protected void removeEventsForListener(IMPEventListener listener) {
		mpnet.getExtensionManager().removeListener(listener);
	}

	private void checkLagSimulation() {
		if (lagSimulationMillis > 0) {
			try {
				logger.debug("Lag simulation, sleeping for: " + lagSimulationMillis + "ms.");
				Thread.sleep(lagSimulationMillis);
			} catch (InterruptedException e) {
				logger.warn("Interruption during lag simulation: " + e);
			}
		}
	}
}