package com.mpnet.api;

import com.mpnet.bitswarm.io.IResponse;
import com.mpnet.bitswarm.io.Response;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.config.DefaultConstants;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.controllers.system.Login;
import com.mpnet.exceptions.MPErrorCode;
import com.mpnet.exceptions.MPErrorData;
import com.mpnet.exceptions.MPLoginException;

/**
 * 
 * @ClassName: LoginErrorHandler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:05:52
 *
 */
public final class LoginErrorHandler {
	
	/**
	 * 
	 * @param sender
	 * @param err
	 */
	public void execute(ISession sender, MPLoginException err) {
		IMPObject resObj = MPObject.newInstance();
		if (err.getErrorData() == null) {
			MPErrorData errData = new MPErrorData(MPErrorCode.GENERIC_ERROR);
			err = new MPLoginException(err.getMessage(), errData);
		}
		IResponse response = new Response();
		response.setId(SystemRequest.Login.getId());
		response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
		response.setContent(resObj);
		response.setRecipients(sender);
		
		resObj.putShort(Login.KEY_ERROR_CODE, err.getErrorData().getCode().getId());
		response.write();
	}
}