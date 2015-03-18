package com.mpnet.bitswarm.io.protocols;

/**
 * 
 * @ClassName: ProtocolType 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:24:29 
 *
 */
public enum ProtocolType{
	BINARY("Binary"),
	TEXT("Text"),
	FLASH_CROSSDOMAIN_POLICY("Flash CrossDomain Policy");
	
	private String description;

	private ProtocolType(String description){
		this.description=description;
	}

	public String toString(){
		return this.description;
	}
}