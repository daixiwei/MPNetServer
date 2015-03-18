package com.mpnet.bitswarm.core;

import com.mpnet.bitswarm.io.IResponse;
import com.mpnet.config.DefaultConstants;
import com.mpnet.util.ITaskHandler;
import com.mpnet.util.Task;

/**
 * 
 * @ClassName: EngineDelayedTaskHandler
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月4日 上午10:14:26
 *
 */
public final class EngineDelayedTaskHandler extends AbstractMethodDispatcher implements ITaskHandler {
	
	/**
	 * 
	 */
	public EngineDelayedTaskHandler() {
		registerTasks();
	}
	
	private void registerTasks() {
		registerMethod(DefaultConstants.TASK_DELAYED_SOCKET_WRITE, "onDelayedSocketWrite");
	}
	
	/**
	 * 
	 */
	public void doTask(Task task) throws Exception {
		try {
			callMethod((String) task.getId(), task);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param o
	 */
	public void onDelayedSocketWrite(Object o) {
		Task task = (Task) o;
		IResponse response = (IResponse) task.getParameters().get("response");
		
		if (response != null)
			response.write();
	}
}