package com.mpnet.util;

import com.mpnet.MPNetServer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: CCULoggerTask
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月26日 上午11:00:01
 *
 */
public class CCULoggerTask implements Runnable {
	private static final int	TIME_TO_LOG		= 55;
	private final MPNetServer	mpnet;
	private final Logger		log;
	private final StatsData		serverStats;
	private int					lastLoggedHour	= -1;
	
	public CCULoggerTask() {
		this.mpnet = MPNetServer.getInstance();
		this.log = LoggerFactory.getLogger(getClass());
		this.serverStats = new StatsData();
	}
	
	public void run() {
		int globalCCU = mpnet.getUserManager().getUserCount();
		serverStats.hourAvg += globalCCU;
		serverStats.peakCCU = (serverStats.peakCCU < globalCCU ? globalCCU : serverStats.peakCCU);
		
		Calendar cal = new GregorianCalendar();
		int min = cal.get(Calendar.MINUTE);
		
		if (min >= TIME_TO_LOG) {
			int currHour = cal.get(Calendar.HOUR);
			
			if (lastLoggedHour != currHour) {
				lastLoggedHour = currHour;
				logStats();
				
				serverStats.hourAvg = 0;
				serverStats.peakCCU = 0;
			}
		}
	}
	
	private void logStats() {
		log.info(String.format("CCU stats: CCU: %s/%s", Integer.valueOf(serverStats.hourAvg / 60), Integer.valueOf(serverStats.peakCCU)));
	}
	
	private static final class StatsData {
		int	hourAvg;
		int	peakCCU;
	}
}