package com.mpnet.bitswarm.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: SimpleController
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午4:22:52
 *
 */
public abstract class SimpleController implements IController {
	protected Object			id;
	protected String			name;
	protected volatile boolean	isActive	= false;
	protected final Logger		logger;
	
	public SimpleController() {
		logger = LoggerFactory.getLogger(getClass());
	}
	
	public void init(Object o) {
		if (isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		}
		isActive = true;
		
		logger.info(String.format("Controller started: %s ", getClass().getName()));
	}
	
	public void destroy(Object o) {
		isActive = false;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if (this.name != null) {
			throw new IllegalStateException("Controller already has a name: " + this.name);
		}
		this.name = name;
	}
	
	public Object getId() {
		return this.id;
	}
	
	public void setId(Object id) {
		if (this.id != null) {
			throw new IllegalStateException("Controller already has an id: " + this.id);
		}
		this.id = id;
	}
}
