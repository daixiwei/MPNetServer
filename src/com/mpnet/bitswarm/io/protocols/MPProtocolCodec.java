package com.mpnet.bitswarm.io.protocols;

import com.mpnet.bitswarm.data.IPacket;
import com.mpnet.bitswarm.data.Packet;
import com.mpnet.bitswarm.io.IOHandler;
import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.bitswarm.io.IResponse;
import com.mpnet.bitswarm.io.Request;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.core.MPConstants;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @ClassName: MPProtocolCodec 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月14日 下午5:58:47 
 *
 */
public class MPProtocolCodec extends AbstractProtocolCodec {
	private static final String	CONTROLLER_ID	= "c";
	private static final String	ACTION_ID		= "a";
	private static final String	PARAM_ID		= "p";
	private final AtomicLong	udpPacketCounter;
	
	public MPProtocolCodec(IOHandler ioHandler) {
		setIOHandler(ioHandler);
		this.udpPacketCounter = new AtomicLong();
	}
	
	public void onPacketRead(IPacket packet) {
		if (packet == null) {
			throw new IllegalStateException("Protocol Codec didn't expect a null packet!");
		}
		IMPObject requestObject = null;
		
		if (packet.isTcp()) {
			ByteBuffer buff = (ByteBuffer) packet.getData();
			try {
				requestObject = MPObject.newFromBinaryData(buff.array());
			} catch (Exception e) {
				this.logger.warn("Error deserializing request: " + e);
			}
			
		} else if (packet.isUdp()) {
			requestObject = (IMPObject) packet.getData();
			requestObject.putShort(ACTION_ID, (short) 0);
		}
		
		if (requestObject != null) {
			dispatchRequest(requestObject, packet);
		}
	}
	
	private void dispatchRequest(IMPObject requestObject, IPacket packet) {
		if (requestObject.isNull(CONTROLLER_ID)) {
			throw new IllegalStateException("Request rejected: No Controller ID in request!");
		}
		if (requestObject.isNull(ACTION_ID)) {
			throw new IllegalStateException("Request rejected: No Action ID in request!");
		}
		if (requestObject.isNull(PARAM_ID)) {
			throw new IllegalStateException("Request rejected: Missing parameters field!");
		}
		
		IRequest request = new Request();
		Object controllerKey = null;
		request.setId(requestObject.getShort(ACTION_ID));
		controllerKey = requestObject.getByte(CONTROLLER_ID);
		request.setContent(requestObject.getMPObject(PARAM_ID));
		request.setSender(packet.getSender());
		request.setTransportType(packet.getTransportType());
		
		if (packet.isUdp()) {
			request.setAttribute(MPConstants.REQUEST_UDP_PACKET_ID, requestObject.getLong("i"));
		}
		dispatchRequestToController(request, controllerKey);
	}
	
	public void onPacketWrite(IResponse response) {
		IMPObject params = MPObject.newInstance();
		params.putByte(CONTROLLER_ID, ((Byte) response.getTargetController()).byteValue());
		params.putShort(ACTION_ID, ((Short) response.getId()).shortValue());
		if (response.isUDP()) {
			params.putLong("i", this.udpPacketCounter.getAndIncrement());
		}
		params.putMPObject(PARAM_ID, (IMPObject) response.getContent());
		
		IPacket packet = new Packet();
		packet.setTransportType(response.getTransportType());
		packet.setData(params);
		packet.setRecipients(response.getRecipients());
		
		if ((response.getRecipients().size() > 0) && (this.logger.isDebugEnabled())) {
			logger.debug("{OUT}: " + SystemRequest.fromId(response.getId()));
		}
		
		ioHandler.onDataWrite(packet);
	}
}