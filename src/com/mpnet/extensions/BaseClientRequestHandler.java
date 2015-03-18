package com.mpnet.extensions;

import com.mpnet.api.IMPApi;
import com.mpnet.common.data.IMPObject;
import com.mpnet.entities.User;
import java.util.List;

/**
 * 
 * @ClassName: BaseClientRequestHandler 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:19:46 
 *
 */
public abstract class BaseClientRequestHandler implements IClientRequestHandler {
	private MPExtension parentExtension;

	public MPExtension getParentExtension() {
		return this.parentExtension;
	}

	public void setParentExtension(MPExtension ext) {
		parentExtension = ext;
	}

	protected IMPApi getApi() {
		return this.parentExtension.mpApi;
	}

	protected void send(String cmdName, IMPObject params, User recipient) {
		parentExtension.send(cmdName, params, recipient);
	}

	protected void send(String cmdName, IMPObject params, List<User> recipients) {
		parentExtension.send(cmdName, params, recipients);
	}

	protected void send(String cmdName, IMPObject params, User recipient, boolean useUDP) {
		parentExtension.send(cmdName, params, recipient, useUDP);
	}

	protected void send(String cmdName, IMPObject params, List<User> recipients, boolean useUDP) {
		parentExtension.send(cmdName, params, recipients, useUDP);
	}

	protected void trace(Object... args) {
		parentExtension.trace(args);
	}

	protected void trace(ExtensionLogLevel level, Object... args) {
		parentExtension.trace(level, args);
	}
}