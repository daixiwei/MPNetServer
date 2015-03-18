package com.mpnet.extensions.filter;

import com.mpnet.common.data.IMPObject;
import com.mpnet.core.IMPEvent;
import com.mpnet.entities.User;
import com.mpnet.exceptions.MPException;
import com.mpnet.exceptions.MPRuntimeException;
import com.mpnet.extensions.MPExtension;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @ClassName: MPExtensionFilterChain
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:30:59
 *
 */
public class MPExtensionFilterChain implements IFilterChain {
	private final Collection<MPExtensionFilter> filters;
	private final MPExtension parentExtension;
	private final Logger log;

	public MPExtensionFilterChain(MPExtension parentExtension) {
		this.parentExtension = parentExtension;
		this.log = LoggerFactory.getLogger(getClass());

		this.filters = new ConcurrentLinkedQueue<MPExtensionFilter>();
	}

	public void addFilter(String filterName, MPExtensionFilter filter) {
		if (this.filters.contains(filter)) {
			throw new MPRuntimeException("A filter with the same name already exists: " + filterName + ", Ext: " + this.parentExtension);
		}

		filter.setName(filterName);

		filter.init(this.parentExtension);

		this.filters.add(filter);
	}

	public void remove(String filterName) {
		for (Iterator<MPExtensionFilter> it = this.filters.iterator(); it.hasNext();) {
			MPExtensionFilter filter = (MPExtensionFilter) it.next();
			if (!filter.getName().equals(filterName))
				continue;
			it.remove();
			break;
		}
	}

	public FilterAction runEventInChain(IMPEvent event) throws MPException {
		FilterAction filterAction = FilterAction.CONTINUE;

		for (MPExtensionFilter filter : this.filters) {
			try {
				filterAction = filter.handleServerEvent(event);

				if (filterAction != FilterAction.HALT) {
					continue;
				}
			} catch (MPException mpEx) {
				throw mpEx;
			} catch (Exception e) {
				this.log.warn(String.format("Exception in FilterChain execution:%s --- Filter: %s, Event: %s, Ext: %s", new Object[] { e.toString(), filter.getName(), event, this.parentExtension }));
			}

		}

		return filterAction;
	}

	public FilterAction runRequestInChain(String requestId, User sender, IMPObject params) {
		FilterAction filterAction = FilterAction.CONTINUE;

		for (MPExtensionFilter filter : this.filters) {
			try {
				filterAction = filter.handleClientRequest(requestId, sender, params);

				if (filterAction != FilterAction.HALT)
					continue;
			} catch (Exception e) {
				this.log.warn(String.format("Exception in FilterChain execution:%s --- Filter: %s, Req: %s, Ext: %s", new Object[] { e.toString(), filter.getName(), requestId, this.parentExtension }));
			}

		}

		return filterAction;
	}

	public int size() {
		return this.filters.size();
	}

	public void destroy() {
		for (MPExtensionFilter filter : this.filters) {
			filter.destroy();
		}

		this.filters.clear();
	}
}