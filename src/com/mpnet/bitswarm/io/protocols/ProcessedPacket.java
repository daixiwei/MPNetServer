package com.mpnet.bitswarm.io.protocols;

/**
 * 
 * @ClassName: ProcessedPacket
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:28:08
 *
 */
public class ProcessedPacket {
	private byte[]			data;
	private PacketReadState	state;
	
	public ProcessedPacket(PacketReadState state, byte[] data) {
		this.state = state;
		this.data = data;
	}
	
	public byte[] getData() {
		return this.data;
	}
	
	public PacketReadState getState() {
		return this.state;
	}
}