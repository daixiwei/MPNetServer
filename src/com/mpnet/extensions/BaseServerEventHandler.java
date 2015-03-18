package com.mpnet.extensions;

import com.mpnet.api.IMPApi;
import com.mpnet.common.data.IMPObject;
import com.mpnet.entities.User;
import java.util.List;


/**
 * 
 * @ClassName: BaseServerEventHandler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:19:41
 *
 */
public abstract class BaseServerEventHandler implements IServerEventHandler {
	private MPExtension parentExtension;

	public MPExtension getParentExtension() {
		return this.parentExtension;
	}

	public void setParentExtension(MPExtension ext) {
		this.parentExtension = ext;
	}

	protected IMPApi getApi() {
		return this.parentExtension.mpApi;
	}

	protected void send(String cmdName, IMPObject params, User recipient) {
		this.parentExtension.send(cmdName, params, recipient);
	}

	protected void send(String cmdName, IMPObject params, List<User> recipients) {
		this.parentExtension.send(cmdName, params, recipients);
	}

	protected void send(String cmdName, IMPObject params, User recipient, boolean useUDP) {
		this.parentExtension.send(cmdName, params, recipient, useUDP);
	}

	protected void send(String cmdName, IMPObject params, List<User> recipients, boolean useUDP) {
		this.parentExtension.send(cmdName, params, recipients, useUDP);
	}

	protected void trace(Object... args) {
		this.parentExtension.trace(args);
	}

	protected void trace(ExtensionLogLevel level, Object... args) {
		this.parentExtension.trace(level, args);
	}
}