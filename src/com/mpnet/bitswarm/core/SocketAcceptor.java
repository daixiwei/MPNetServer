package com.mpnet.bitswarm.core;

import com.mpnet.bitswarm.data.BindableSocket;
import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.events.BitSwarmEvents;
import com.mpnet.bitswarm.events.Event;
import com.mpnet.bitswarm.service.BaseCoreService;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.bitswarm.sessions.ISessionManager;
import com.mpnet.config.DefaultConstants;
import com.mpnet.config.ServerSettings.SocketAddress;
import com.mpnet.exceptions.RefusedAddressException;
import com.mpnet.util.Logging;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: SocketAcceptor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月7日 上午10:28:27
 *
 */
public class SocketAcceptor extends BaseCoreService implements ISocketAcceptor, Runnable {
	private final BitSwarmEngine	engine;
	private final Logger			logger;
	private volatile int			threadId		= 1;
	private int						threadPoolSize	= 1;
	private final ExecutorService	threadPool;
	private List<SocketChannel>		acceptableConnections;
	private List<BindableSocket>	boundSockets;
	private IConnectionFilter		connectionFilter;
	private ISessionManager			sessionManager;
	private ISocketReader			socketReader;
	private IDatagramReader			datagramReader;
	private Selector				acceptSelector;
	private volatile boolean		isActive		= false;
	
	public SocketAcceptor() {
		this(1);
	}
	
	public SocketAcceptor(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
		
		engine = BitSwarmEngine.getInstance();
		logger = LoggerFactory.getLogger(SocketAcceptor.class);
		
		threadPool = Executors.newFixedThreadPool(threadPoolSize);
		
		acceptableConnections = new ArrayList<SocketChannel>();
		boundSockets = new ArrayList<BindableSocket>();
		socketReader = engine.getSocketReader();
		datagramReader = engine.getDatagramReader();
		
		connectionFilter = new DefaultConnectionFilter();
		try {
			acceptSelector = Selector.open();
			logger.info("AcceptSelector opened");
		} catch (IOException e) {
			logger.warn("Problems during SocketAcceptor init: " + e);
			Logging.logStackTrace(logger, e);
		}
	}
	
