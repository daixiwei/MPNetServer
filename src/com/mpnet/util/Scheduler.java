package com.mpnet.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import com.mpnet.bitswarm.service.IService;

/**
 * 
 * @ClassName: Scheduler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:16:57
 *
 */
public class Scheduler implements IService, Runnable {
	private static AtomicInteger		schedulerId	= new AtomicInteger(0);
	
	private volatile int				threadId	= 1;
	private long						SLEEP_TIME	= 250L;
	private ExecutorService				taskExecutor;
	private LinkedList<ScheduledTask>	taskList;
	private LinkedList<ScheduledTask>	addList;
	private String						serviceName;
	private Logger						logger;
	private volatile boolean			running		= false;
	
	public Scheduler(Logger logger) {
		schedulerId.incrementAndGet();
		this.taskList = new LinkedList<ScheduledTask>();
		this.addList = new LinkedList<ScheduledTask>();
		this.logger = logger;
	}
	
	public void init(Object o) {
		startService();
	}
	
	public void destroy(Object o) {
		stopService();
	}
	
	public String getName() {
		return this.serviceName;
	}
	
	public void setName(String name) {
		this.serviceName = name;
	}
	
	public void handleMessage(Object message) {
		throw new UnsupportedOperationException("not supported in this class version");
	}
	
	public void startService() {
		this.running = true;
		this.taskExecutor = Executors.newSingleThreadExecutor();
		this.taskExecutor.execute(this);
	}
	
	public void stopService() {
		this.running = false;
		List<?> leftOvers = this.taskExecutor.shutdownNow();
		this.taskExecutor = null;
		this.logger.info("Scheduler stopped. Unprocessed tasks: " + leftOvers.size());
	}
	
	public void run() {
		Thread.currentThread().setName("Scheduler" + schedulerId.get() + "-thread-" + this.threadId++);
		this.logger.info("Scheduler started: " + this.serviceName);
		
		while (this.running) {
			try {
				executeTasks();
				Thread.sleep(this.SLEEP_TIME);
			} catch (InterruptedException ie) {
				this.logger.warn("Scheduler: " + this.serviceName + " interrupted.");
			} catch (Exception e) {
				Logging.logStackTrace(this.logger, "Scheduler: " + this.serviceName + " caught a generic exception: " + e, e.getStackTrace());
			}
		}
	}
	
	public void addScheduledTask(Task task, int interval, boolean loop, ITaskHandler callback) {
		synchronized (addList) {
			addList.add(new ScheduledTask(task, interval, loop, callback));
		}
	}
	
	private void executeTasks() {
		long now = System.currentTimeMillis();
		
		if (taskList.size() > 0) {
			synchronized (taskList) {
				for (Iterator<ScheduledTask> it = taskList.iterator(); it.hasNext();) {
					ScheduledTask t = (ScheduledTask) it.next();
					
					if (!t.task.isActive()) {
						it.remove();
					} else {
						if (now < t.expiry) {
							continue;
						}
						try {
							t.callback.doTask(t.task);
						} catch (Exception e) {
							Logging.logStackTrace(this.logger, "Scheduler callback exception. Callback: " + t.callback + ", Exception: " + e, e.getStackTrace());
						}
						
						if (t.loop) {
							t.expiry += t.interval * 1000;
						} else {
							it.remove();
						}
					}
				}
			}
			
		}
		
		if (this.addList.size() > 0) {
			synchronized (this.taskList) {
				this.taskList.addAll(this.addList);
				this.addList.clear();
			}
		}
	}
	
	private final class ScheduledTask {
		long			expiry;
		int				interval;
		boolean			loop;
		ITaskHandler	callback;
		Task			task;
		
		public ScheduledTask(Task t, int interval, boolean loop, ITaskHandler callback) {
			this.task = t;
			this.interval = interval;
			this.expiry = (System.currentTimeMillis() + interval * 1000);
			this.callback = callback;
			this.loop = loop;
		}
	}
}