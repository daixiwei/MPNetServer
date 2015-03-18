package com.mpnet.config;

/**
 * 
 * @ClassName: DefaultConstants 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月14日 下午5:52:54 
 *
 */
public final class DefaultConstants {
	public static final String	LOG_LINE_SEPARATOR						= "::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::";
	public static final String	LOG4J_PROPERTIES						= "config/log4j.properties";
	public static final String	SERVER_CFG_FILE							= "config/server.xml";
	
	public static final int		CLIENT_API_MIN_REQUESTED_VERSION		= 60;
	public static final Byte	CORE_SYSTEM_CONTROLLER_ID				= new Byte((byte) 0);
	public static final Byte	CORE_EXTENSIONS_CONTROLLER_ID			= new Byte((byte) 1);
	
	public static final String	BUFFER_TYPE_DIRCT						= "DIRECT";
	public static final String	BUFFER_TYPE_HEAP						= "HEAP";
	public static final String	DEFAULT_READ_BUFFER_TYPE				= BUFFER_TYPE_HEAP;
	public static final String	DEFAULT_WRITE_BUFFER_TYPE				= BUFFER_TYPE_HEAP;
	public static final String	SERVICE_SOCKET_ACCEPTOR					= "socketAcceptor";
	public static final String	SERVICE_SOCKET_READER					= "socketReader";
	public static final String	SERVICE_DATAGRAM_READER					= "datagramReader";
	public static final String	SERVICE_SOCKET_WRITER					= "socketWriter";
	public static final String	SERVICE_SCHEDULER						= "scheduler";
	public static final String	SERVICE_SESSION_MANAGER					= "sessionManager";
	public static final String	SERVICE_CONTROLLER_MANAGER				= "controllerManager";
	public static final int		DEFAULT_SESSION_QUEUE_SIZE				= 200;
	public static final int		DEFAULT_READ_MAX_BUFFER_SIZE			= 8192;
	public static final int		DEFAULT_WRITE_MAX_BUFFER_SIZE			= 32768;
	public static final int		DEFAULT_MAX_INCOMING_REQUEST_SIZE		= 4096;
	public static final String	TASK_DELAYED_SOCKET_WRITE				= "delayedSocketWrite";
	public static final String	SESSION_SELECTION_KEY					= "SessionSelectionKey";
	public static final int		DEFAULT_CONNECTION_FILTER_MAX_IP		= 3;
	
	public static final String	USP_INVITATION_PROCESS_RUNNING			= "InvitationProcessRunning";
	public static final String	USP_UDP_PORT							= "UDPPort";
	public static final String	USP_FLOOD_FILTER_REQ_TABLE				= "FloodFilterRequestTable";
	public static final long	BOOT_LOG_MAXSIZE						= 500000000L;
	public static final int		BOOT_LOG_MAX_DAYS						= 5;
	public static final String	BOOT_LOGGING_LEVEL						= "INFO";
	public static final int		ADOBE_MASTER_SOCKET_POLICY_SERVER_PORT	= 843;
	public static final int		MIN_TIME_BETWEEN_CLIENT_SEARCHES		= 1000;
}