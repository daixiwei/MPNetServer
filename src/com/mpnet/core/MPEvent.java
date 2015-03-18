package com.mpnet.core;

import java.util.Map;

/**
 * 
 * @ClassName: MPEvent 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:21:55 
 *
 */
public class MPEvent implements IMPEvent{
	private final MPEventType type;
	private final Map<IMPEventParam,Object> params;

	public MPEvent(MPEventType type){
		this(type,null);
	}

	public MPEvent(MPEventType type,Map<IMPEventParam,Object> params){
		this.type=type;
		this.params=params;
	}

	public MPEventType getType(){
		return this.type;
	}

	public Object getParameter(IMPEventParam id){
		Object param=null;
		if(this.params!=null){
			param=this.params.get(id);
		}
		return param;
	}

	public String toString(){
		return String.format("{ %s, Params: %s }",new Object[]{this.type,this.params!=null?this.params.keySet():"none"});
	}
}