	public void init(Object o) {
		super.init(o);
		if (isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		if (threadPoolSize < 1) {
			throw new IllegalArgumentException("Illegal value for a thread pool size: " + threadPoolSize);
		}
		sessionManager = engine.getSessionManager();
		
		isActive = true;
		initThreadPool();
		
		logger.info("SocketAcceptor initialized");
		checkBoundSockets();
	}
	
	public void destroy(Object o) {
		super.destroy(o);
		
		isActive = false;
		shutDownBoundSockets();
		
		List<Runnable> leftOvers = threadPool.shutdownNow();
		try {
			Thread.sleep(500L);
			
			acceptSelector.close();
		} catch (Exception e) {
			logger.warn("Error when shutting down Accept selector: " + e.getMessage());
		}
		
		logger.info("SocketAcceptor stopped. Unprocessed tasks: " + leftOvers.size());
	}
	
	private void initThreadPool() {
		for (int j = 0; j < threadPoolSize; j++)
			threadPool.execute(this);
	}
	
	public void run() {
		Thread.currentThread().setName("SocketAcceptor-" + threadId++);
		
		while (isActive) {
			try {
				acceptLoop();
			} catch (IOException e) {
				logger.info("I/O Error with Accept Selector: " + e.getMessage());
				Logging.logStackTrace(logger, e);
			}
		}
		
		logger.info("SocketAcceptor threadpool shutting down.");
	}
	
	private void acceptLoop() throws IOException {
		acceptSelector.select();
		
		Set<SelectionKey> readyKeys = acceptSelector.selectedKeys();
		SelectionKey key = null;
		
		for (Iterator<SelectionKey> it = readyKeys.iterator(); it.hasNext();) {
			try {
				key = (SelectionKey) it.next();
				it.remove();
				
				ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
				SocketChannel clientChannel = ssChannel.accept();
				
				logger.trace("Accepted client connection on: " + ssChannel.socket().getInetAddress().getHostAddress() + ":" + ssChannel.socket().getLocalPort());
				
				synchronized (acceptableConnections) {
					acceptableConnections.add(clientChannel);
				}
			} catch (IOException error) {
				logger.info("I/O Error during accept loop: " + error.getMessage());
			}
			
		}
		
		if (isActive)
			socketReader.getSelector().wakeup();
	}
	
	public void handleAcceptableConnections() {
		if (acceptableConnections.size() == 0) {
			return;
		}
		synchronized (acceptableConnections) {
			for (Iterator<SocketChannel> it = acceptableConnections.iterator(); it.hasNext();) {
				SocketChannel connection = (SocketChannel) it.next();
				it.remove();
				try {
					InetAddress iAddr = connection.socket().getInetAddress();
					if (iAddr == null) {
						continue;
					}
					connectionFilter.validateAndAddAddress(iAddr.getHostAddress());
					connection.configureBlocking(false);
					
					connection.socket().setTcpNoDelay(engine.getConfiguration().tcpNoDelay);
					
					SelectionKey selectionKey = connection.register(socketReader.getSelector(), 1);
					
					ISession session = sessionManager.createSession(connection);
					session.setSystemProperty(DefaultConstants.SESSION_SELECTION_KEY, selectionKey);
					
					sessionManager.addSession(session);
					
					Event sessionAddedEvent = new Event(BitSwarmEvents.SESSION_ADDED);
					sessionAddedEvent.setParameter("session", session);
					dispatchEvent(sessionAddedEvent);
				} catch (RefusedAddressException e) {
					logger.info("Refused connection. " + e.getMessage());
					try {
						connection.socket().shutdownInput();
						connection.socket().shutdownOutput();
						connection.close();
					} catch (IOException e1) {
						logger.warn("Additional problem with refused connection. Was not able to shut down the channel: " + e1.getMessage());
					}
					
				} catch (IOException e) {
					StringBuilder sb = new StringBuilder("Failed accepting connection: ");
					
					if ((connection != null) && (connection.socket() != null)) {
						sb.append(connection.socket().getInetAddress().getHostAddress());
					}
					logger.info(sb.toString());
				}
			}
		}
	}
	
	public void bindSocket(SocketAddress socketConfig) throws IOException {
		if (socketConfig.type.equalsIgnoreCase(SocketAddress.TYPE_TCP)) {
			bindTcpSocket(socketConfig.address, socketConfig.port);
		} else if (socketConfig.type.equalsIgnoreCase(SocketAddress.TYPE_UDP)) {
			bindUdpSocket(socketConfig.address, socketConfig.port);
		} else
			throw new UnsupportedOperationException("Invalid transport type!");
	}
	
	public List<BindableSocket> getBoundSockets() {
		ArrayList<BindableSocket> list = null;
		
		synchronized (boundSockets) {
			list = new ArrayList<BindableSocket>(boundSockets);
		}
		
		return list;
	}
	
	public IConnectionFilter getConnectionFilter() {
		return connectionFilter;
	}
	
	public void setConnectionFilter(IConnectionFilter filter) {
		if (connectionFilter != null) {
			throw new IllegalStateException("A connection filter already exists!");
		}
		connectionFilter = filter;
	}
	
	private void bindTcpSocket(String address, int port) throws IOException {
		ServerSocketChannel socketChannel = ServerSocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.socket().bind(new InetSocketAddress(address, port));
		socketChannel.socket().setReuseAddress(true);
		
		socketChannel.register(acceptSelector, 16);
		
		synchronized (boundSockets) {
			boundSockets.add(new BindableSocket(socketChannel, address, port, TransportType.TCP));
		}
		
		logger.info("Added bound tcp socket --> " + address + ":" + port);
	}
	
	private void bindUdpSocket(String address, int port) throws IOException {
		DatagramChannel datagramChannel = DatagramChannel.open();
		datagramChannel.configureBlocking(false);
		datagramChannel.socket().bind(new InetSocketAddress(address, port));
		datagramChannel.socket().setReuseAddress(true);
		
		datagramChannel.register(datagramReader.getSelector(), 1);
		
		synchronized (boundSockets) {
			boundSockets.add(new BindableSocket(datagramChannel, address, port, TransportType.UDP));
		}
		
		logger.info("Added bound udp socket --> " + address + ":" + port);
	}
	
	private void checkBoundSockets() {
		if (boundSockets.size() < 1)
			logger.error("No bound sockets! Check the boot logs for possible problems!");
	}
	
	private void shutDownBoundSockets() {
		List<BindableSocket> problematicSockets = null;
		
		for (BindableSocket bindableSocket : boundSockets) {
			try {
				bindableSocket.getChannel().close();
			} catch (IOException e) {
				if (problematicSockets == null) {
					problematicSockets = new ArrayList<BindableSocket>();
				}
				problematicSockets.add(bindableSocket);
			}
		}
		
		if (problematicSockets != null) {
			StringBuilder sb = new StringBuilder("Problems closing bound socket(s). The following socket(s) raised exceptions: ");
			
			for (BindableSocket socket : problematicSockets) {
				sb.append(socket.toString()).append(" ");
			}
			
			throw new RuntimeException(sb.toString());
		}
	}
}