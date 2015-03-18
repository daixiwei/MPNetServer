package com.mpnet.test;

import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.entities.User;
import com.mpnet.extensions.BaseClientRequestHandler;
import com.mpnet.extensions.MPExtension;

/**
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月16日 下午5:32:23
 * @version V2.9
 */
public class TestHandler extends BaseClientRequestHandler {
	
	public TestHandler(MPExtension extendsion) {
		extendsion.addRequestHandler("test", this);
	}
	
	@Override
	public void handleClientRequest(User user, IMPObject params) {
		String cmd = params.getUtfString(MPExtension.REQUEST_ID);
		IMPObject data = new MPObject();
		if (cmd.equals("test")) {
			this.trace(params.getUtfString("s"));
			data.putUtfString("c", "hello client!");
			send("test", data, user);
		}
		
	}
	
}
