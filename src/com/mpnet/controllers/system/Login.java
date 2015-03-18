package com.mpnet.controllers.system;

import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.controllers.BaseControllerCommand;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.core.MPConstants;
import com.mpnet.exceptions.MPRequestValidationException;

/**
 * 
 * @ClassName: Login 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:51:28 
 *
 */
public class Login extends BaseControllerCommand{
	public static final String KEY_USERNAME="un";
	public static final String KEY_PASSWORD="pw";
	public static final String KEY_PARAMS="p";
	public static final String KEY_PRIVILEGE_ID="pi";
	public static final String KEY_ID="id";
	public static final String KEY_RECONNECTION_SECONDS="rs";

	public Login(){
		super(SystemRequest.Login);
	}

	public boolean validate(IRequest request) throws MPRequestValidationException{
		boolean res=true;
		IMPObject mpo=(IMPObject)request.getContent();
		if((!mpo.containsKey(KEY_USERNAME))||(!mpo.containsKey(KEY_PASSWORD))){
			throw new MPRequestValidationException("Bad Login Request. Essential parameters are missing. Client API is probably fake.");
		}
		return res;
	}

	public void execute(IRequest request) throws Exception{
		IMPObject reqObj=(IMPObject)request.getContent();
		String userName=reqObj.getUtfString(KEY_USERNAME);
		String password=reqObj.getUtfString(KEY_PASSWORD);
		IMPObject paramsOut=MPObject.newInstance();
		if(paramsOut!=null){
			String newUserName=paramsOut.getUtfString(MPConstants.NEW_LOGIN_NAME);
			if(newUserName!=null){
				userName=newUserName;
			}
		}
		api.login(request.getSender(),userName,password,reqObj.getMPObject(KEY_PARAMS),paramsOut,true);
	}
}