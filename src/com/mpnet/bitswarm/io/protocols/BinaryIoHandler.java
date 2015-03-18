package com.mpnet.bitswarm.io.protocols;

import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.core.BitSwarmEngine;
import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.bitswarm.data.Packet;
import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.io.IProtocolCodec;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.bitswarm.sessions.Session;
import com.mpnet.common.data.IMPObject;
import com.mpnet.config.ServerSettings;
import com.mpnet.entities.User;
import com.mpnet.exceptions.ExceptionMessageComposer;
import com.mpnet.util.ByteUtils;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: BinaryIoHandler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:22:51
 *
 */
public class BinaryIoHandler {
	private static final int		SHORT_SIZE				= 2;
	private static final int		INT_SIZE				= 4;
	private final Logger			log;
	private final BitSwarmEngine	engine;
	private final MPNetServer		mpnet;
	private final MPIoHandler		parentHandler;
	private final IPacketCompressor	packetCompressor;
	private IProtocolCodec			protocolCodec;
	private ServerSettings			serverSettings;
	private final int				maxIncomingPacketSize;
	private volatile long			packetsRead				= 0L;
	private volatile long			droppedIncomingPackets	= 0L;
	
	public BinaryIoHandler(MPIoHandler parentHandler) {
		this.log = LoggerFactory.getLogger(getClass());
		this.engine = BitSwarmEngine.getInstance();
		this.mpnet = MPNetServer.getInstance();
		this.parentHandler = parentHandler;
		this.serverSettings = this.mpnet.getConfigurator().getServerSettings();
		this.packetCompressor = new DefaultPacketCompressor();
		this.maxIncomingPacketSize = BitSwarmEngine.getInstance().getConfiguration().maxIncomingRequestSize;
	}
	
	public void setProtocolCodec(IProtocolCodec protocolCodec) {
		this.protocolCodec = protocolCodec;
	}
	
	public IPacketCompressor getPacketCompressor() {
		return this.packetCompressor;
	}
	
	public long getReadPackets() {
		return this.packetsRead;
	}
	
	public long getIncomingDroppedPackets() {
		return this.droppedIncomingPackets;
	}
	
	public void handleWrite(IPacket packet) throws Exception {
		byte[] binData = ((IMPObject) packet.getData()).toBinary();
		packet.setData(binData);
		
		boolean isEncrypted = false;
		if (packet.getAttribute("encryption") != null) {
			isEncrypted = ((Boolean) packet.getAttribute("encryption")).booleanValue();
		}
		
		boolean isCompressed = false;
		int originalSize = binData.length;
		if (!isEncrypted) {
			if (binData.length > this.serverSettings.protocolCompressionThreshold) {
				byte[] beforeCompression = binData;
				binData = this.packetCompressor.compress(binData);
				
				if (binData != beforeCompression) {
					isCompressed = true;
				}
			}
			
		}
		
		int sizeBytes = SHORT_SIZE;
		
		if (binData.length > 65535) {
			sizeBytes = INT_SIZE;
		}
		
		PacketHeader packetHeader = new PacketHeader(true, isEncrypted, isCompressed, false, sizeBytes > SHORT_SIZE);
		
		byte headerByte = this.parentHandler.encodeFirstHeaderByte(packetHeader);
		
		ByteBuffer packetBuffer = ByteBuffer.allocate(1 + sizeBytes + binData.length);
		packetBuffer.put(headerByte);
		
		if (sizeBytes > SHORT_SIZE)
			packetBuffer.putInt(binData.length);
		else {
			packetBuffer.putShort((short) binData.length);
		}
		packetBuffer.put(binData);
		
		packet.setData(packetBuffer.array());
		
		if ((isCompressed) && (this.log.isDebugEnabled())) {
			this.log.debug(String.format(" (cmp: %sb / %sb)", new Object[] { Integer.valueOf(originalSize), Integer.valueOf(binData.length) }));
		}
		if ((binData.length < 1024) && (this.log.isDebugEnabled())) {
			this.log.debug(ByteUtils.fullHexDump((byte[]) packet.getData()));
		}
		
		this.engine.getSocketWriter().enqueuePacket(packet);
	}
	
