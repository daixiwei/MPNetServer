package com.mpnet.bitswarm.data;

/**
 * 
 * @ClassName: TransportType
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:24:05
 *
 */
public enum TransportType {
	TCP("Tcp"), 
	UDP("Udp");
	
	String	name;
	
	private TransportType(String name) {
		this.name = name;
	}
	
	public String toString() {
		return "(" + name + ")";
	}
	
	public static TransportType fromName(String name) {
		for (TransportType tt : values()) {
			if (tt.name.equalsIgnoreCase(name)) {
				return tt;
			}
		}
		throw new IllegalArgumentException("There is no TransportType definition for the requested type: " + name);
	}
}