package com.mpnet.bitswarm.io.protocols;

import com.mpnet.MPNetServer;
import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.bitswarm.data.Packet;
import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.io.AbstractIOHandler;
import com.mpnet.bitswarm.io.IProtocolCodec;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.bitswarm.sessions.Session;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.config.DefaultConstants;
import com.mpnet.entities.User;
import com.mpnet.exceptions.ExceptionMessageComposer;
import com.mpnet.exceptions.MPCodecException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: MPIoHandler 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月18日 上午11:47:53 
 *
 */
public class MPIoHandler extends AbstractIOHandler {
	private static final int		UDP_PACKET_MIN_SIZE	= 13;
	private static final String		KEY_UDP_HANDSHAKE	= "h";
	private final BinaryIoHandler	binHandler;
	private final Logger			logger;
	private final MPNetServer		mpnet;
	
	
	public MPIoHandler() {
		this.logger = LoggerFactory.getLogger(getClass());
		this.mpnet = MPNetServer.getInstance();
		this.binHandler = new BinaryIoHandler(this);
		
		setCodec(new MPProtocolCodec(this));
	}
	
	public void onDataRead(ISession session, byte[] data) {
		if ((data == null) || (data.length < 1)) {
			throw new IllegalArgumentException("Unexpected null or empty byte array!");
		}
		
		ProtocolType sessionProtocol = (ProtocolType) session.getSystemProperty(Session.PROTOCOL);
		
		if (sessionProtocol == null) {
			if (data[0] == 60) {
				return;
			}
			
			session.setSystemProperty(Session.PROTOCOL, ProtocolType.BINARY);
			session.setSystemProperty(Session.PACKET_READ_STATE, PacketReadState.WAIT_NEW_PACKET);
		}
		
		this.binHandler.handleRead(session, data);
	}
	
	public void setCodec(IProtocolCodec codec) {
		super.setCodec(codec);
		this.binHandler.setProtocolCodec(codec);
	}
	
	public long getReadPackets() {
		return this.binHandler.getReadPackets();
	}
	
	public long getIncomingDroppedPackets() {
		return this.binHandler.getIncomingDroppedPackets();
	}
	
