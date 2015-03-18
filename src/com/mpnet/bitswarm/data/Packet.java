package com.mpnet.bitswarm.data;

import com.mpnet.bitswarm.sessions.ISession;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 
 * @ClassName: Packet
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:23:59
 *
 */
public class Packet implements IPacket {
	// protected long creationTime;
	protected Object						data;
	protected MessagePriority				priority;
	protected ISession						sender;
	protected TransportType					transportType;
	protected int							originalSize	= -1;
	protected ConcurrentMap<String, Object>	attributes;
	protected Collection<ISession>			recipients;
	protected byte[]						fragmentBuffer;
	
	public Packet() {
		// this.creationTime = System.nanoTime();
		this.priority = MessagePriority.NORMAL;
		this.transportType = TransportType.TCP;
	}
	
	public Object getAttribute(String key) {
		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}
	
	public void setAttribute(String key, Object attr) {
		if (this.attributes == null) {
			this.attributes = new ConcurrentHashMap<String, Object>();
		}
		this.attributes.put(key, attr);
	}
	
	// public long getCreationTime() {
	// return this.creationTime;
	// }
	//
	// public void setCreationTime(long creationTime) {
	// this.creationTime = creationTime;
	// }
	
	public Object getData() {
		return this.data;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public MessagePriority getPriority() {
		return this.priority;
	}
	
	public void setPriority(MessagePriority priority) {
		this.priority = priority;
	}
	
	public ISession getSender() {
		return this.sender;
	}
	
	public void setSender(ISession sender) {
		this.sender = sender;
	}
	
	public TransportType getTransportType() {
		return this.transportType;
	}
	
	public void setTransportType(TransportType transportType) {
		this.transportType = transportType;
	}
	
	public Collection<ISession> getRecipients() {
		return this.recipients;
	}
	
	public void setRecipients(Collection<ISession> recipients) {
		this.recipients = recipients;
	}
	
	public boolean isTcp() {
		return this.transportType == TransportType.TCP;
	}
	
	public boolean isUdp() {
		return this.transportType == TransportType.UDP;
	}
	
	public boolean isFragmented() {
		return this.fragmentBuffer != null;
	}
	
	public int getOriginalSize() {
		return this.originalSize;
	}
	
	public void setOriginalSize(int originalSize) {
		if (this.originalSize == -1)
			this.originalSize = originalSize;
	}
	
	public byte[] getFragmentBuffer() {
		return this.fragmentBuffer;
	}
	
	public void setFragmentBuffer(byte[] bb) {
		this.fragmentBuffer = bb;
	}
	
	public String toString() {
		return String.format("{ Packet: %s, data: %s, Pri: %s }", transportType, data.getClass().getName(), priority);
	}
	
	public IPacket clone() {
		IPacket newPacket = new Packet();
		
		// newPacket.setCreationTime(getCreationTime());
		newPacket.setData(getData());
		newPacket.setOriginalSize(getOriginalSize());
		newPacket.setPriority(getPriority());
		newPacket.setRecipients(null);
		newPacket.setSender(getSender());
		newPacket.setTransportType(getTransportType());
		
		return newPacket;
	}
}