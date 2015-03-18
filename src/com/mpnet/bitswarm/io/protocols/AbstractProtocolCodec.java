package com.mpnet.bitswarm.io.protocols;

import com.mpnet.bitswarm.controllers.IController;
import com.mpnet.bitswarm.controllers.IControllerManager;
import com.mpnet.bitswarm.core.BitSwarmEngine;
import com.mpnet.bitswarm.io.IOHandler;
import com.mpnet.bitswarm.io.IProtocolCodec;
import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.exceptions.RequestQueueFullException;
import com.mpnet.util.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: AbstractProtocolCodec
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:39:17
 *
 */
public abstract class AbstractProtocolCodec implements IProtocolCodec {
	protected final IControllerManager	controllerManager;
	protected final BitSwarmEngine		engine;
	protected final Logger				logger;
	protected IOHandler					ioHandler;
	
	public AbstractProtocolCodec() {
		logger = LoggerFactory.getLogger(getClass());
		engine = BitSwarmEngine.getInstance();
		controllerManager = engine.getControllerManager();
	}
	
	/**
	 * 
	 * @param request
	 * @param controllerId
	 */
	protected void dispatchRequestToController(IRequest request, Object controllerId) {
		if (controllerId == null) {
			throw new IllegalStateException("Invalid Request: missing controllerId -> " + request);
		}
		IController controller = controllerManager.getControllerById(controllerId);
		try {
			controller.enqueueRequest(request);
		} catch (RequestQueueFullException err) {
			logger.error(String.format("RequestQueue is full (%s). Controller ID: %s, Dropping incoming request: ", controller.getQueueSize(), controllerId.toString(), request.toString()));
		} catch (NullPointerException err) {
			logger.warn("Can't handle this request! The related controller is not found: " + controllerId + ", Request: " + request);
			Logging.logStackTrace(logger, err);
		}
	}
	
	public IOHandler getIOHandler() {
		return this.ioHandler;
	}
	
	public void setIOHandler(IOHandler handler) {
		this.ioHandler = handler;
	}
}