package com.mpnet.entities.managers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mpnet.MPNetServer;
import com.mpnet.api.LoginErrorHandler;
import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.bitswarm.service.ISimpleService;
import com.mpnet.bitswarm.sessions.ISession;
import com.mpnet.config.ServerSettings;
import com.mpnet.controllers.IControllerCommand;
import com.mpnet.core.IMPEvent;
import com.mpnet.core.IMPEventListener;
import com.mpnet.core.IMPEventManager;
import com.mpnet.core.MPEventSysParam;
import com.mpnet.core.MPEventType;
import com.mpnet.core.MPSystemEvent;
import com.mpnet.exceptions.ExceptionMessageComposer;
import com.mpnet.exceptions.MPExtensionException;
import com.mpnet.exceptions.MPLoginException;
import com.mpnet.exceptions.MPRuntimeException;
import com.mpnet.extensions.IMPExtension;


/**
 * 
 * @ClassName: MPExtensionManager 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午5:51:11 
 *
 */
public final class MPExtensionManager implements IExtensionManager, IMPEventListener {
	private IMPExtension extension;
	private final Map<MPEventType, Set<IMPEventListener>> listenersByEvent;
	private final Logger logger;
	private MPNetServer mpnet;
	private final LoginErrorHandler loginErrorHandler;
	private IMPEventManager eventManager;
	private final Map<String,ISimpleService> services;
	
	public MPExtensionManager() {
		logger = LoggerFactory.getLogger(getClass());
		
		services = new ConcurrentHashMap<String, ISimpleService>();
		listenersByEvent = new ConcurrentHashMap<MPEventType, Set<IMPEventListener>>();
		loginErrorHandler = new LoginErrorHandler();
	}

	public IMPExtension getExtension() {
		return extension;
	}

	private void createExtension(ServerSettings.ExtensionSettings settings) throws MPExtensionException {
		if ((settings.className == null) || (settings.className.length() == 0)) {
			throw new MPExtensionException("Extension file parameter is missing!");
		}
		if ((settings.name == null) || (settings.name.length() == 0)) {
			throw new MPExtensionException("Extension name parameter is missing!");
		}
		IMPExtension extension = createJavaExtension(settings);
		extension.setName(settings.name);
		extension.setExtensionClassName(settings.className);
		try {
			extension.init();
			synchronized (services) {
				Iterator<ISimpleService> iterator = services.values().iterator();
				for (; iterator.hasNext();) {
					ISimpleService service = iterator.next();
					service.init(extension);
				}
			}
			this.extension = extension;
		} catch (Exception err) {
			ExceptionMessageComposer msg = new ExceptionMessageComposer(err);
			msg.setDescription("Extension initialization failed.");
			this.logger.error(msg.toString());
		}
	}

	private IMPExtension createJavaExtension(ServerSettings.ExtensionSettings settings) throws MPExtensionException {
		try {
			Class<?> extensionClass = Class.forName(settings.className);
			if (!IMPExtension.class.isAssignableFrom(extensionClass)) {
				throw new MPExtensionException("Extension does not implement IMPExtension interface: " + settings.name);
			}
			IMPExtension extension = (IMPExtension) extensionClass.newInstance();
			return extension;
		} catch (IllegalAccessException e) {
			throw new MPExtensionException("Illegal access while instantiating class: " + settings.className);
		} catch (InstantiationException e) {
			throw new MPExtensionException("Cannot instantiate class: " + settings.className);
		} catch (ClassNotFoundException e) {
			throw new MPExtensionException("Class not found: " + settings.className);
		}
	}

	public void init() {
		mpnet = MPNetServer.getInstance();
		eventManager = mpnet.getEventManager();
		for (MPEventType type : MPEventType.values()) {
			eventManager.addEventListener(type, this);
		}
		
		ServerSettings.ExtensionSettings settings =mpnet.getConfigurator().getServerSettings().extensionSettings;
		try {
			createExtension(settings);
		} catch (MPExtensionException err) {
			String extName = settings.name == null ? "{Unknown}" : settings.name;
			throw new MPRuntimeException("Extension creation failure: " + extName + " - " + err.getMessage());
		}
		logger.debug("Extension Manager started.");
	}

