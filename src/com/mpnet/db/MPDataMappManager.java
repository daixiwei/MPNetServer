package com.mpnet.db;

import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.MPNetServer;
import com.mpnet.db.MappingConfig.TableSetting;
import com.mpnet.exceptions.MPRuntimeException;

/**
 * 
 * @ClassName: MPDataMappManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月3日 上午11:08:16
 *
 */
public class MPDataMappManager implements IDataMappManager {
	private boolean							active		= false;
	private final Map<String, TableMapp>	tabelsMapping;
	private Connection						conn;
	private Object							lockObject	= new Object();
	private final Logger					log;
	private final MappingConfig				config;
	
	public MPDataMappManager(MappingConfig config) {
		this.config = config;
		tabelsMapping = new ConcurrentHashMap<String, TableMapp>();
		log = LoggerFactory.getLogger(getClass());
	}
	
	public void init(Object o) {
		MPNetServer mpnet = MPNetServer.getInstance();
		conn = (Connection) o;
		List<TableSetting> tables = config.tables;
		try {
			for (TableSetting table : tables) {
				TableMapp tm = new TableMapp(this, table);
				tm.initTable(conn);
				addTableMapp(tm);
			}
		} catch (Exception e) {
			throw new MPRuntimeException(e);
		}
		active = true;
		if (config.autoCommit)
			mpnet.getTaskScheduler().scheduleAtFixedRate(new AutoCommitRunneble(), config.initialDelayTime, config.intervalTime, TimeUnit.SECONDS);
		log.info("Data mapping Manager active");
	}
	
	public void destroy(Object o) {
		try {
			enforceCommit();
			conn.close();
		} catch (Exception ex) {
			log.error(ex.toString());
		}
		log.info("Data mapping Manager destroy");
	}
	
	/**
	 * 添加表格数据映射对象
	 * 
	 * @param tableMapp
	 */
	private final void addTableMapp(TableMapp tableMapp) {
		tabelsMapping.put(tableMapp.getName(), tableMapp);
	}
	
	@Override
	public TableMapp getTableMapp(String table) {
		checkState();
		return tabelsMapping.get(table);
	}
	
	@Override
	public void enforceCommit() throws Exception {
		checkState();
		synchronized (lockObject) {
			conn.setAutoCommit(false);
			Collection<TableMapp> datas = tabelsMapping.values();
			for (Iterator<TableMapp> it = datas.iterator(); it.hasNext();) {
				TableMapp table = it.next();
				table.commit(conn);
			}
			conn.setAutoCommit(true);
		}
	}
	
	/**
	 * 提交映射表数据到数据库
	 * 
	 * @throws Exception
	 */
	void commit() throws Exception {
		checkState();
		synchronized (lockObject) {
			conn.setAutoCommit(false);
			Collection<TableMapp> datas = tabelsMapping.values();
			for (Iterator<TableMapp> it = datas.iterator(); it.hasNext();) {
				TableMapp table = it.next();
				table.commit(conn);
			}
			conn.setAutoCommit(true);
		}
	}
	
	private void checkState() throws MPRuntimeException {
		if (!active)
			throw new MPRuntimeException("The DataMappManager is NOT active");
	}
	
	/**
	 *
	 */
	private final class AutoCommitRunneble implements Runnable {
		@Override
		public void run() {
			try {
				commit();
			} catch (Exception e) {
				log.error(e.toString());
			}
		}
	}
}