	public void handleRead(ISession session, byte[] data) {
		if ((data.length < 1024) && (this.log.isDebugEnabled())) {
			this.log.debug(ByteUtils.fullHexDump(data));
		}
		
		PacketReadState readState = (PacketReadState) session.getSystemProperty(Session.PACKET_READ_STATE);
		try {
			while (data.length > 0) {
				if (this.log.isDebugEnabled()) {
					this.log.debug("STATE: " + readState);
				}
				if (readState == PacketReadState.WAIT_NEW_PACKET) {
					ProcessedPacket process = handleNewPacket(session, data);
					readState = process.getState();
					data = process.getData();
				}
				
				if (readState == PacketReadState.WAIT_DATA_SIZE) {
					ProcessedPacket process = handleDataSize(session, data);
					readState = process.getState();
					data = process.getData();
				}
				
				if (readState == PacketReadState.WAIT_DATA_SIZE_FRAGMENT) {
					ProcessedPacket process = handleDataSizeFragment(session, data);
					readState = process.getState();
					data = process.getData();
				}
				
				if (readState != PacketReadState.WAIT_DATA)
					continue;
				ProcessedPacket process = handlePacketData(session, data);
				readState = process.getState();
				data = process.getData();
			}
			
		} catch (Exception err) {
			ExceptionMessageComposer emc = new ExceptionMessageComposer(err);
			this.log.warn(emc.toString());
			
			readState = PacketReadState.WAIT_NEW_PACKET;
		}
		
		session.setSystemProperty(Session.PACKET_READ_STATE, readState);
	}
	
	private ProcessedPacket handleNewPacket(ISession session, byte[] data) {
		PacketHeader header = this.parentHandler.decodeFirstHeaderByte(data[0]);
		
		session.setSystemProperty(Session.DATA_BUFFER, new PendingPacket(header));
		
		data = ByteUtils.resizeByteArray(data, 1, data.length - 1);
		
		return new ProcessedPacket(PacketReadState.WAIT_DATA_SIZE, data);
	}
	
	private ProcessedPacket handleDataSize(ISession session, byte[] data) {
		PacketReadState state = PacketReadState.WAIT_DATA;
		PendingPacket pending = (PendingPacket) session.getSystemProperty(Session.DATA_BUFFER);
		int dataSize = -1;
		int sizeBytes = SHORT_SIZE;
		
		if (pending.getHeader().isBigSized()) {
			if (data.length >= INT_SIZE) {
				dataSize = 0;
				
				for (int i = 0; i < INT_SIZE; i++) {
					int pow256 = (int) Math.pow(256.0D, 3 - i);
					
					int intByte = data[i] & 0xFF;
					
					dataSize += pow256 * intByte;
				}
			}
			
			sizeBytes = INT_SIZE;
			
			if (log.isDebugEnabled()) {
				log.debug("BIG SIZED PACKET: " + (dataSize == -1 ? "Unknown" : Integer.valueOf(dataSize)));
			}
			
		} else {
			if (data.length >= SHORT_SIZE) {
				int msb = data[0] & 0xFF;
				int lsb = data[1] & 0xFF;
				dataSize = msb * 256 + lsb;
			}
			
			if (log.isDebugEnabled()) {
				log.debug("NORMAL SIZED PACKET: " + (dataSize == -1 ? "Unknown" : Integer.valueOf(dataSize)));
			}
		}
		
		if (dataSize != -1) {
			validateIncomingDataSize(session, dataSize);
			
			pending.getHeader().setExpectedLen(dataSize);
			
			pending.setBuffer(ByteBuffer.allocate(dataSize));
			data = ByteUtils.resizeByteArray(data, sizeBytes, data.length - sizeBytes);
		} else {
			state = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
			
			ByteBuffer sizeBuffer = ByteBuffer.allocate(INT_SIZE);
			sizeBuffer.put(data);
			
			pending.setBuffer(sizeBuffer);
			
			data = new byte[0];
		}
		
		return new ProcessedPacket(state, data);
	}
	
