package com.mpnet.controllers.system;

import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.controllers.BaseControllerCommand;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPRequestValidationException;

/**
 * 
 * @ClassName: ManualDisconnection 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月28日 下午3:01:16 
 *
 */
public class ManualDisconnection extends BaseControllerCommand{
	public ManualDisconnection(){
		super(SystemRequest.ManualDisconnection);
	}

	public boolean validate(IRequest request) throws MPRequestValidationException{
		return true;
	}

	public void execute(IRequest request) throws Exception{
		User sender=this.api.getUserBySession(request.getSender());
		if((mpserver.getUserManager().getUserReconnectionSeconds()>0)){
			sender.setReconnectionSeconds(0);
		}
	}
}