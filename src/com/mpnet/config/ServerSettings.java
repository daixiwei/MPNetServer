package com.mpnet.config;

import java.util.ArrayList;
import java.util.List;
import com.mpnet.util.executor.SmartExecutorConfig;

/**
 * 
 * @ClassName: ServerSettings 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月14日 下午5:53:20 
 *
 */
public class ServerSettings {
	public volatile List<SocketAddress>	socketAddresses					= new ArrayList<SocketAddress>();
	public volatile IpFilterSettings	ipFilter						= new IpFilterSettings();
	public volatile int					maxConnectionsPerIp				= 99999;
	public volatile int					schedulerThreadPoolSize			= 1;
	public volatile int					protocolCompressionThreshold	= 300;
	
	public volatile boolean				useDebugMode					= false;
	public volatile boolean				useFriendlyExceptions			= true;
	public volatile int					controllerRequestQueueSize		= 10000;
	
	public String						readBufferType					= "HEAP";
	public String						writeBufferType					= "HEAP";
	public int							maxIncomingRequestSize			= 4096;
	public int							maxReadBufferSize				= 1024;
	public int							maxWriteBufferSize				= 32768;
	public int							socketAcceptorThreadPoolSize	= 1;
	public int							socketReaderThreadPoolSize		= 1;
	public int							socketWriterThreadPoolSize		= 1;
	public int							sessionPacketQueueSize			= 120;
	public int							sessionMaxIdleTime;
	public int							userMaxIdleTime;
	public boolean						tcpNoDelay						= false;
	
	public volatile boolean				ghostHunterEnabled				= true;
	public volatile boolean				statsExtraLoggingEnabled		= true;
	public SmartExecutorConfig			systemThreadPoolSettings		= new SmartExecutorConfig();
	public SmartExecutorConfig			extensionThreadPoolSettings		= new SmartExecutorConfig();
	public UserSettings					userSettings					= new UserSettings();
	public WordFilterSettings			wordsFilter						= new WordFilterSettings();
	public ExtensionSettings			extensionSettings				= new ExtensionSettings();
	public DatabaseManagerSettings		databaseManager					= new DatabaseManagerSettings();
	
	// /**
	// *
	// * @ClassName: IpFilterSettings
	// * @Description: TODO(这里用一句话描述这个类的作用)
	// * @author daixiwei daixiwei15@126.com
	// * @date 2015年2月5日 下午5:04:12
	// *
	// */
	public static final class IpFilterSettings {
		public List<String>	addressBlackList			= new ArrayList<String>();
		public List<String>	addressWhiteList			= new ArrayList<String>();
		public volatile int	maxConnectionsPerAddress	= 99999;
	}
	
	/**
	 * 
	 * @ClassName: SocketAddress
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年2月5日 下午5:04:16
	 *
	 */
	public static final class SocketAddress {
		public static final String	TYPE_UDP	= "UDP";
		public static final String	TYPE_TCP	= "TCP";
		public String				address		= "127.0.0.1";
		public int					port		= 9339;
		public String				type		= "TCP";
	}
	
	/**
	 * 
	 * @ClassName: UserSettings
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年2月5日 下午4:33:01
	 *
	 */
	public static final class UserSettings {
		public boolean	isCustomLogin			= false;
		public boolean	isForceLogout			= true;
		public boolean	isFilterUserNames		= true;
		public int		maxUsers				= 1000;
		public int		userReconnectionSeconds	= 0;
		public int		overrideMaxUserIdleTime	= 120;
		
		public boolean	allowGuestUsers			= true;
		public String	guestUserNamePrefix		= "Guest#";
	}
	
	/**
	 * 
	 * @ClassName: WordFilterSettings
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年2月5日 下午4:33:04
	 *
	 */
	public static final class WordFilterSettings {
		public boolean	isActive					= false;
		public String	wordsFile					= "config/wordsFile.txt";
		public String	hideBadWordWithCharacter	= "*";
	}
	
	/**
	 * 
	 * @ClassName: ExtensionSettings
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年2月5日 下午5:04:41
	 *
	 */
	public static final class ExtensionSettings {
		public String	name		= "";
		public String	className	= "";
	}
	
	/**
	 * 
	 * @ClassName: DatabaseManagerSettings
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年3月2日 下午4:16:26
	 *
	 */
	public static final class DatabaseManagerSettings {
		public boolean	isActive			= false;
		public String	driverName			= "";
		public String	connectionString	= "";
		public String	userName			= "";
		public String	password			= "";
		public boolean	isMapping			= false;
		public String	mappingFile			= "";
	}
}