	private ProcessedPacket handleDataSizeFragment(ISession session, byte[] data) {
		if (log.isDebugEnabled()) {
			log.debug("Handling DataSize fragment...");
		}
		PacketReadState state = PacketReadState.WAIT_DATA_SIZE_FRAGMENT;
		PendingPacket pending = (PendingPacket) session.getSystemProperty(Session.DATA_BUFFER);
		ByteBuffer sizeBuffer = (ByteBuffer) pending.getBuffer();
		
		int remaining = pending.getHeader().isBigSized() ? INT_SIZE - sizeBuffer.position() : SHORT_SIZE - sizeBuffer.position();
		
		if (data.length >= remaining) {
			sizeBuffer.put(data, 0, remaining);
			sizeBuffer.flip();
			int dataSize = pending.getHeader().isBigSized() ? sizeBuffer.getInt() : sizeBuffer.getShort();
			
			if (this.log.isDebugEnabled()) {
				this.log.debug("DataSize is ready: " + dataSize);
			}
			
			validateIncomingDataSize(session, dataSize);
			pending.getHeader().setExpectedLen(dataSize);
			pending.setBuffer(ByteBuffer.allocate(dataSize));
			
			state = PacketReadState.WAIT_DATA;
			
			if (data.length > remaining)
				data = ByteUtils.resizeByteArray(data, remaining, data.length - remaining);
			else {
				data = new byte[0];
			}
			
		} else {
			sizeBuffer.put(data);
			data = new byte[0];
		}
		
		return new ProcessedPacket(state, data);
	}
	
	private ProcessedPacket handlePacketData(ISession session, byte[] data) throws Exception {
		PacketReadState state = PacketReadState.WAIT_DATA;
		PendingPacket pending = (PendingPacket) session.getSystemProperty(Session.DATA_BUFFER);
		ByteBuffer dataBuffer = (ByteBuffer) pending.getBuffer();
		
		int readLen = dataBuffer.remaining();
		
		boolean isThereMore = data.length > readLen;
		
		if (data.length >= readLen) {
			dataBuffer.put(data, 0, readLen);
			
			if (pending.getHeader().getExpectedLen() != dataBuffer.capacity()) {
				throw new IllegalStateException("Expected data size differs from the buffer capacity! Expected: " + pending.getHeader().getExpectedLen() + ", Buffer size: " + dataBuffer.capacity());
			}
			
			if (log.isDebugEnabled()) {
				log.debug("<<< PACKET COMPLETE >>>");
			}
			
			if (pending.getHeader().isCompressed()) {
				byte[] compressedData;
				if (data.length == readLen) {
					compressedData = dataBuffer.array();
				} else {
					compressedData = new byte[pending.getHeader().getExpectedLen()];
					System.arraycopy(dataBuffer.array(), 0, compressedData, 0, compressedData.length);
				}
				
				long t1 = System.nanoTime();
				byte[] deflatedData = this.packetCompressor.uncompress(compressedData);
				long t2 = System.nanoTime();
				
				if (log.isDebugEnabled()) {
					log.debug("<<< Packet was decompressed >>>");
					log.debug(String.format("Original: %s, Deflated: %s, Comp. Ratio: %s%%, Time: %sms.", dataBuffer.capacity(), deflatedData.length, 100 - dataBuffer.capacity() * 100 / deflatedData.length, (float) (t2 - t1) / 1000000.0F));
				}
				
				dataBuffer = ByteBuffer.wrap(deflatedData);
			}
			
			IPacket newPacket = new Packet();
			newPacket.setData(dataBuffer);
			newPacket.setSender(session);
			newPacket.setOriginalSize(dataBuffer.capacity());
			newPacket.setTransportType(TransportType.TCP);
			
			newPacket.setAttribute("type", ProtocolType.BINARY);
			
			this.packetsRead += 1L;
			this.protocolCodec.onPacketRead(newPacket);
			
			state = PacketReadState.WAIT_NEW_PACKET;
		} else {
			dataBuffer.put(data);
			
			if (log.isDebugEnabled()) {
				log.debug("NOT ENOUGH DATA, GO AHEAD");
			}
		}
		
		if (isThereMore)
			data = ByteUtils.resizeByteArray(data, readLen, data.length - readLen);
		else {
			data = new byte[0];
		}
		return new ProcessedPacket(state, data);
	}
	
	private void validateIncomingDataSize(ISession session, int dataSize) {
		User user = this.mpnet.getUserManager().getUserBySession(session);
		String who = user != null ? user.toString() : session.toString();
		
		if (dataSize < 1) {
			this.droppedIncomingPackets += 1L;
			throw new IllegalArgumentException("Illegal request size: " + dataSize + " bytes, from: " + who);
		}
		
		if (dataSize > this.maxIncomingPacketSize) {
			this.mpnet.getAPIManager().getApi().disconnect(session);
			this.droppedIncomingPackets += 1L;
			
			throw new IllegalArgumentException(String.format("Incoming request size too large: %s, Current limit: %s, From: %s", new Object[] { Integer.valueOf(dataSize), Integer.valueOf(this.maxIncomingPacketSize), who }));
		}
	}
}