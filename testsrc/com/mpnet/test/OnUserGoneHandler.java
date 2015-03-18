package com.mpnet.test;

import com.mpnet.core.IMPEvent;
import com.mpnet.core.MPEventParam;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPException;
import com.mpnet.extensions.BaseServerEventHandler;

/**
 * 
 * @ClassName: OnUserGoneHandler 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月14日 下午5:39:51 
 *
 */
public class OnUserGoneHandler extends BaseServerEventHandler {

	@Override
	public void handleServerEvent(IMPEvent event) throws MPException {
		User user = (User) event.getParameter(MPEventParam.USER);
		this.trace(user.getName() + "离开!");
	}

}
