package com.mpnet.bitswarm.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.bitswarm.controllers.DefaultControllerManager;
import com.mpnet.bitswarm.controllers.IController;
import com.mpnet.bitswarm.controllers.IControllerManager;
import com.mpnet.bitswarm.events.BitSwarmEvents;
import com.mpnet.bitswarm.events.Event;
import com.mpnet.bitswarm.events.IEvent;
import com.mpnet.bitswarm.events.IEventListener;
import com.mpnet.bitswarm.io.IOHandler;
import com.mpnet.bitswarm.io.IResponse;
import com.mpnet.bitswarm.io.protocols.MPIoHandler;
import com.mpnet.bitswarm.service.BaseCoreService;
import com.mpnet.bitswarm.service.IService;
import com.mpnet.bitswarm.sessions.DefaultSessionManager;
import com.mpnet.bitswarm.sessions.ISessionManager;
import com.mpnet.config.DefaultConstants;
import com.mpnet.config.ServerSettings;
import com.mpnet.config.ServerSettings.SocketAddress;
import com.mpnet.controllers.ExtensionController;
import com.mpnet.controllers.SystemController;
import com.mpnet.exceptions.BootSequenceException;
import com.mpnet.util.ITaskHandler;
import com.mpnet.util.Scheduler;

/**
 * 
 * @ClassName: BitSwarmEngine
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:25:12
 *
 */
public final class BitSwarmEngine extends BaseCoreService {
	private static BitSwarmEngine		__engine__;
	private ISocketAcceptor				socketAcceptor;
	private ISocketReader				socketReader;
	private IDatagramReader				datagramReader;
	private ISocketWriter				socketWriter;
	private Scheduler					scheduler;
	private Logger						logger;
	private ServerSettings				configuration;
	private ISessionManager				sessionManager;
	private IControllerManager			controllerManager;
	private volatile boolean			inited	= false;
	private Map<String, IService>		coreServicesByName;
	private Map<IService, Object>		configByService;
	private IEventListener				eventHandler;
	private EngineDelayedTaskHandler	engineDelayedTaskHandler;
	
	public static BitSwarmEngine getInstance() {
		if (__engine__ == null) {
			__engine__ = new BitSwarmEngine();
		}
		return __engine__;
	}
	
	private BitSwarmEngine() {
		setName("BitSwarmEngine");
	}
	
	private void initializeServerEngine() {
		logger = LoggerFactory.getLogger(BitSwarmEngine.class);
		inited = true;
	}
	
	public void start() throws Exception {
		start(null);
	}
	
	public void start(String extraLogMessage) throws Exception {
		if (!inited) {
			initializeServerEngine();
		}
		if (extraLogMessage != null) {
			logger.info(extraLogMessage);
		}
		eventHandler = new IEventListener() {
			public void handleEvent(IEvent event) {
				dispatchEvent(event);
			}
		};
		engineDelayedTaskHandler = new EngineDelayedTaskHandler();
		coreServicesByName = new ConcurrentHashMap<String, IService>();
		configByService = new HashMap<IService, Object>();
		
		bootSequence();
		
		((BaseCoreService) socketAcceptor).addEventListener(BitSwarmEvents.SESSION_ADDED, this.eventHandler);
		((BaseCoreService) socketReader).addEventListener(BitSwarmEvents.SESSION_LOST, this.eventHandler);
		((BaseCoreService) socketWriter).addEventListener(BitSwarmEvents.PACKET_DROPPED, this.eventHandler);
		
		Event engineStartedEvent = new Event(BitSwarmEvents.ENGINE_STARTED);
		dispatchEvent(engineStartedEvent);
	}
	
	private final void bootSequence() throws BootSequenceException, Exception {
		logger.info("BitSwarmEngine :  { " + Thread.currentThread().getName() + " }");
		
		startCoreServices();
		
		bindSockets(configuration.socketAddresses);
		for (IService service : coreServicesByName.values()) {
			if (service != null) {
				service.init(configByService.get(service));
			}
		}
	}
	
	public void shutDownSequence() throws Exception {
		stopCoreServices();
	}
	
	public void write(IResponse response) {
		writeToSocket(response);
	}
	
