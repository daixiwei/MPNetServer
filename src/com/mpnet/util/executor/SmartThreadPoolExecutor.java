package com.mpnet.util.executor;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: SmartThreadPoolExecutor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午11:32:21
 *
 */
public class SmartThreadPoolExecutor extends ThreadPoolExecutor {
	private final Logger				logger;
	private final SmartExecutorConfig	cfg;
	private final int					maxThreads;
	private final int					backupThreadsExpirySeconds;
	private volatile long				lastQueueCheckTime;
	private volatile long				lastBackupTime;
	private volatile boolean			threadShutDownNotified	= false;
	
	private static final class MPThreadFactory implements ThreadFactory {
		private static final AtomicInteger	POOL_ID				= new AtomicInteger(0);
		private static final String			THREAD_BASE_NAME	= "MPWorker:%s:%s";
		private final AtomicInteger			threadId			= new AtomicInteger(1);
		private final String				poolName;
		
		public MPThreadFactory(String poolName) {
			this.poolName = poolName;
			
			POOL_ID.incrementAndGet();
		}
		
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, String.format(THREAD_BASE_NAME, poolName != null ? poolName : POOL_ID.get(), threadId.getAndIncrement()));
			if (t.isDaemon()) {
				t.setDaemon(false);
			}
			if (t.getPriority() != 5) {
				t.setPriority(5);
			}
			return t;
		}
	}
	
	public SmartThreadPoolExecutor(SmartExecutorConfig config) {
		super(config.coreThreads, 2147483647, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new MPThreadFactory(config.name));
		
		this.cfg = config;
		this.logger = LoggerFactory.getLogger(getClass());
		
		this.maxThreads = (cfg.coreThreads + cfg.backupThreads * cfg.maxBackups);
		this.backupThreadsExpirySeconds = (cfg.backupThreadsExpiry * 1000);
		this.lastQueueCheckTime = -1L;
	}
	
	public void execute(Runnable command) {
		if (getPoolSize() >= cfg.coreThreads) {
			boolean needsBackup = checkQueueWarningLevel();
			if (needsBackup) {
				if (getPoolSize() >= maxThreads) {
					logger.warn(String.format("Queue size is big: %s, but all backup thread are already active: %s", getQueue().size(), getPoolSize()));
				} else {
					setCorePoolSize(getPoolSize() + this.cfg.backupThreads);
					
					lastBackupTime = (this.lastQueueCheckTime = System.currentTimeMillis());
					
					threadShutDownNotified = false;
					
					logger.info(String.format("Added %s new threads, current size is: %s", cfg.backupThreads, getPoolSize()));
				}
			} else if (getPoolSize() > cfg.coreThreads) {
				boolean isTimeToShutDownBackupThreads = System.currentTimeMillis() - this.lastBackupTime > this.backupThreadsExpirySeconds;
				boolean isQueueSizeSmallEnough = getQueue().size() < cfg.queueSizeTriggeringBackupExpiry;
				if ((isTimeToShutDownBackupThreads) && (isQueueSizeSmallEnough) && (!this.threadShutDownNotified)) {
					setCorePoolSize(this.cfg.coreThreads);
					
					threadShutDownNotified = true;
					
					logger.info("Shutting down old backup threads");
				}
			}
		}
		super.execute(command);
	}
	
	private boolean checkQueueWarningLevel() {
		boolean needsBackup = false;
		boolean queueIsBusy = getQueue().size() >= cfg.queueSizeTriggeringBackup;
		long now = System.currentTimeMillis();
		if (this.lastQueueCheckTime < 0L) {
			this.lastQueueCheckTime = now;
		}
		if (queueIsBusy) {
			if (now - this.lastQueueCheckTime > cfg.secondsTriggeringBackup * 1000) {
				needsBackup = true;
			}
		} else {
			lastQueueCheckTime = now;
		}
		return needsBackup;
	}
	
	public int getCoreThreads() {
		return cfg.coreThreads;
	}
	
	public int getBackupThreads() {
		return cfg.backupThreads;
	}
	
	public int getMaxBackups() {
		return cfg.maxBackups;
	}
	
	public int getQueueSizeTriggeringBackup() {
		return cfg.queueSizeTriggeringBackup;
	}
	
	public int getSecondsTriggeringBackup() {
		return cfg.secondsTriggeringBackup;
	}
	
	public int getBackupThreadsExpiry() {
		return cfg.backupThreadsExpiry;
	}
	
	public int getQueueSizeTriggeringBackupExpiry() {
		return cfg.queueSizeTriggeringBackupExpiry;
	}
	
	public int getMaxQueueSize() {
		return maxThreads;
	}
}
