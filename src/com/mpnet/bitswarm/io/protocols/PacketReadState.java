package com.mpnet.bitswarm.io.protocols;

/**
 * 
 * @ClassName: PacketReadState 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:27:58 
 *
 */
public enum PacketReadState {
	WAIT_NEW_PACKET, 
	WAIT_DATA_SIZE, 
	WAIT_DATA_SIZE_FRAGMENT, 
	WAIT_DATA;
}