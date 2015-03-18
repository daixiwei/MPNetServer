package com.mpnet.controllers.system;

import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.bitswarm.io.IResponse;
import com.mpnet.bitswarm.io.Response;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.common.data.MPObject;
import com.mpnet.config.DefaultConstants;
import com.mpnet.controllers.BaseControllerCommand;
import com.mpnet.controllers.SystemRequest;
import com.mpnet.exceptions.MPRequestValidationException;

/**
 * 
 * @ClassName: PingPong 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月28日 下午3:01:54 
 *
 */
public class PingPong extends BaseControllerCommand{
	private static final String KEY_LAST_PING_TIME="key_lastPingTime";
	private static final int MIN_PING_TIME=900;

	public PingPong(){
		super(SystemRequest.PingPong);
	}

	public boolean validate(IRequest request) throws MPRequestValidationException{
		boolean isOk=true;
		ISession sender=request.getSender();
		Long lastPing=(Long)sender.getProperty(KEY_LAST_PING_TIME);
		long now=System.currentTimeMillis();
		if(lastPing!=null){
			if(now-lastPing.longValue()<MIN_PING_TIME){
				isOk=false;
			}
		}
		return isOk;
	}

	public void execute(IRequest request) throws Exception{
		request.getSender().setProperty(KEY_LAST_PING_TIME,System.currentTimeMillis());
		sendPingPongResponse(request.getSender());
	}
	
	private final void sendPingPongResponse(ISession recipient) {
		IResponse response = new Response();
		response.setId(SystemRequest.PingPong.getId());
		response.setTargetController(DefaultConstants.CORE_SYSTEM_CONTROLLER_ID);
		response.setContent(new MPObject());
		response.setRecipients(recipient);

		response.write();
	}
}