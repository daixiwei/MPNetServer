package com.mpnet.util;

import com.mpnet.bitswarm.core.BitSwarmEngine;

/**
 * 
 * @ClassName: EngineStats
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:16:12
 *
 */
public class EngineStats {
	public static long getIncomingBytes() {
		return BitSwarmEngine.getInstance().getSocketReader().getReadBytes() + BitSwarmEngine.getInstance().getDatagramReader().getReadBytes();
	}
	
	public static long getIncomingPackets() {
		return BitSwarmEngine.getInstance().getSocketReader().getReadPackets();
	}
	
	public static long getOutgoingBytes() {
		return BitSwarmEngine.getInstance().getSocketWriter().getWrittenBytes();
	}
	
	public static long getOutgoingPackets() {
		return BitSwarmEngine.getInstance().getSocketWriter().getWrittenPackets();
	}
	
	public static long getOutgoingDroppedPackets() {
		return BitSwarmEngine.getInstance().getSocketWriter().getDroppedPacketsCount();
	}
	
}