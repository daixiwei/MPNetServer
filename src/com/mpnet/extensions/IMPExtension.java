package com.mpnet.extensions;

import com.mpnet.common.data.IMPObject;
import com.mpnet.core.IMPEventListener;
import com.mpnet.core.MPEventType;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPException;
import java.util.List;

/**
 * 
 * @ClassName: IMPExtension 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:29:51 
 *
 */
public interface IMPExtension {
	public void init();

	public void destroy();

	public String getName();

	public void setName(String name);

	public String getExtensionClassName();

	public void setExtensionClassName(String className);
	
	public void setLoginHandler(ILoginHandler handler);

	public ILoginHandler getLoginHandler();
	
	public boolean isActive();

	public void setActive(boolean paramBoolean);

	public void addEventListener(MPEventType eventType, IMPEventListener listener);

	public void removeEventListener(MPEventType eventType, IMPEventListener listener);

	public void handleClientRequest(String cmdName, User user, IMPObject object) throws MPException;

	public Object handleInternalMessage(String cmdName, Object params);

	public void send(String cmdName, IMPObject params, User recipient, boolean useUDP);

	public void send(String cmdName, IMPObject params, User recipient);

	public void send(String cmdName, IMPObject params, List<User> recipients, boolean useUDP);

	public void send(String cmdName, IMPObject params, List<User> recipients);
}