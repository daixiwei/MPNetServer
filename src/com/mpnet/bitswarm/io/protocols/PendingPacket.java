package com.mpnet.bitswarm.io.protocols;

/**
 * 
 * @ClassName: PendingPacket
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:28:03
 *
 */
public class PendingPacket {
	private PacketHeader	header;
	private Object			buffer;
	
	public PendingPacket(PacketHeader header) {
		this.header = header;
	}
	
	public PacketHeader getHeader() {
		return this.header;
	}
	
	public Object getBuffer() {
		return this.buffer;
	}
	
	public void setBuffer(Object buffer) {
		this.buffer = buffer;
	}
	
	public String toString() {
		return this.header.toString() + this.buffer.toString();
	}
}