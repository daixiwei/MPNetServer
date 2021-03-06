package com.mpnet.util;

import com.mpnet.bitswarm.service.IService;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: TaskScheduler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午5:31:12
 *
 */
public class TaskScheduler implements IService {
	private static AtomicInteger				schedulerId	= new AtomicInteger(0);
	private final ScheduledThreadPoolExecutor	taskScheduler;
	private final String						serviceName;
	private final Logger						logger;
	
	public TaskScheduler(int threadPoolSize) {
		this.serviceName = ("TaskScheduler-" + schedulerId.getAndIncrement());
		this.logger = LoggerFactory.getLogger(getClass());
		this.taskScheduler = new ScheduledThreadPoolExecutor(threadPoolSize);
	}
	
	public void init(Object o) {
		this.logger.info(this.serviceName + " started.");
	}
	
	public void destroy(Object o) {
		List<?> awaitingExecution = this.taskScheduler.shutdownNow();
		this.logger.info(this.serviceName + " stopping. Tasks awaiting execution: " + awaitingExecution.size());
	}
	
	public String getName() {
		return this.serviceName;
	}
	
	public void handleMessage(Object arg0) {}
	
	public void setName(String arg0) {}
	
	public ScheduledFuture<?> schedule(Runnable task, int delay, TimeUnit unit) {
		this.logger.debug("Task scheduled: " + task + ", " + delay + " " + unit);
		return this.taskScheduler.schedule(task, delay, unit);
	}
	
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, int initialDelay, int period, TimeUnit unit) {
		return this.taskScheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
	}
	
	public void resizeThreadPool(int threadPoolSize) {
		this.taskScheduler.setCorePoolSize(threadPoolSize);
	}
	
	public int getThreadPoolSize() {
		return this.taskScheduler.getCorePoolSize();
	}
}