	private void writeToSocket(IResponse res) {
		socketWriter.getIOHandler().getCodec().onPacketWrite(res);
	}
	
	
	private void startCoreServices() throws Exception {
		// securityManager = new DefaultSecurityManager();
		scheduler = new Scheduler(logger);
		sessionManager = DefaultSessionManager.getInstance();
		logger.info("Session manager ready: " + this.sessionManager);
		// instance controllers
		controllerManager = new DefaultControllerManager();
		configureControllers();
		socketReader = new SocketReader(configuration.socketReaderThreadPoolSize);
		// instance io handler
		IOHandler ioHandler = new MPIoHandler();
		socketReader.setIoHandler(ioHandler);
		// instance (udp)datagram reader
		datagramReader = new DatagramReader();
		datagramReader.setIoHandler(ioHandler);
		// instance socket acceptor
		socketAcceptor = new SocketAcceptor(configuration.socketAcceptorThreadPoolSize);
		socketAcceptor.getConnectionFilter().setMaxConnectionsPerIp(configuration.ipFilter.maxConnectionsPerAddress);
		// instance socket writer
		socketWriter = new SocketWriter(configuration.socketWriterThreadPoolSize);
		socketWriter.setIOHandler(ioHandler);

		scheduler.setName(DefaultConstants.SERVICE_SCHEDULER);
		sessionManager.setName(DefaultConstants.SERVICE_SESSION_MANAGER);
		controllerManager.setName(DefaultConstants.SERVICE_CONTROLLER_MANAGER);
		
		((BaseCoreService) socketAcceptor).setName(DefaultConstants.SERVICE_SOCKET_ACCEPTOR);
		((BaseCoreService) socketReader).setName(DefaultConstants.SERVICE_SOCKET_READER);
		((BaseCoreService) socketWriter).setName(DefaultConstants.SERVICE_SOCKET_WRITER);
		
		coreServicesByName.put(DefaultConstants.SERVICE_SCHEDULER, scheduler);
		coreServicesByName.put(DefaultConstants.SERVICE_SESSION_MANAGER, sessionManager);
		coreServicesByName.put(DefaultConstants.SERVICE_CONTROLLER_MANAGER, controllerManager);
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_ACCEPTOR, (IService) socketAcceptor);
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_READER, (IService) socketReader);
		coreServicesByName.put(DefaultConstants.SERVICE_SOCKET_WRITER, (IService) socketWriter);
		coreServicesByName.put(DefaultConstants.SERVICE_DATAGRAM_READER, (IService) datagramReader);
	}
	
	private void stopCoreServices() throws Exception {
		scheduler.destroy(null);
		((IService) socketWriter).destroy(null);
		((IService) socketReader).destroy(null);
		((IService) datagramReader).destroy(null);
		
		Thread.sleep(2000L);
		
		controllerManager.destroy(null);
		sessionManager.destroy(null);
		((IService) socketAcceptor).destroy(null);
	}
	
	private void configureControllers() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		IController controller = new SystemController();
		controllerManager.addController(controller.getId(), controller);
		controller = new ExtensionController();
		controllerManager.addController(controller.getId(), controller);
	}
	
	private void bindSockets(List<SocketAddress> bindableSockets) {
		for (SocketAddress socketCfg : bindableSockets) {
			try {
				this.socketAcceptor.bindSocket(socketCfg);
			} catch (IOException e) {
				logger.warn("Was not able to bind socket: " + socketCfg);
			}
		}
	}
	
	public IService getServiceByName(String serviceName) {
		return (IService) this.coreServicesByName.get(serviceName);
	}
	
	public ISocketAcceptor getSocketAcceptor() {
		return this.socketAcceptor;
	}
	
	public ISocketReader getSocketReader() {
		return this.socketReader;
	}
	
	public IDatagramReader getDatagramReader() {
		return this.datagramReader;
	}
	
	public ISocketWriter getSocketWriter() {
		return this.socketWriter;
	}
	
	public Logger getLogger() {
		return this.logger;
	}
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	public ServerSettings getConfiguration() {
		return this.configuration;
	}
	
	public void setConfiguration(ServerSettings configuration) {
		this.configuration = configuration;
	}
	
	public ITaskHandler getEngineDelayedTaskHandler() {
		return this.engineDelayedTaskHandler;
	}
	
	public IControllerManager getControllerManager() {
		return this.controllerManager;
	}
	
	public ISessionManager getSessionManager() {
		return this.sessionManager;
	}
	
	public void init(Object o) {
		throw new UnsupportedOperationException("This call is not supported in this class!");
	}
	
	public void destroy(Object o) {
		throw new UnsupportedOperationException("This call is not supported in this class!");
	}
}
