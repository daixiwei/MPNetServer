package com.mpnet.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpnet.MPNetServer;
import com.mpnet.util.executor.SmartExecutorConfig;
import com.mpnet.util.executor.SmartThreadPoolExecutor;

/**
 * 
 * @ClassName: MPEventManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:22:00 
 *
 */
public final class MPEventManager extends BaseCoreService implements IMPEventManager {
	private ThreadPoolExecutor threadPool;
	private final Map<MPEventType, Set<IMPEventListener>> listenersByEvent;
	private final Logger logger;
	private boolean inited = false;


	private static final class MPEventRunner implements Runnable {
		private final IMPEventListener listener;
		private final IMPEvent event;

		public MPEventRunner(IMPEventListener listener, IMPEvent event) {
			this.listener = listener;
			this.event = event;
		}

		public void run() {
			try {
				listener.handleServerEvent(this.event);
			} catch (Exception e) {
				LoggerFactory.getLogger(MPEventManager.class).warn("Error in event handler: " + e + ", Event: " + this.event + " Listener: " + this.listener);
			}
		}
	}

	public MPEventManager() {
		setName("MPEventManager");

		logger = LoggerFactory.getLogger(MPEventManager.class);

		listenersByEvent = new ConcurrentHashMap<MPEventType, Set<IMPEventListener>>();
	}

	public synchronized void init(Object o) {
		if (!inited) {
			super.init(o);

			SmartExecutorConfig cfg = MPNetServer.getInstance().getConfigurator().getServerSettings().extensionThreadPoolSettings;
			cfg.name = "Ext";

			threadPool = new SmartThreadPoolExecutor(cfg);

			logger.info(this.name + " initalized");
			inited = true;
		}
	}

	public void destroy(Object o) {
		super.destroy(o);
		listenersByEvent.clear();
		logger.info(name + " shut down.");
	}

	public Executor getThreadPool() {
		return threadPool;
	}

	public void setThreadPoolSize(int poolSize) {
		threadPool.setCorePoolSize(poolSize);
	}

	public synchronized void addEventListener(MPEventType type, IMPEventListener listener) {
		Set<IMPEventListener> listeners = listenersByEvent.get(type);
		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<IMPEventListener>();
			listenersByEvent.put(type, listeners);
		}
		listeners.add(listener);
	}

	public boolean hasEventListener(MPEventType type) {
		boolean found = false;

		Set<IMPEventListener> listeners = listenersByEvent.get(type);
		if ((listeners != null) && (listeners.size() > 0)) {
			found = true;
		}
		return found;
	}

	public synchronized void removeEventListener(MPEventType type, IMPEventListener listener) {
		Set<IMPEventListener> listeners = listenersByEvent.get(type);
		if (listeners != null) {
			listeners.remove(listener);
		}
	}

	public void dispatchEvent(IMPEvent event) {
		Set<IMPEventListener> listeners = listenersByEvent.get(event.getType());
		if ((listeners != null) && (listeners.size() > 0)) {
			for (IMPEventListener listener : listeners) {
				threadPool.execute(new MPEventRunner(listener, event));
			}
		}
	}
}
