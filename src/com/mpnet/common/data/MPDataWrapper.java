package com.mpnet.common.data;

/**
 * 
 * @ClassName: MPDataWrapper
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 下午5:58:48
 *
 */
public class MPDataWrapper {
	private MPDataType	typeId;
	private Object		object;
	
	public MPDataWrapper(MPDataType typeId, Object object) {
		this.typeId = typeId;
		this.object = object;
	}
	
	public MPDataType getTypeId() {
		return typeId;
	}
	
	public Object getObject() {
		return object;
	}
}
