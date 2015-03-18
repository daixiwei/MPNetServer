package com.mpnet.controllers;

//import java.util.Arrays;
//import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpnet.MPNetServer;
import com.mpnet.api.IMPApi;
import com.mpnet.bitswarm.io.IRequest;

/**
 * 
 * @ClassName: BaseControllerCommand 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月7日 下午3:04:19 
 *
 */
public abstract class BaseControllerCommand implements IControllerCommand{
	public static final String KEY_ERROR_CODE="ec";
	public static final String KEY_ERROR_PARAMS="ep";
	protected final Logger logger;
	protected final MPNetServer mpserver;
	protected final IMPApi api;
	private short id;
	private final SystemRequest requestType;

	public BaseControllerCommand(SystemRequest request){
		this.logger=LoggerFactory.getLogger(getClass());
		this.mpserver=MPNetServer.getInstance();
		this.api=this.mpserver.getAPIManager().getApi();
		this.id=((Short)request.getId()).shortValue();
		this.requestType=request;
	}

	public Object preProcess(IRequest request) throws Exception{
		return null;
	}

	public short getId(){
		return this.id;
	}

	public SystemRequest getRequestType(){
		return this.requestType;
	}

}