package com.mpnet.util;

import com.mpnet.controllers.SystemRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @ClassName: RequestMonitor
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午5:32:41
 *
 */
public class RequestMonitor {
	private final Map<SystemRequest, RequestRateMeter>	rateMeters;
	
	public RequestMonitor() {
		this.rateMeters = new ConcurrentHashMap<SystemRequest, RequestRateMeter>();
	}
	
	public int updateRequest(SystemRequest request) {
		RequestRateMeter meter = (RequestRateMeter) this.rateMeters.get(request);
		
		if (meter != null) {
			return meter.updateAndCheck();
		}
		
		this.rateMeters.put(request, new RequestRateMeter());
		return 1;
	}
	
	private static final class RequestRateMeter {
		private static final int	DEFAULT_SECONDS	= 1;
		private int					rateMonitorMillis;
		private long				lastUpdate;
		private int					reqCounter;
		
		public RequestRateMeter() {
			this(1);
		}
		
		public RequestRateMeter(int secondsMonitored) {
			this.rateMonitorMillis = (secondsMonitored * 1000);
			this.lastUpdate = System.currentTimeMillis();
			this.reqCounter = 0;
		}
		
		public synchronized int updateAndCheck() {
			long now = System.currentTimeMillis();
			
			if (now - this.lastUpdate > this.rateMonitorMillis) {
				this.reqCounter = 0;
			}
			this.reqCounter += DEFAULT_SECONDS;
			this.lastUpdate = now;
			
			return this.reqCounter;
		}
	}
}