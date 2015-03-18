package com.mpnet.bitswarm.core;

import com.mpnet.bitswarm.io.IOHandler;
import com.mpnet.bitswarm.service.IService;
import com.mpnet.util.Logging;
import com.mpnet.util.NetworkServices;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: DatagramReader
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午1:30:25
 *
 */
public class DatagramReader implements IDatagramReader, Runnable, IService {
	private final BitSwarmEngine	engine;
	private final Logger			logger;
	private final Thread			readerThread;
	private Selector				udpSelector;
	private IOHandler				ioHandler;
	private volatile boolean		isActive	= false;
	private volatile long			readBytes	= 0L;
	
	public DatagramReader() {
		engine = BitSwarmEngine.getInstance();
		logger = LoggerFactory.getLogger(getClass());
		try {
			udpSelector = Selector.open();
			logger.info("UDP Selector opened");
		} catch (IOException e) {
			logger.error("Failed opening TCP Selector: " + e.toString());
			e.printStackTrace();
		}
		
		this.readerThread = new Thread(this, "DatagramReader");
	}
	
	public void init(Object o) {
		if (this.isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		this.isActive = true;
		this.readerThread.start();
		
		this.logger.info("DatagramReader started");
	}
	
	public void destroy(Object o) {
		this.isActive = false;
		try {
			Thread.sleep(500L);
			
			this.udpSelector.close();
		} catch (Exception e) {
			this.logger.warn("Error when shutting down UDP Selector: " + e.getMessage());
			Logging.logStackTrace(this.logger, e);
		}
	}
	
	public void run() {
		ByteBuffer readBuffer = NetworkServices.allocateBuffer(engine.getConfiguration().maxReadBufferSize, engine.getConfiguration().readBufferType);
		
		while (this.isActive) {
			try {
				readIncomingDatagrams(readBuffer);
			} catch (Throwable t) {
				this.logger.warn("Problems in DatagramReader main loop: " + t);
				Logging.logStackTrace(this.logger, t);
			}
		}
		
		this.logger.info("SocketReader threadpool shutting down.");
	}
	
	private void readIncomingDatagrams(ByteBuffer readBuffer) {
		DatagramChannel chan = null;
		SelectionKey key = null;
		try {
			this.udpSelector.select();
			Iterator<?> selectedKeys = this.udpSelector.selectedKeys().iterator();
			
			while (selectedKeys.hasNext()) {
				try {
					key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();
					
					if (!key.isValid()) {
						continue;
					}
					if (!key.isReadable())
						continue;
					readBuffer.clear();
					chan = (DatagramChannel) key.channel();
					readPacket(chan, readBuffer);
				} catch (IOException e) {
					this.logger.warn(String.format("Problem reading UDP Packet, from: %s, Error: %s", new Object[] { chan.toString(), e.toString() }));
				}
				
			}
			
		} catch (ClosedSelectorException e) {
			this.logger.debug("Selector is closed!");
		} catch (CancelledKeyException localCancelledKeyException) {} catch (IOException ioe) {
			this.logger.warn("Datagram selection IOError: " + ioe);
			Logging.logStackTrace(this.logger, ioe);
		} catch (Exception err) {
			this.logger.warn("Generic reading/selection error: " + err);
			Logging.logStackTrace(this.logger, err);
		}
	}
	
	private void readPacket(DatagramChannel chan, ByteBuffer readBuffer) throws IOException {
		long byteCount = 0L;
		SocketAddress address = chan.receive(readBuffer);
		
		if (address != null) {
			byteCount = readBuffer.position();
			
			if (byteCount > 0L) {
				this.readBytes += byteCount;
				
				readBuffer.flip();
				
				byte[] binaryData = new byte[readBuffer.limit()];
				readBuffer.get(binaryData);
				
				ioHandler.onDataRead(chan, address, binaryData);
			}
		} else {
			this.logger.info("Could not read any data from DatagramChannel: " + chan);
		}
	}
	
	public IOHandler getIOHandler() {
		return this.ioHandler;
	}
	
	public long getReadBytes() {
		return this.readBytes;
	}
	
	public long getReadPackets() {
		return this.ioHandler.getReadPackets();
	}
	
	public void setIoHandler(IOHandler handler) {
		if (handler == null) {
			throw new IllegalStateException("IOHandler is already set!");
		}
		this.ioHandler = handler;
	}
	
	public String getName() {
		return "DatagramReader";
	}
	
	public Selector getSelector() {
		return this.udpSelector;
	}
	
	public void handleMessage(Object message) {
		throw new UnsupportedOperationException();
	}
	
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}
}