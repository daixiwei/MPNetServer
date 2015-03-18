package com.mpnet.bitswarm.io;

import com.mpnet.bitswarm.core.BitSwarmEngine;
import com.mpnet.bitswarm.data.TransportType;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.config.DefaultConstants;
import com.mpnet.util.Scheduler;
import com.mpnet.util.Task;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @ClassName: Response
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月10日 上午11:28:06
 *
 */
public class Response extends AbstractEngineMessage implements IResponse {
	private Collection<ISession>	recipients;
	private TransportType			type;
	private Object					targetController;
	
	public Response() {
		type = TransportType.TCP;
	}
	
	public Collection<ISession> getRecipients() {
		return recipients;
	}
	
	public TransportType getTransportType() {
		return type;
	}
	
	public boolean isTCP() {
		return type == TransportType.TCP;
	}
	
	public boolean isUDP() {
		return type == TransportType.UDP;
	}
	
	public void setRecipients(Collection<ISession> recipents) {
		this.recipients = recipents;
	}
	
	public void setRecipients(ISession session) {
		List<ISession> recipients = new ArrayList<ISession>();
		recipients.add(session);
		setRecipients(recipients);
	}
	
	public void setTransportType(TransportType type) {
		this.type = type;
	}
	
	public void write() {
		BitSwarmEngine.getInstance().write(this);
	}
	
	public void write(int delay) {
		Scheduler scheduler = (Scheduler) BitSwarmEngine.getInstance().getServiceByName(DefaultConstants.SERVICE_SCHEDULER);
		Task delayedSocketWriteTask = new Task(DefaultConstants.TASK_DELAYED_SOCKET_WRITE);
		delayedSocketWriteTask.getParameters().put("response", this);
		scheduler.addScheduledTask(delayedSocketWriteTask, delay, false, BitSwarmEngine.getInstance().getEngineDelayedTaskHandler());
	}
	
	public Object getTargetController() {
		return targetController;
	}
	
	public void setTargetController(Object o) {
		targetController = o;
	}
	
	public static IResponse clone(IResponse original) {
		IResponse newResponse = new Response();
		newResponse.setContent(original.getContent());
		newResponse.setTargetController(original.getTargetController());
		newResponse.setId(original.getId());
		newResponse.setTransportType(original.getTransportType());
		return newResponse;
	}
}