package com.mpnet;

import java.io.FileNotFoundException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.api.APIManager;
import com.mpnet.bitswarm.core.BitSwarmEngine;
import com.mpnet.bitswarm.data.BindableSocket;
import com.mpnet.bitswarm.events.BitSwarmEventParam;
import com.mpnet.bitswarm.events.BitSwarmEvents;
import com.mpnet.bitswarm.events.IEvent;
import com.mpnet.bitswarm.events.IEventListener;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.bitswarm.sessions.ISessionManager;
import com.mpnet.config.DefaultConstants;
import com.mpnet.config.IConfigurator;
import com.mpnet.config.MPConfigurator;
import com.mpnet.config.ServerSettings;
import com.mpnet.core.IMPEventManager;
import com.mpnet.core.IMPEventParam;
import com.mpnet.core.MPEvent;
import com.mpnet.core.MPEventManager;
import com.mpnet.core.MPEventParam;
import com.mpnet.core.MPEventType;
import com.mpnet.core.MPShutdownHook;
import com.mpnet.core.ServerState;
import com.mpnet.db.IDBManager;
import com.mpnet.db.MPDBManager;
import com.mpnet.entities.User;
import com.mpnet.entities.managers.IExtensionManager;
import com.mpnet.entities.managers.IUserManager;
import com.mpnet.entities.managers.MPExtensionManager;
import com.mpnet.entities.managers.MPUserManager;
import com.mpnet.exceptions.ExceptionMessageComposer;
import com.mpnet.exceptions.MPException;
import com.mpnet.exceptions.MPRuntimeException;
import com.mpnet.util.CCULoggerTask;
import com.mpnet.util.ClientDisconnectionReason;
import com.mpnet.util.GhostUserHunter;
import com.mpnet.util.IGhostUserHunter;
import com.mpnet.util.MPRestart;
import com.mpnet.util.ServerUptime;
import com.mpnet.util.TaskScheduler;
import com.mpnet.util.executor.SmartExecutorConfig;
import com.mpnet.util.executor.SmartThreadPoolExecutor;

/**
 * The server main class.
 * 
 * @ClassName: MPNetServer
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:35:08
 *
 */
public final class MPNetServer {
	/**
	 * The smartfox server version.
	 */
	private final String			version			= "1.0.0";
	/**
	 * The smartfox server class instance.
	 */
	private static MPNetServer		_instance		= null;
	private static AtomicInteger	restartCounter	= new AtomicInteger(0);
	private final BitSwarmEngine	bitSwarmEngine;
	private final Logger			log;
	private APIManager				apiManager;
	private volatile ServerState	state			= ServerState.STARTING;
	private volatile boolean		initialized		= false;
	private volatile boolean		started			= false;
	private volatile long			serverStartTime;
	private volatile boolean		isRebooting		= false;
	private volatile boolean		isHalting		= false;
	private final IConfigurator		configurator;
	private IEventListener			networkEvtListener;
	private TaskScheduler			taskScheduler;
	private final IMPEventManager	eventManager;
	private IGhostUserHunter		ghostUserHunter;
	private IUserManager			userManager;
	private IExtensionManager		extensionManager;
	private SmartThreadPoolExecutor	sysmtemWorkerPool;
	private IDBManager				dbManager;
	
	/**
	 * 
	 * @return
	 */
	public static MPNetServer getInstance() {
		if (_instance == null) {
			_instance = new MPNetServer();
		}
		return _instance;
	}
	
	private MPNetServer() {
		bitSwarmEngine = BitSwarmEngine.getInstance();
		configurator = new MPConfigurator();
		
		log = LoggerFactory.getLogger(getClass());
		
		networkEvtListener = new NetworkEvtListener();
		eventManager = new MPEventManager();
		userManager = new MPUserManager();
		extensionManager = new MPExtensionManager();
		taskScheduler = new TaskScheduler(1);
	}
	
	public String getVersion() {
		return version;
	}
	
