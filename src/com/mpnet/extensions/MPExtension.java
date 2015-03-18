package com.mpnet.extensions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.mpnet.common.data.IMPObject;
import com.mpnet.core.IMPEvent;
import com.mpnet.core.MPEventType;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPRuntimeException;
import com.mpnet.extensions.filter.FilterAction;
import com.mpnet.extensions.filter.IFilterChain;
import com.mpnet.extensions.filter.MPExtensionFilter;
import com.mpnet.extensions.filter.MPExtensionFilterChain;

/**
 * 
 * @ClassName: MPExtension 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:29:44 
 *
 */
public abstract class MPExtension extends BaseMPExtension {
	public static final String REQUEST_ID = "__[[REQUEST_ID]]__";
	private final IFilterChain filterChain;
	private final Map<String, Class<?>> handlers;
	private final Map<String, Object> cachedHandlers;
	
	
	public MPExtension() {
		handlers = new ConcurrentHashMap<String, Class<?>>();
		cachedHandlers = new ConcurrentHashMap<String, Object>();
		filterChain = new MPExtensionFilterChain(this);
	}

	public void destroy() {
		clearAllHandlers();
		filterChain.destroy();
		removeEventsForListener(this);
	}

	public void addRequestHandler(String requestId, IClientRequestHandler requestHandler) {
		setHandlerParentExtension(requestHandler);
		cachedHandlers.put(requestId, requestHandler);
	}


	protected void addEventHandler(MPEventType eventType, IServerEventHandler handler) {
		addEventListener(eventType, this);
		addHandler(eventType.toString(), handler);
	}

	private void addHandler(String handlerKey, Object requestHandler) {
		setHandlerParentExtension(requestHandler);
		cachedHandlers.put(handlerKey, requestHandler);
	}
	
	protected void removeRequestHandler(String requestId) {
		handlers.remove(requestId);
		if (cachedHandlers.containsKey(requestId))
			cachedHandlers.remove(requestId);
	}

	protected void removeEventHandler(MPEventType eventType) {
		removeEventListener(eventType, this);
		removeRequestHandler(eventType.toString());
	}

	protected void clearAllHandlers() {
		handlers.clear();
		cachedHandlers.clear();
	}
	
	public void handleClientRequest(String requestId, User sender, IMPObject params) {
		if (filterChain.size() > 0) {
			if (filterChain.runRequestInChain(requestId, sender, params) == FilterAction.HALT) {
				return;
			}
		}
		try {
			IClientRequestHandler handler = (IClientRequestHandler) getHandlerInstance(requestId);

			if (handler == null) {
				throw new MPRuntimeException("Request handler not found: '" + requestId + "'. Make sure the handler is registered in your extension using addRequestHandler()");
			}
			params.putUtfString(REQUEST_ID, requestId);
			handler.handleClientRequest(sender, params);
		} catch (InstantiationException err) {
			trace(ExtensionLogLevel.WARN, "Cannot instantiate handler class: ", err);
		} catch (IllegalAccessException err) {
			trace(ExtensionLogLevel.WARN, "Illegal access for handler class: ", err);
		}
	}

	public void handleServerEvent(IMPEvent event) throws Exception {
		String handlerId = event.getType().toString();

		if (filterChain.size() > 0) {
			if (filterChain.runEventInChain(event) == FilterAction.HALT) {
				return;
			}
		}
		try {
			IServerEventHandler handler = (IServerEventHandler) getHandlerInstance(handlerId);

			if (handler == null) {
				throw new MPRuntimeException("Event handler not found: '" + handlerId + "'. Make sure the handler is registered in your extension using addEventHandler()");
			}

			handler.handleServerEvent(event);
		} catch (InstantiationException err) {
			trace(ExtensionLogLevel.WARN, "Cannot instantiate handler class: ", err);
		} catch (IllegalAccessException err) {
			trace(ExtensionLogLevel.WARN, "Illegal access for handler class: ", err);
		}
	}

	private Object getHandlerInstance(String key) throws InstantiationException, IllegalAccessException {
		Object handler = cachedHandlers.get(key);
		return handler;
	}

	private void setHandlerParentExtension(Object handler) {
		if ((handler instanceof IClientRequestHandler)) {
			((IClientRequestHandler) handler).setParentExtension(this);
		} else if ((handler instanceof IServerEventHandler))
			((IServerEventHandler) handler).setParentExtension(this);
	}
	
	public final void addFilter(String filterName, MPExtensionFilter filter) {
		filterChain.addFilter(filterName, filter);
	}

	public void removeFilter(String filterName) {
		filterChain.remove(filterName);
	}

	public void clearFilters() {
		filterChain.destroy();
	}
}