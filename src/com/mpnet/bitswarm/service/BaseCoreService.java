package com.mpnet.bitswarm.service;

import com.mpnet.bitswarm.events.IEvent;
import com.mpnet.bitswarm.events.IEventDispatcher;
import com.mpnet.bitswarm.events.IEventListener;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 
 * @ClassName: BaseCoreService
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:25:20
 *
 */
public abstract class BaseCoreService implements IService, IEventDispatcher {
	private String								serviceName;
	private Map<String, Set<IEventListener>>	listenersByEvent;
	
	public BaseCoreService() {
		listenersByEvent = new ConcurrentHashMap<String, Set<IEventListener>>();
	}
	
	public void init(Object o) {}
	
	public void destroy(Object o) {
		listenersByEvent.clear();
	}
	
	public String getName() {
		return serviceName;
	}
	
	public void setName(String name) {
		serviceName = name;
	}
	
	public void handleMessage(Object message) {}
	
	public synchronized void addEventListener(String eventType, IEventListener listener) {
		Set<IEventListener> listeners = listenersByEvent.get(eventType);
		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<IEventListener>();
			listenersByEvent.put(eventType, listeners);
		}
		
		listeners.add(listener);
	}
	
	public boolean hasEventListener(String eventType) {
		boolean found = false;
		Set<IEventListener> listeners = listenersByEvent.get(eventType);
		if ((listeners != null) && (listeners.size() > 0)) {
			found = true;
		}
		return found;
	}
	
	public void removeEventListener(String eventType, IEventListener listener) {
		Set<IEventListener> listeners = listenersByEvent.get(eventType);
		if (listeners != null)
			listeners.remove(listener);
	}
	
	public void dispatchEvent(IEvent event) {
		Set<IEventListener> listeners = (Set<IEventListener>) listenersByEvent.get(event.getName());
		if ((listeners != null) && (listeners.size() > 0)) {
			for (IEventListener listenerObj : listeners) {
				listenerObj.handleEvent(event);
			}
		}
	}
}