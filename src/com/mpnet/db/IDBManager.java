package com.mpnet.db;

import com.mpnet.bitswarm.service.ISimpleService;
import com.mpnet.config.ServerSettings.DatabaseManagerSettings;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @ClassName: IDBManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月2日 下午4:08:18
 *
 */
public interface IDBManager extends ISimpleService {
	
	/**
	 * Get DBManager is active
	 * 
	 * @return
	 */
	public boolean isActive();
	
	/**
	 * Get DBManager config
	 * 
	 * @return
	 */
	public DatabaseManagerSettings getConfig();
	
	/**
	 * Get Data Mapping Manager
	 * 
	 * @return
	 */
	public IDataMappManager getDataMappManager();
	
	/**
	 * Get connection object
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection() throws SQLException;
	
}