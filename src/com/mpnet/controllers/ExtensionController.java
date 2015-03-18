package com.mpnet.controllers;

import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.controllers.SimpleController;
import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.config.DefaultConstants;
import com.mpnet.core.MPConstants;
import com.mpnet.entities.User;
import com.mpnet.entities.managers.IExtensionManager;
import com.mpnet.exceptions.ExceptionMessageComposer;
import com.mpnet.exceptions.MPExtensionException;
import com.mpnet.exceptions.RequestQueueFullException;
import com.mpnet.extensions.IMPExtension;
import com.mpnet.util.Logging;

/**
 * 
 * @ClassName: ExtensionController 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:40:55 
 *
 */
public class ExtensionController extends SimpleController {
	public static final String KEY_EXT_CMD = "c";
	public static final String KEY_EXT_PARAMS = "p";
	private final Logger logger;
	private final MPNetServer mpnet;
	private IExtensionManager extensionManager;
	private ThreadPoolExecutor threadPool;
//	private int qSize;

	public ExtensionController() {
		this.logger = LoggerFactory.getLogger(getClass());
		this.mpnet = MPNetServer.getInstance();
		super.setId(DefaultConstants.CORE_EXTENSIONS_CONTROLLER_ID);
	}

	public void init(Object o) {
		super.init(o);
		extensionManager = mpnet.getExtensionManager();
		threadPool = ((ThreadPoolExecutor) mpnet.getEventManager().getThreadPool());
	}

	public void enqueueRequest(final IRequest request) throws RequestQueueFullException {
		threadPool.execute(new Runnable() {
			public void run() {
				if (isActive) {
					try {
						processRequest(request);
					} catch (Throwable t) {
						Logging.logStackTrace(logger, t);
					}
				}
			}
		});
	}

	protected void processRequest(IRequest request) throws Exception {
		if (logger.isDebugEnabled()) {
			logger.debug(request.toString());
		}
		long t1 = System.nanoTime();

		User sender = mpnet.getUserManager().getUserBySession(request.getSender());
		if (sender == null) {
			throw new MPExtensionException("Extension Request refused. Sender is not a User: " + request.getSender());
		}
		IMPObject reqObj = (IMPObject) request.getContent();
		String cmd = reqObj.getUtfString(KEY_EXT_CMD);
		if ((cmd == null) || (cmd.length() == 0)) {
			throw new MPExtensionException("Extension Request refused. Missing CMD. " + sender);
		}

		IMPObject params = reqObj.getMPObject(KEY_EXT_PARAMS);
		if (request.isUdp()) {
			if (params == null) {
				params = new MPObject();
			}
			params.putLong(MPConstants.REQUEST_UDP_PACKET_ID, ((Long) request.getAttribute(MPConstants.REQUEST_UDP_PACKET_ID)).longValue());
		}

		IMPExtension extension = extensionManager.getExtension();
		
		if (extension == null) {
			throw new MPExtensionException("No extensions can be invoked!");
		}
		sender.updateLastRequestTime();
		try {
			extension.handleClientRequest(cmd, sender, params);
		} catch (Exception e) {
			ExceptionMessageComposer composer = new ExceptionMessageComposer(e);
			composer.setDescription("Error while handling client request in extension: " + extension.toString());
			composer.addInfo("Extension Cmd: " + cmd);

			logger.error(composer.toString());
		}
		long t2 = System.nanoTime();
		if (logger.isDebugEnabled()) {
			logger.debug("Extension call executed in: " + (t2 - t1) / 1000000.0D);
		}
	}

	public int getQueueSize() {
		return this.threadPool.getQueue().size();
	}

//	public int getMaxQueueSize() {
//		return this.qSize;
//	}
//
//	public void setMaxQueueSize(int size) {
//		this.qSize = size;
//	}

	public int getThreadPoolSize() {
		return this.threadPool.getPoolSize();
	}

	public void setThreadPoolSize(int size) {}

	public void handleMessage(Object message) {}
}
