package com.mpnet.bitswarm.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.bitswarm.events.BitSwarmEventParam;
import com.mpnet.bitswarm.events.BitSwarmEvents;
import com.mpnet.bitswarm.events.Event;
import com.mpnet.bitswarm.events.IEvent;
import com.mpnet.bitswarm.io.IOHandler;
import com.mpnet.bitswarm.service.BaseCoreService;
import com.mpnet.bitswarm.sessions.IPacketQueue;
import com.mpnet.bitswarm.sessions.ISession;
//import com.mpnet.bitswarm.sessions.Session;
import com.mpnet.bitswarm.sessions.SessionType;
//import com.mpnet.bitswarm.sessions.bluebox.IBBClient;
import com.mpnet.config.DefaultConstants;
import com.mpnet.config.ServerSettings;
import com.mpnet.exceptions.MessageQueueFullException;
import com.mpnet.exceptions.PacketQueueWarning;
import com.mpnet.util.Logging;
import com.mpnet.util.NetworkServices;

public final class SocketWriter extends BaseCoreService implements ISocketWriter, Runnable {
	private BitSwarmEngine					engine;
	private IOHandler						ioHandler;
	private final Logger					logger;
	private final ExecutorService			threadPool;
	private final BlockingQueue<ISession>	sessionTicketsQueue;
	private volatile int					threadId				= 1;
	private volatile boolean				isActive				= false;
	private volatile long					droppedPacketsCount		= 0L;
	private volatile long					writtenBytes			= 0L;
	private volatile long					writtenPackets			= 0L;
	private volatile long					droppedUdpPacketsCount	= 0L;
	private int								threadPoolSize;
	
	public SocketWriter(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
		
		threadPool = Executors.newFixedThreadPool(threadPoolSize);
		logger = LoggerFactory.getLogger(SocketWriter.class);
		
		sessionTicketsQueue = new LinkedBlockingQueue<ISession>();
	}
	
