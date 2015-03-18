package com.mpnet.test;

import com.mpnet.core.MPEventType;
import com.mpnet.extensions.MPExtension;

/**
 * 
 * @ClassName: TestExtension 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月14日 下午5:45:33 
 *
 */
public class TestExtension extends MPExtension {
	
	@Override
	public void init() {
		
		//设置登陆处理器
		this.setLoginHandler(new UserLoginHandler());
		
		new TestHandler(this);
		
		OnUserGoneHandler og = new OnUserGoneHandler();
		addEventHandler(MPEventType.USER_DISCONNECT, og);
		addEventHandler(MPEventType.USER_LOGOUT, og);
		
	}
	
	public void destroy() {
		super.destroy();
	}
	
}
