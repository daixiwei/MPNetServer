package com.mpnet.core;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @ClassName: BaseCoreService 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:22:34 
 *
 */
public abstract class BaseCoreService implements ICoreService {
	private static final AtomicInteger serviceId = new AtomicInteger(0);
	private static final String DEFAULT_NAME = "AnonymousService-";
	protected String name;
	protected volatile boolean active = false;

	public void init(Object o) {
		this.name = getId();
		this.active = true;
	}

	public void destroy(Object o) {
		this.active = false;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void handleMessage(Object param) {
		throw new UnsupportedOperationException("This method should be overridden by the child class!");
	}

	public boolean isActive() {
		return this.active;
	}

	public String toString() {
		return "[Core Service]: " + this.name + ", State: " + (isActive() ? "active" : "not active");
	}

	protected static String getId() {
		return DEFAULT_NAME + serviceId.getAndIncrement();
	}
}