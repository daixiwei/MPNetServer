package com.mpnet.bitswarm.events;

/**
 * 
 * @ClassName: BitSwarmEvents
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:23:30
 *
 */
public final class BitSwarmEvents {
	public static final String	ENGINE_STARTED					= "serverStarted";
	public static final String	MEMORY_LOW						= "memoryLow";
	public static final String	SESSION_LOST					= "sessionLost";
	public static final String	SESSION_RECONNECTION_TRY		= "sessionReconnectionTry";
	public static final String	SESSION_RECONNECTION_SUCCESS	= "sessionReconnectionSuccess";
	public static final String	SESSION_RECONNECTION_FAILURE	= "sessionReconnectionFailure";
	public static final String	SESSION_ADDED					= "sessionAdded";
	public static final String	SESSION_IDLE					= "sessionIdle";
	public static final String	PACKET_DROPPED					= "packetDropped";
	public static final String	DEBUG_PACKET_WRITE_FAIL			= "debugPacketWriteFail";
	public static final String	SESSION_IDLE_CHECK_COMPLETE		= "sessionIdleCheckComplete";
}