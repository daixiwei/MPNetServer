package com.mpnet.core;

/**
 * 
 * @ClassName: BaseMPEventListener 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月18日 上午11:48:45 
 *
 */
public class BaseMPEventListener {
	private Object parentObject;

	public BaseMPEventListener() {
		this.parentObject = null;
	}

	public BaseMPEventListener(Object parentObject) {
		this.parentObject = parentObject;
	}

	public Object getParentObject() {
		return this.parentObject;
	}

	public void handleServerEvent(IMPEvent event) {}

	public String toString() {
		return this.parentObject == null ? "{ Anonymous listener }" : this.parentObject.toString();
	}
}