	public void init(Object o) {
		super.init(o);
		
		if (isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		if (threadPoolSize < 1) {
			throw new IllegalArgumentException("Illegal value for a thread pool size: " + threadPoolSize);
		}
		engine = BitSwarmEngine.getInstance();
		isActive = true;
		
		initThreadPool();
		
		logger.info("Socket Writer started (pool size:" + threadPoolSize + ")");
	}
	
	public void destroy(Object o) {
		super.destroy(o);
		
		isActive = false;
		List<Runnable> leftOvers = threadPool.shutdownNow();
		logger.info("SocketWriter stopped. Unprocessed tasks: " + leftOvers.size());
	}
	
	public int getQueueSize() {
		return sessionTicketsQueue.size();
	}
	
	public int getThreadPoolSize() {
		return threadPoolSize;
	}
	
	public IOHandler getIOHandler() {
		return ioHandler;
	}
	
	public void setIOHandler(IOHandler ioHandler) {
		if (this.ioHandler != null) {
			throw new IllegalStateException("You cannot reassign the IOHandler class!");
		}
		this.ioHandler = ioHandler;
	}
	
	public void continueWriteOp(ISession session) {
		if (session != null)
			sessionTicketsQueue.add(session);
	}
	
	private void initThreadPool() {
		for (int j = 0; j < threadPoolSize; j++)
			threadPool.execute(this);
	}
	
	public void run() {
		Thread.currentThread().setName("SocketWriter-" + threadId++);
		
		ServerSettings setting = MPNetServer.getInstance().getConfigurator().getServerSettings();
		ByteBuffer writeBuffer = NetworkServices.allocateBuffer(setting.maxWriteBufferSize, setting.writeBufferType);
		
		while (isActive) {
			try {
				ISession session = (ISession) sessionTicketsQueue.take();
				
				processSessionQueue(writeBuffer, session);
			} catch (InterruptedException e) {
				logger.warn("SocketWriter thread interrupted: " + Thread.currentThread());
				isActive = false;
			} catch (Throwable t) {
				logger.warn("Problems in SocketWriter main loop, Thread: " + Thread.currentThread());
				Logging.logStackTrace(logger, t);
			}
		}
		
		logger.info("SocketWriter threadpool shutting down.");
	}
	
	private void processSessionQueue(ByteBuffer writeBuffer, ISession session) {
		if (session != null) {
			SessionType type = session.getType();
			
			if (type == SessionType.DEFAULT) {
				processRegularSession(writeBuffer, session);
			} else if (type == SessionType.BLUEBOX) {
				// processBlueBoxSession(session);
			} else if (type == SessionType.VOID)
				return;
		}
	}
	
	// private void processBlueBoxSession(ISession session) {
	// IPacketQueue sessionQ = session.getPacketQueue();
	// IPacket packet = null;
	//
	// synchronized (sessionQ) {
	// if (!sessionQ.isEmpty()) {
	// packet = sessionQ.take();
	// }
	// }
	//
	// if (packet != null) {
	// IBBClient bbClient = (IBBClient) session.getSystemProperty(Session.BBCLIENT);
	// bbClient.enqueueMessage((byte[]) packet.getData());
	// }
	// }
	
	private void processRegularSession(ByteBuffer writeBuffer, ISession session) {
		if (session.isFrozen()) {
			return;
		}
		IPacket packet = null;
		try {
			IPacketQueue sessionQ = session.getPacketQueue();
			
			synchronized (sessionQ) {
				if (!sessionQ.isEmpty()) {
					packet = sessionQ.peek();
					
					if (packet == null) {
						return;
					}
					
					if (packet.isTcp()) {
						tcpSend(writeBuffer, sessionQ, session, packet);
					} else if (packet.isUdp()) {
						udpSend(writeBuffer, sessionQ, session, packet);
					} else {
						logger.warn("Unknow packet type: " + packet);
					}
					
				}
				
			}
			
		} catch (ClosedChannelException cce) {
			logger.debug("Socket closed during write operation for session: " + session);
		} catch (IOException localIOException) {} catch (Exception e) {
			logger.warn("Error during write. Session: " + session);
			Logging.logStackTrace(logger, e);
		}
	}
	
	private void tcpSend(ByteBuffer writeBuffer, IPacketQueue sessionQ, ISession session, IPacket packet) throws Exception {
		SocketChannel channel = session.getConnection();
		if (channel == null) {
			logger.debug("Skipping packet, found null socket for Session: " + session);
			return;
		}
		writeBuffer.clear();
		
		byte[] buffer = packet.isFragmented() ? packet.getFragmentBuffer() : (byte[]) packet.getData();
		if (writeBuffer.capacity() < buffer.length) {
			if (logger.isTraceEnabled()) {
				logger.trace("Allocating new buffer. Curr. capacity: " + writeBuffer.capacity() + ", Need: " + buffer.length);
			}
			writeBuffer = NetworkServices.allocateBuffer(buffer.length, engine.getConfiguration().writeBufferType);
		}
		writeBuffer.put(buffer);
		writeBuffer.flip();
		
		long toWrite = writeBuffer.remaining();
		
		long bytesWritten = channel.write(writeBuffer);
		
		writtenBytes += bytesWritten;
		session.addWrittenBytes(bytesWritten);
		if (bytesWritten < toWrite) {
			byte[] bb = new byte[writeBuffer.remaining()];
			writeBuffer.get(bb);
			if (logger.isTraceEnabled()) {
				logger.trace("<<< Partial Socket Write >>>");
				logger.trace("Remaining: " + bb.length);
			}
			packet.setFragmentBuffer(bb);
			
			SelectionKey sk = (SelectionKey) session.getSystemProperty(DefaultConstants.SESSION_SELECTION_KEY);
			if ((sk != null) && (sk.isValid())) {
				sk.interestOps(5);
			} else {
				logger.warn("Could not OP_WRITE for Session: " + session + ", written bytes: " + bytesWritten);
				System.out.println("SK: " + sk + ", Valid:" + sk.isValid());
			}
		} else {
			writtenPackets += 1L;
			
			sessionQ.take();
			if (!sessionQ.isEmpty()) {
				sessionTicketsQueue.add(session);
			}
		}
	}
	
	private void udpSend(ByteBuffer writeBuffer, IPacketQueue sessionQ, ISession session, IPacket packet) throws Exception {
		sessionQ.take();
		if (!sessionQ.isEmpty()) {
			sessionTicketsQueue.add(session);
		}
		writeBuffer.clear();
		
		byte[] buffer = (byte[]) packet.getData();
		if (writeBuffer.capacity() < buffer.length) {
			logger.trace("Allocating new buffer. Curr. capacity: " + writeBuffer.capacity() + ", Need: " + buffer.length);
			writeBuffer = NetworkServices.allocateBuffer(buffer.length, engine.getConfiguration().writeBufferType);
		}
		writeBuffer.put(buffer);
		writeBuffer.flip();
		
		DatagramChannel datagramChannel = session.getDatagramChannel();
		Integer sessionUdpPort = (Integer) session.getSystemProperty(DefaultConstants.USP_UDP_PORT);
		if (datagramChannel == null) {
			throw new IllegalStateException("UDP Packet cannot be sent to: " + session + ", no DatagramChannel was ever set!");
		}
		if (sessionUdpPort == null) {
			throw new IllegalStateException("UDP Packet cannot be sent to: " + session + ", no UDP port set.");
		}
		int written = datagramChannel.send(writeBuffer, new InetSocketAddress(session.getAddress(), sessionUdpPort.intValue()));
		if (written != 0) {
			writtenBytes += written;
			session.addWrittenBytes(written);
		} else {
			droppedUdpPacketsCount += 1L;
		}
	}
	
	public void enqueuePacket(IPacket packet) {
		enqueueLocal(packet);
	}
	
	private void enqueueLocal(IPacket packet) {
		Collection<ISession> recipients = packet.getRecipients();
		int size = recipients.size();
		
		if ((recipients != null) && (size > 0)) {
			if (packet.getSender() != null) {
				packet.getSender().setLastWriteTime(System.currentTimeMillis());
			}
			if (size == 1) {
				enqueueLocalPacket((ISession) packet.getRecipients().iterator().next(), packet);
			} else
				for (ISession session : recipients) {
					enqueueLocalPacket(session, packet.clone());
				}
		}
	}
	
	private void enqueueLocalPacket(ISession session, IPacket packet) {
		IPacketQueue sessionQ = session.getPacketQueue();
		boolean isBlueBoxed = session.getType() == SessionType.BLUEBOX;
		
		if (sessionQ != null) {
			synchronized (sessionQ) {
				try {
					boolean wasEmpty = sessionQ.isEmpty();
					
					sessionQ.put(packet);
					
					if ((wasEmpty) || (isBlueBoxed)) {
						sessionTicketsQueue.add(session);
					}
					
					packet.setRecipients(null);
				} catch (PacketQueueWarning err) {
					dropOneMessage(session);
					
					if (logger.isDebugEnabled()) {
						logger.debug(err.getMessage() + ": " + session);
					}
					
				} catch (MessageQueueFullException error) {
					dropOneMessage(session);
				}
			}
		}
	}
	
	private void dropOneMessage(ISession session) {
		session.addDroppedMessages(1);
		droppedPacketsCount += 1L;
		
		IEvent event = new Event(BitSwarmEvents.PACKET_DROPPED);
		event.setParameter(BitSwarmEventParam.SESSION, session);
		dispatchEvent(event);
	}
	
	public long getDroppedPacketsCount() {
		return droppedPacketsCount;
	}
	
	public long getDroppedUdpPacketCount() {
		return droppedUdpPacketsCount;
	}
	
	public long getWrittenBytes() {
		return writtenBytes;
	}
	
	public long getWrittenPackets() {
		return writtenPackets;
	}
}