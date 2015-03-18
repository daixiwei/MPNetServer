package com.mpnet.util.executor;

/**
 * 
 * @ClassName: SmartExecutorConfig
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:32:15
 *
 */
public final class SmartExecutorConfig {
	public String	name							= null;
	public int		coreThreads						= 16;
	public int		backupThreads					= 8;
	public int		maxBackups						= 2;
	public int		queueSizeTriggeringBackup		= 500;
	public int		secondsTriggeringBackup			= 60;
	public int		backupThreadsExpiry				= 3600;
	public int		queueSizeTriggeringBackupExpiry	= 300;
	public boolean	logActivity						= true;
	public int		queueFullWarningInterval		= 300;
}
