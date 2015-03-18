package com.mpnet.db;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mpnet.config.ServerSettings.DatabaseManagerSettings;
import com.mpnet.exceptions.MPRuntimeException;
import com.thoughtworks.xstream.XStream;

/**
 * 
 * @ClassName: MPDBManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月2日 下午4:08:44
 *
 */
public class MPDBManager implements IDBManager {
	private boolean							active	= false;
	private final DatabaseManagerSettings	config;
	private final Logger					log;
	private IDataMappManager				dataMappManager;
	
	public MPDBManager(DatabaseManagerSettings config) {
		this.config = config;
		log = LoggerFactory.getLogger(getClass());
	}
	
	@Override
	public void init(Object o) {
		if (config == null)
			throw new IllegalStateException("DBManager was not configured! ");
		if (!config.isActive) {
			return;
		}
		
		if (config.isMapping) {
			try {
				FileInputStream inStream = new FileInputStream(config.mappingFile);
				MappingConfig mappingConfig = (MappingConfig) getDataMappingXStreamDefinitions().fromXML(inStream);
				Connection connection = getConnection(true);
				dataMappManager = new MPDataMappManager(mappingConfig);
				dataMappManager.init(connection);
			} catch (Exception e) {
				throw new MPRuntimeException(e);
			}
		}
		active = true;
		log.info("Data base Manager active");
	}
	
	private XStream getDataMappingXStreamDefinitions() {
		XStream xstream = new XStream();
		xstream.alias("mappingConfig", MappingConfig.class);
		
		xstream.alias("table", MappingConfig.TableSetting.class);
		
		xstream.alias("findpk", MappingConfig.FindPKConfig.class);
		xstream.useAttributeFor(MappingConfig.FindPKConfig.class, "key");
		xstream.useAttributeFor(MappingConfig.FindPKConfig.class, "colIndex");
		return xstream;
	}
	
	@Override
	public void destroy(Object o) {
		if (dataMappManager != null) {
			dataMappManager.destroy(o);
		}
	}
	
	@Override
	public DatabaseManagerSettings getConfig() {
		return this.config;
	}
	
	@Override
	public IDataMappManager getDataMappManager() {
		return dataMappManager;
	}
	
	@Override
	public boolean isActive() {
		return this.active;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		return getConnection(false);
	}
	
	private final Connection getConnection(boolean isSkip) throws SQLException {
		if (!isSkip)
			checkState();
		try {
			Class.forName(config.driverName);
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}
		Connection conn = DriverManager.getConnection(config.connectionString, config.userName, config.password);
		return conn;
	}
	
	private void checkState() throws SQLException {
		if (!active)
			throw new SQLException("The DBManager is NOT active");
	}
}