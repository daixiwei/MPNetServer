package com.mpnet.controllers.system;

import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.controllers.BaseControllerCommand;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPRequestValidationException;

/**
 * 
 * @ClassName: Logout 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月28日 下午3:01:11 
 *
 */
public class Logout extends BaseControllerCommand{
	public static final String KEY_ZONE_NAME="zn";

	public Logout(){
		super(SystemRequest.Logout);
	}

	public boolean validate(IRequest request) throws MPRequestValidationException{
		return true;
	}

	public void execute(IRequest request) throws Exception{
		User sender=api.getUserBySession(request.getSender());
		if(sender==null){
			throw new IllegalArgumentException("Logout failure. Session is not logged in: "+request.getSender());
		}
		api.logout(sender);
	}
}