	public void destroy() {
		for (MPEventType type : MPEventType.values()) {
			eventManager.removeEventListener(type, this);
		}
		listenersByEvent.clear();
		synchronized (services) {
			Iterator<ISimpleService> iterator = services.values().iterator();
			for (; iterator.hasNext();) {
				ISimpleService service = iterator.next();
				service.destroy(extension);
			}
			services.clear();
		}
		extension.destroy();
		extension = null;
		logger.debug("Extension Manager stopped.");
	}

	public void reloadExtension() {
		logger.info("Reloading extension: " + extension);
		ServerSettings.ExtensionSettings newSettings = new ServerSettings.ExtensionSettings();
		newSettings.className = extension.getExtensionClassName();
		newSettings.name = extension.getName();
		try {
			createExtension(newSettings);
			extension.destroy();
		} catch (Throwable t) {
			ExceptionMessageComposer composer = new ExceptionMessageComposer(t);
			composer.setDescription("An error occurred while reloading extension: " + extension.getName());
			composer.addInfo("The new extension might not function properly.");
			logger.error(composer.toString());
		}
	}

	public synchronized void addEventListener(MPEventType type, IMPEventListener listener) {
		Set<IMPEventListener> listeners = listenersByEvent.get(type);
		if (listeners == null) {
			listeners = new CopyOnWriteArraySet<IMPEventListener>();
			listenersByEvent.put(type, listeners);
		}
		listeners.add(listener);
	}

	public void dispatchEvent(IMPEvent event) {
		List<IMPEventListener> allListeners = new ArrayList<IMPEventListener>();
		MPEventType type = event.getType();
		Set<IMPEventListener> listeners = listenersByEvent.get(type);
		if (listeners != null) {
			allListeners.addAll(listeners);
		}
		dispatchEvent(event, allListeners);
	}

	private void dispatchEvent(IMPEvent event, Collection<IMPEventListener> listeners) {
		if ((listeners != null) && (listeners.size() > 0)) {
			for (IMPEventListener listener : listeners) {
				try {
					listener.handleServerEvent(event);
					if (!(event instanceof MPSystemEvent))
						continue;
					executeEventCommand((MPSystemEvent) event);
				} catch (MPLoginException logErr) {
					handleLoginException((MPSystemEvent) event, logErr);
				} catch (Exception e) {
					ExceptionMessageComposer composer = new ExceptionMessageComposer(e);
					composer.setDescription("Error during event handling: " + e + ", Listener: " + listener);
					logger.warn(composer.toString());
				}
			}
		}
	}

	public void removeListener(IMPEventListener listener) {
		if (listenersByEvent != null) {
			for (Set<IMPEventListener> listeners : listenersByEvent.values()) {
				listeners.remove(listener);
			}
		}
	}

	public void removeEventListener(MPEventType type, IMPEventListener listener) {
		removeEventListener(listenersByEvent, type, listener);
	}

	private void removeEventListener(Map<MPEventType, Set<IMPEventListener>> listenersByType, MPEventType type, IMPEventListener listener) {
		if (listenersByType != null) {
			Set<IMPEventListener> listeners = listenersByType.get(type);
			if (listeners != null)
				listeners.remove(listener);
		}
	}

	public void handleServerEvent(IMPEvent event) {
		dispatchEvent(event);
	}

	private void executeEventCommand(MPSystemEvent sysEvent) throws Exception {
		Class<?> commandClass = (Class<?>) sysEvent.getSysParameter(MPEventSysParam.NEXT_COMMAND);
		IRequest request = (IRequest) sysEvent.getSysParameter(MPEventSysParam.REQUEST_OBJ);
		if ((commandClass != null) && (request != null)) {
			IControllerCommand command = (IControllerCommand) commandClass.newInstance();
			command.execute(request);
		}
	}

	private void handleLoginException(MPSystemEvent event, MPLoginException err) {
		logger.warn(err.toString());
		ISession sender = ((IRequest) event.getSysParameter(MPEventSysParam.REQUEST_OBJ)).getSender();
		loginErrorHandler.execute(sender, err);
	}

	@Override
	public void addService(String key,ISimpleService service){
		synchronized (services) {
			services.put(key, service);
		}
	}
	
	@Override
	public void removeService(String key,ISimpleService service){
		synchronized (services) {
			services.remove(key);
		}
	}

	@Override
	public ISimpleService getService(String key) {
		return services.get(key);
	}

}