	public void start() {
		if (!initialized) {
			initialize();
		}
		
		try {
			configurator.loadConfiguration();
			eventManager.init(null);

			initSystemWorkers();
			configureServer();
			configureBitSwarm();
			
			bitSwarmEngine.start("Bit Swarm Engine!");
		} catch (FileNotFoundException e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("There has been a problem loading the server configuration. The server cannot start.");
			msg.setPossibleCauses("Make sure that core.xml and server.xml files exist in your config/ folder.");
			log.error(msg.toString());
		} catch (BindException e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("The specified TCP linkPort cannot be bound to the configured IP address.");
			log.error(msg.toString());
		} catch (MPException e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("An error occurred during the Server boot, preventing it to start.");
			log.error(msg.toString());
		} catch (Exception e) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(e);
			msg.setDescription("Unexpected error during Server boot. The server cannot start.");
			log.error(msg.toString());
		}
	}
	
	public int getRestartCount() {
		return restartCounter.get();
	}
	
	public synchronized void restart() {
		if (isRebooting) {
			return;
		}
		isRebooting = true;
		log.warn("*** SERVER RESTARTING ***");
		try {
			bitSwarmEngine.shutDownSequence();
			started = false;
			
			Thread restarter = new MPRestart();
			restarter.start();
		} catch (Exception e) {
			log.error("Restart Failure: " + e);
		}
	}
	
	public void halt() {
		if (isHalting) {
			return;
		}
		isHalting = true;
		
		log.warn("*** SERVER HALTING ***");
		try {
			Thread stopper = new Thread(new Runnable() {
				int	countDown	= 3;
				
				public void run() {
					while (countDown > 0) {
						log.warn("Server Halt in " + countDown-- + " seconds...");
						try {
							Thread.sleep(1000L);
						} catch (InterruptedException localInterruptedException) {}
					}
					System.exit(0);
				}
			});
			stopper.start();
		} catch (Exception e) {
			log.error("Halt Failure: " + e);
		}
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public boolean isProcessControlAllowed() {
		String osName = System.getProperty("os.name");
		
		return (osName.toLowerCase().indexOf("linux") != -1) || (osName.toLowerCase().indexOf("mac os x") != -1) || (osName.toLowerCase().indexOf("windows") != -1);
	}
	
	public TaskScheduler getTaskScheduler() {
		return taskScheduler;
	}
	
	public IMPEventManager getEventManager() {
		return eventManager;
	}
	
	public IExtensionManager getExtensionManager() {
		return extensionManager;
	}
	
	public IUserManager getUserManager() {
		return userManager;
	}
	
	public ISessionManager getSessionManager() {
		return bitSwarmEngine.getSessionManager();
	}
	
	/**
	 * 
	 * @return
	 */
	public IDBManager getDBManager() {
		return dbManager;
	}
	
	public ServerState getState() {
		return state;
	}
	
	public IConfigurator getConfigurator() {
		return configurator;
	}
	
	public int getMinClientApiVersion() {
		return 60;
	}
	
	public APIManager getAPIManager() {
		return apiManager;
	}
	
	public ServerUptime getUptime() {
		if (serverStartTime == 0L) {
			throw new IllegalStateException("Server not ready yet, cannot provide uptime!");
		}
		return new ServerUptime(System.currentTimeMillis() - serverStartTime);
	}
	
	private void initialize() {
		if (initialized) {
			throw new IllegalStateException("SmartFoxServer engine already initialized!");
		}
		
		PropertyConfigurator.configure(DefaultConstants.LOG4J_PROPERTIES);
		
		showSystemInfo(log);
		
		Runtime.getRuntime().addShutdownHook(new MPShutdownHook());
		
		apiManager = new APIManager();
		apiManager.init(null);
		
		ghostUserHunter = new GhostUserHunter();
		
		bitSwarmEngine.addEventListener(BitSwarmEvents.ENGINE_STARTED, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.SESSION_ADDED, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.SESSION_LOST, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.SESSION_IDLE, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.SESSION_IDLE_CHECK_COMPLETE, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.PACKET_DROPPED, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.SESSION_RECONNECTION_TRY, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.SESSION_RECONNECTION_SUCCESS, networkEvtListener);
		bitSwarmEngine.addEventListener(BitSwarmEvents.SESSION_RECONNECTION_FAILURE, networkEvtListener);
		
		initialized = true;
	}
	
	private void initSystemWorkers() {
		SmartExecutorConfig cfg = getConfigurator().getServerSettings().systemThreadPoolSettings;
		cfg.name = "Sys";
		
		sysmtemWorkerPool = new SmartThreadPoolExecutor(cfg);
	}
	
	public Executor getSystemThreadPool() {
		return sysmtemWorkerPool;
	}
	
	private static void showSystemInfo(Logger log) {
		System.out.println("==============================================================================\n" + 
						   " >>System Info \n" + 
						   "==============================================================================\n");
		
		List<String> props = new ArrayList<String>();
		props.add("os.name");
		props.add("os.arch");
		props.add("os.version");
		props.add("java.version");
		props.add("java.vendor");
		props.add("java.vm.version");
		props.add("java.vm.vendor");
		props.add("java.vm.name");
		
		Runtime rt = Runtime.getRuntime();
		log.info("System CPU(s): " + rt.availableProcessors());
		log.info("VM Max memory: " + rt.maxMemory() / 1000000L + " MB");
		for (Iterator<String> i = props.iterator(); i.hasNext();) {
			String prop = (String) i.next();
			log.info(prop + ": " + System.getProperty(prop));
		}
		
		System.out.println("\n==============================================================================\n" + 
						   ">>Begin start server....\n" + 
						   "============================================================================== \n");
	}
	
	private void configureServer() {
		ServerSettings settings = configurator.getServerSettings();
		taskScheduler.resizeThreadPool(settings.schedulerThreadPoolSize);
		
		ExceptionMessageComposer.globalPrintStackTrace = settings.useDebugMode;
		ExceptionMessageComposer.useExtendedMessages = settings.useFriendlyExceptions;
		
		userManager.init(null);
		
		dbManager = new MPDBManager(settings.databaseManager);
		dbManager.init(null);
		
		extensionManager.init();
	}
	
	private void configureBitSwarm() {
		ServerSettings settings = configurator.getServerSettings();
		String protocolType = "Protocol Type is: BINARY";
		log.info(protocolType);
		bitSwarmEngine.setConfiguration(settings);
	}
	
	private void onSocketEngineStart() {
		for (String blockedIp : configurator.getServerSettings().ipFilter.addressBlackList) {
			bitSwarmEngine.getSocketAcceptor().getConnectionFilter().addBannedAddress(blockedIp);
		}
		
		for (String allowedIp : configurator.getServerSettings().ipFilter.addressWhiteList) {
			bitSwarmEngine.getSocketAcceptor().getConnectionFilter().addWhiteListAddress(allowedIp);
		}
		
		bitSwarmEngine.getSocketAcceptor().getConnectionFilter().setMaxConnectionsPerIp(configurator.getServerSettings().maxConnectionsPerIp);
		
		List<BindableSocket> sockets = bitSwarmEngine.getSocketAcceptor().getBoundSockets();
		String message = "Listening Sockets: ";
		for (BindableSocket socket : sockets) {
			message = message + socket.toString() + " ";
		}
		log.info(message);
		
		log.info("\n\n==============================================================================\n" + 
				 ">>Server(" + version + ") ready!\n" + 
				 "============================================================================== \n");
		
		serverStartTime = System.currentTimeMillis();
		started = true;
		
		eventManager.dispatchEvent(new MPEvent(MPEventType.SERVER_READY));
		
		if (getConfigurator().getServerSettings().statsExtraLoggingEnabled) {
			taskScheduler.scheduleAtFixedRate(new CCULoggerTask(), 1, 1, TimeUnit.MINUTES);
		}
		
	}
	
	private void onSessionClosed(ISession session) {
		apiManager.getApi().disconnect(session);
	}
	
	private void onSessionIdle(ISession idleSession) {
		User user = getUserManager().getUserBySession(idleSession);
		
		if (user == null) {
			throw new MPRuntimeException("IdleSession event ignored, cannot find any User for Session: " + idleSession);
		}
		
		apiManager.getApi().disconnectUser(user, ClientDisconnectionReason.IDLE);
	}
	
	private void onSessionReconnectionTry(ISession session) {
		User user = getUserManager().getUserBySession(session);
		
		if (user == null) {
			throw new MPRuntimeException("-Unexpected- Cannot find any User for Session: " + session);
		}
		
		Map<IMPEventParam, Object> evtParams = new HashMap<IMPEventParam, Object>();
		evtParams.put(MPEventParam.USER, user);
		
		eventManager.dispatchEvent(new MPEvent(MPEventType.USER_RECONNECTION_TRY, evtParams));
	}
	
	private void onSessionReconnectionSuccess(ISession session) {
		User user = getUserManager().getUserBySession(session);
		
		if (user == null) {
			throw new MPRuntimeException("-Unexpected- Cannot find any User for Session: " + session);
		}
		
		Map<IMPEventParam, Object> evtParams = new HashMap<IMPEventParam, Object>();
		evtParams.put(MPEventParam.USER, user);
		
		eventManager.dispatchEvent(new MPEvent(MPEventType.USER_RECONNECTION_SUCCESS, evtParams));
	}
	
	private void onSessionReconnectionFailure(ISession incomingSession) {
		apiManager.getApi().notifyReconnectionFailure(incomingSession);
	}
	
	private class NetworkEvtListener implements IEventListener {
		private NetworkEvtListener() {}
		
		public void handleEvent(IEvent event) {
			String evtName = event.getName();
			
			if (evtName.equals(BitSwarmEvents.ENGINE_STARTED)) {
				onSocketEngineStart();
			} else if (evtName.equals(BitSwarmEvents.SESSION_LOST)) {
				ISession session = (ISession) event.getParameter(BitSwarmEventParam.SESSION);
				
				if (session == null) {
					throw new MPRuntimeException("UNEXPECTED: Session was lost, but session object is NULL!");
				}
				onSessionClosed(session);
			} else if ((evtName.equals(BitSwarmEvents.SESSION_IDLE_CHECK_COMPLETE)) && (getConfigurator().getServerSettings().ghostHunterEnabled)) {
				ghostUserHunter.hunt();
			} else if (evtName.equals(BitSwarmEvents.SESSION_IDLE)) {
				onSessionIdle((ISession) event.getParameter(BitSwarmEventParam.SESSION));
			} else if (evtName.equals(BitSwarmEvents.SESSION_RECONNECTION_TRY)) {
				onSessionReconnectionTry((ISession) event.getParameter(BitSwarmEventParam.SESSION));
			} else if (evtName.equals(BitSwarmEvents.SESSION_RECONNECTION_SUCCESS)) {
				onSessionReconnectionSuccess((ISession) event.getParameter(BitSwarmEventParam.SESSION));
			} else if (evtName.equals(BitSwarmEvents.SESSION_RECONNECTION_FAILURE)) {
				onSessionReconnectionFailure((ISession) event.getParameter(BitSwarmEventParam.SESSION));
			}
		}
	}
}