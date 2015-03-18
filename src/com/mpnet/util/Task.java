package com.mpnet.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @ClassName: Task
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:17:08
 *
 */
public class Task {
	private Object				id;
	private Map<Object, Object>	parameters;
	private volatile boolean	active	= true;
	
	public Task() {
		this.parameters = new HashMap<Object, Object>();
	}
	
	public Task(Object id) {
		this();
		this.id = id;
	}
	
	public Task(Object id, Map<Object, Object> mapObj) {
		this.id = id;
		this.parameters = mapObj;
	}
	
	public Object getId() {
		return this.id;
	}
	
	public Map<Object, Object> getParameters() {
		return this.parameters;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
}