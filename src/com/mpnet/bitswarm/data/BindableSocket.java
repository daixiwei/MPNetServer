package com.mpnet.bitswarm.data;

import java.nio.channels.SelectableChannel;

/**
 * 
 * @ClassName: BindableSocket
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:23:35
 *
 */
public class BindableSocket {
	protected SelectableChannel	channel;
	private String				address;
	private int					port;
	private TransportType		type;
	
	public BindableSocket(SelectableChannel channel, String address, int port, TransportType type) {
		this.address = address;
		this.port = port;
		this.type = type;
		
		this.channel = channel;
	}
	
	public SelectableChannel getChannel() {
		return channel;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getPort() {
		return port;
	}
	
	public TransportType getType() {
		return type;
	}
	
	public String toString() {
		return String.format("%s%s[%d]", type, address, port);
	}
}