	public void onDataRead(DatagramChannel channel, SocketAddress address, byte[] data) {
		String senderIP = null;
		int senderPort = 0;
		try {
			if (data.length < 4) {
				throw new MPCodecException("Packet too small: " + data.length + " bytes");
			}
			
			PacketHeader packetHeader = decodeFirstHeaderByte(data[0]);
			
			int msb = data[1] & 0xFF;
			int lsb = data[2] & 0xFF;
			int dataSize = msb * 256 + lsb;
			
			if (dataSize < UDP_PACKET_MIN_SIZE) {
				throw new MPCodecException("Packet data too small: " + data.length + " bytes");
			}
			
			String[] adrData = address.toString().split("\\:");
			senderIP = adrData[0].substring(1);
			senderPort = Integer.parseInt(adrData[1]);
			
			byte[] mpObjData = new byte[data.length - 3];
			System.arraycopy(data, 3, mpObjData, 0, mpObjData.length);
			
			if (mpObjData.length != dataSize) {
				throw new MPCodecException("Packet truncated. Expected: " + dataSize + ", only got: " + mpObjData.length);
			}
			
			if (packetHeader.isCompressed()) {
				try {
					mpObjData = binHandler.getPacketCompressor().uncompress(mpObjData);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			
			IMPObject reqObj = MPObject.newFromBinaryData(mpObjData);
			
			ISession session = validateUDPRequest(senderIP, senderPort, reqObj);
			
			if (reqObj.containsKey(KEY_UDP_HANDSHAKE)) {
				if (logger.isDebugEnabled()) {
					logger.debug("UDP Handshake OK: " + session);
				}
				if (session.getDatagramChannel() == null) {
					session.setDatagrmChannel(channel);
					sendUDPHandshakeResponse(session);
				} else {
					logger.warn("Client already UDP inited: " + session.toString());
				}
				return;
			}
			
			IPacket newPacket = new Packet();
			newPacket.setData(reqObj);
			newPacket.setSender(session);
			newPacket.setOriginalSize(dataSize);
			newPacket.setTransportType(TransportType.UDP);
			newPacket.setAttribute("type", ProtocolType.BINARY);
			
			this.codec.onPacketRead(newPacket);
		} catch (RuntimeException err) {
			this.logger.warn("Problems decoding UDP packet from: " + senderIP + ":" + senderPort + ", " + err);
			err.printStackTrace();
		} catch (MPCodecException codecErr) {
			this.logger.warn(String.format("Discard UDP packet from %s:%s, reason: %s ", new Object[] { senderIP, Integer.valueOf(senderPort), codecErr.getMessage() }));
		}
	}
	
	private ISession validateUDPRequest(String senderIP, int senderPort, IMPObject packet) throws MPCodecException {
		if (!packet.containsKey("c")) {
			throw new MPCodecException("Missing controllerId");
		}
		if (!packet.containsKey("u")) {
			throw new MPCodecException("Missing userId");
		}
		if (!packet.containsKey("i")) {
			throw new MPCodecException("Missing packet id");
		}
		int userId = packet.getInt("u").intValue();
		User sender = this.mpnet.getUserManager().getUserById(userId);
		
		if (sender == null) {
			throw new MPCodecException("User does not exist, id: " + userId);
		}
		ISession session = sender.getSession();
		
		if (!session.getAddress().equals(senderIP)) {
			throw new MPCodecException(String.format("Sender IP doesn't match TCP session address: %s != %s", senderIP, session.getAddress()));
		}
		Integer sessionUdpPort = (Integer) session.getSystemProperty(DefaultConstants.USP_UDP_PORT);
		
		if (sessionUdpPort == null) {
			session.setSystemProperty(DefaultConstants.USP_UDP_PORT, Integer.valueOf(senderPort));
		} else if (senderPort != sessionUdpPort.intValue()) {
			throw new MPCodecException(String.format("Sender UDP Port doesn't match current session linkPort: %s != %s", senderPort, sessionUdpPort));
		}
		
		return sender.getSession();
	}
	
	public void onDataWrite(IPacket packet) {
		if (packet.getRecipients().size() > 0) {
			try {
				this.binHandler.handleWrite(packet);
			} catch (Exception e) {
				ExceptionMessageComposer composer = new ExceptionMessageComposer(e);
				this.logger.warn(composer.toString());
			}
		}
	}
	
	public PacketHeader decodeFirstHeaderByte(byte headerByte) {
		return new PacketHeader((headerByte & 0x80) > 0, (headerByte & 0x40) > 0,
				(headerByte & 0x20) > 0, (headerByte & 0x10) > 0, (headerByte & 0x8) > 0);
	}
	
	public byte encodeFirstHeaderByte(PacketHeader packetHeader) {
		byte headerByte = 0;
		
		if (packetHeader.isBinary()) {
			headerByte = (byte) (headerByte + 128);
		}
		if (packetHeader.isEncrypted()) {
			headerByte = (byte) (headerByte + 64);
		}
		if (packetHeader.isCompressed()) {
			headerByte = (byte) (headerByte + 32);
		}
		if (packetHeader.isBlueBoxed()) {
			headerByte = (byte) (headerByte + 16);
		}
		if (packetHeader.isBigSized()) {
			headerByte = (byte) (headerByte + 8);
		}
		return headerByte;
	}
	
	private void sendUDPHandshakeResponse(ISession recipient) {
		IMPObject responseObj = new MPObject();
		responseObj.putByte("c", (byte) 1);
		responseObj.putByte(KEY_UDP_HANDSHAKE, (byte) 1);
		responseObj.putShort("a", (byte) 0);
		
		IPacket udpResponsePacket = new Packet();
		udpResponsePacket.setTransportType(TransportType.UDP);
		udpResponsePacket.setRecipients(Arrays.asList(recipient));
		udpResponsePacket.setData(responseObj);
		
		onDataWrite(udpResponsePacket);
	}
}