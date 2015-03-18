package com.mpnet.db;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.mpnet.common.data.IMPObject;
import com.mpnet.common.data.MPObject;
import com.mpnet.db.MappingConfig.FindPKConfig;
import com.mpnet.db.MappingConfig.TableSetting;
import com.mpnet.exceptions.MPRuntimeException;

/**
 * 
 * @ClassName: TableMapp
 * @Description: The table mapping class.
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月3日 上午11:23:08
 *
 */
public class TableMapp implements ITableMapp {
	
	/** The table name */
	private final String							tableName;
	/** The table datas */
	private final List<IRowMapp>					datas;
	/** The table datas by id */
	private final Map<Integer, IRowMapp>			datasById;
	/** The table datas by primary key */
	private final Map<String, FindPrimarykeyMode>	datasByPks;
	/** The table new row mapping list */
	private final Map<Integer, IRowMapp>			addRows;
	/** The table update row mapping list */
	private final Map<Integer, IRowMapp>			updateRows;
	/** The table delete row mapping list */
	private final Map<Integer, IRowMapp>			deleteRows;
	/** The table heads mapping */
	private final List<String>						heads;
	/** The table max id index */
	private final AtomicInteger						idCounter		= new AtomicInteger(0);
	/** The table is init mapping */
	private boolean									isInit;
	private final TableSetting						config;
	private final ReadWriteLock						readWriteLock	= new ReentrantReadWriteLock();
	private final IDataMappManager					dataMappManager;
	
	/**
	 * 
	 * @param dataMappManager
	 * @param tableName
	 */
	TableMapp(IDataMappManager dataMappManager, TableSetting config) {
		this.dataMappManager = dataMappManager;
		this.config = config;
		tableName = config.name;
		datas = new ArrayList<IRowMapp>();
		datasById = new ConcurrentHashMap<Integer, IRowMapp>();
		datasByPks = new ConcurrentHashMap<String, FindPrimarykeyMode>();
		addRows = new ConcurrentHashMap<Integer, IRowMapp>();
		updateRows = new ConcurrentHashMap<Integer, IRowMapp>();
		deleteRows = new ConcurrentHashMap<Integer, IRowMapp>();
		heads = new ArrayList<String>();
	}
	
	/**
	 * 映射数据库数据
	 * 
	 * @param conn
	 * @throws Exception
	 */
	void initTable(Connection conn) throws Exception {
		final List<FindPKConfig> findpks = config.findpks;
		final List<FindPrimarykeyMode> findList = new ArrayList<FindPrimarykeyMode>();
		for (FindPKConfig pk : findpks) {
			FindPrimarykeyMode mode = new FindPrimarykeyMode(pk.key);
			datasByPks.put(pk.key, mode);
			findList.add(mode);
		}
		String sql = "select * from " + tableName;// + " ORDER BY id ASC";
		PreparedStatement stmt = conn.prepareStatement(sql);
		ResultSet rset = stmt.executeQuery();
		
		ResultSetMetaData metaData = rset.getMetaData();
		for (int i = 0; i < metaData.getColumnCount(); ++i) {
			heads.add(metaData.getColumnName(i + 1));
		}
		int col = 0;
		while (rset.next()) {
			if (rset.isBeforeFirst()) {
				rset.next();
			}
			
			Class<?> pojoClazz = Class.forName(config.className);
			Object pojo = pojoClazz.newInstance();
			if (!(pojo instanceof IRowMapp)) {
				throw new IllegalStateException("Cannot deserialize object: " + pojo + ", type: " + config.className + " -- It doesn't implement the IRowMapp interface");
			}
			// RowMapp row = new RowMapp(this);
			IRowMapp row = (IRowMapp) pojo;
			for (col = 1; col <= heads.size(); col++) {
				Object rawDataObj = rset.getObject(col);
				int type = metaData.getColumnType(col);
				if (rawDataObj == null) {
					continue;
				}
				setMappFieldValue(row, heads.get(col - 1), rawDataObj, type);
			}
			if (row instanceof BaseRowMapp) {
				((BaseRowMapp) row).setTableMapp(this);
			}
			datas.add(row);
			idCounter.set(Math.max(row.getId(), idCounter.get()));
			for (FindPrimarykeyMode mode : findList) {
				mode.putRowMapp(row);
			}
			datasById.put(row.getId(), row);
		}
		isInit = true;
	}
	
	/**
	 * 获得映射表名称
	 * 
	 * @return
	 */
	public String getName() {
		return tableName;
	}
	
	@Override
	public IRowMapp getRowMappByPrimarykey(String key, Object value) {
		readWriteLock.readLock().lock();
		IRowMapp row = null;
		FindPrimarykeyMode mode = datasByPks.get(key);
		if (mode != null)
			row = mode.datasByPrimarykey.get(value);
		readWriteLock.readLock().unlock();
		return row;
	}
	
	@Override
	public IRowMapp getRowMappByID(Integer id) {
		readWriteLock.readLock().lock();
		IRowMapp row = datasById.get(id);
		readWriteLock.readLock().unlock();
		return row;
	}
	
	@Override
	public List<IRowMapp> getAllData() {
		readWriteLock.readLock().lock();
		List<IRowMapp> list = new ArrayList<IRowMapp>(datas);
		readWriteLock.readLock().unlock();
		return list;
	}
	
	@Override
	public List<IRowMapp> getRowMappByColValue(String colName, Object value) {
		List<IRowMapp> list = new ArrayList<IRowMapp>();
		try {
			for (IRowMapp row : datas) {
				if (getMappFieldValue(row, colName).equals(value)) {
					list.add(row);
				}
			}
		} catch (Exception e) {
			throw new MPRuntimeException(e);
		}
		return list;
	}
	
	@Override
	public void addRowMapp(IRowMapp row) {
		checkState();
		readWriteLock.writeLock().lock();
		row.setId(idCounter.incrementAndGet());
		addRows.put(row.getId(), row);
		try {
			Collection<FindPrimarykeyMode> findList = datasByPks.values();
			for (FindPrimarykeyMode mode : findList) {
				mode.putRowMapp(row);
			}
		} catch (Exception e) {
			throw new MPRuntimeException(e);
		}
		datasById.put(row.getId(), row);
		datas.add(row);
		if (row instanceof BaseRowMapp) {
			((BaseRowMapp) row).setTableMapp(this);
		}
		readWriteLock.writeLock().unlock();
	}
	
	@Override
	public void removeRowMapp(IRowMapp row) {
		checkState();
		readWriteLock.writeLock().lock();
		if (addRows.containsKey(row.getId())) {
			addRows.remove(row);
		} else {
			if (!deleteRows.containsKey(row.getId())) {
				deleteRows.put(row.getId(), row);
			}
		}
		Collection<FindPrimarykeyMode> findList = datasByPks.values();
		try {
			for (FindPrimarykeyMode mode : findList) {
				mode.removeRowMapp(row);
			}
		} catch (Exception e) {
			throw new MPRuntimeException(e);
		}
		datasById.remove(row.getId());
		datas.remove(row);
		readWriteLock.writeLock().unlock();
	}
	
	@Override
	public void updateRowMapp(IRowMapp row) {
		checkState();
		readWriteLock.writeLock().lock();
		if (!updateRows.containsKey(row)) {
			updateRows.put(row.getId(), row);
		}
		readWriteLock.writeLock().unlock();
	}
	
	/**
	 * 
	 * @throws MPRuntimeException
	 */
	private void checkState() throws MPRuntimeException {
		if (!isInit)
			throw new MPRuntimeException("The table mapping is not init!");
	}
	
	@Override
	public IDataMappManager getDataMappManager() {
		return dataMappManager;
	}
	
	/**
	 * 提交映射表数据到数据库
	 * 
	 * @throws Exception
	 */
	synchronized void commit(Connection conn) throws Exception {
		checkState();
		
		readWriteLock.readLock().lock();
		commitNewRows(conn);
		commitRemoveRows(conn);
		commitUpdateRows(conn);
		readWriteLock.readLock().unlock();
	}
	
	private static final void setMappFieldValue(Object pojo, String fieldName, Object fieldValue, int type) throws Exception {
		Class<?> pojoClazz = pojo.getClass();
		Field field = pojoClazz.getDeclaredField(fieldName);
		int modifiers = field.getModifiers();
		
		if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
			return;
		}
		field.setAccessible(true);
		if (type == -4 || type == -3) {
			IMPObject mpo = MPObject.newFromBinaryData((byte[]) fieldValue);
			field.set(pojo, mpo);
		} else {
			field.set(pojo, fieldValue);
		}
	}
	
	private static final Object getMappFieldValue(Object pojo, String fieldName) throws Exception {
		Class<?> pojoClazz = pojo.getClass();
		Field field = pojoClazz.getDeclaredField(fieldName);
		int modifiers = field.getModifiers();
		if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
			return null;
		}
		return field.get(pojo);
	}
	
	/**
	 * 
	 * @param conn
	 * @throws Exception
	 */
	private final void commitRemoveRows(Connection conn) throws Exception {
		// detete rows commit db
		if (deleteRows.size() > 0) {
			String sql = String.format("delete from %s where id=?", tableName);
			commitRows(deleteRows, sql, conn, true);
		}
	}
	
	/**
	 * 
	 * @param conn
	 * @throws Exception
	 */
	private final void commitUpdateRows(Connection conn) throws Exception {
		// update rows commit db
		if (updateRows.size() > 0) {
			StringBuilder setsql = new StringBuilder();
			for (int i = 1; i < heads.size(); ++i) {
				String colName = heads.get(i);
				if (i > 1)
					setsql.append(",");
				setsql.append(colName);
				setsql.append("=?");
			}
			
			String sql = String.format("update %s set %s where id=?", tableName, setsql.toString());
			commitRows(updateRows, sql, conn, false);
		}
	}
	
	/**
	 * 
	 * @param conn
	 * @throws Exception
	 */
	private final void commitNewRows(Connection conn) throws Exception {
		// new rows commit db
		if (addRows.size() > 0) {
			StringBuilder setsql = new StringBuilder();
			for (int i = 0; i < heads.size(); ++i) {
				if (i > 0)
					setsql.append(",");
				setsql.append("?");
			}
			
			StringBuilder setsql1 = new StringBuilder();
			setsql1.append(tableName);
			setsql1.append("(");
			for (int i = 1; i < heads.size(); ++i) {
				String colName = heads.get(i);
				if (i > 1)
					setsql1.append(",");
				setsql1.append(colName);
			}
			setsql1.append(",id)");
			String sql = String.format("insert into %s values(%s)", setsql1.toString(), setsql.toString());
			commitRows(addRows, sql, conn, false);
		}
	}
	
	/**
	 * 
	 * @param rows
	 * @param sql
	 * @param conn
	 * @param remove
	 * @throws Exception
	 */
	private final void commitRows(Map<Integer, IRowMapp> rows, String sql, Connection conn, boolean remove) throws Exception {
		int col = 0, i = 0;
		IRowMapp row = null;
		conn.setAutoCommit(false);
		PreparedStatement ps = conn.prepareStatement(sql);
		IRowMapp[] rowvalues = new IRowMapp[rows.size()];
		rows.values().toArray(rowvalues);
		
		for (i = 0; i < rowvalues.length; ++i) {
			row = rowvalues[i];
			if (!remove) {
				for (col = 1; col < heads.size(); ++col) {
					String colName = heads.get(col);
					Object fieldValue = getMappFieldValue(row, colName);
					if (fieldValue instanceof IMPObject) {
						IMPObject mpo = (IMPObject) fieldValue;
						ps.setBytes(col, mpo.toBinary());
					} else {
						ps.setObject(col, fieldValue);
					}
				}
			}
			ps.setInt(remove ? 1 : heads.size(), row.getId());
			ps.addBatch();
		}
		rows.clear();
		ps.executeBatch();
		conn.commit();
	}
	
	/**
	 * 
	 * @ClassName: FindPrimarykeyMode
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年3月3日 下午1:34:46
	 *
	 */
	private static final class FindPrimarykeyMode {
		private final String				colName;
		private final Map<Object, IRowMapp>	datasByPrimarykey;
		
		/**
		 * 
		 * @param colName
		 */
		private FindPrimarykeyMode(String colName) {
			this.colName = colName;
			datasByPrimarykey = new ConcurrentHashMap<Object, IRowMapp>();
		}
		
		/**
		 * 
		 * @param row
		 */
		private void putRowMapp(IRowMapp row) throws Exception {
			datasByPrimarykey.put(getMappFieldValue(row, colName), row);
		}
		
		/**
		 * 
		 * @param row
		 * @throws Exception
		 */
		private void removeRowMapp(IRowMapp row) throws Exception {
			datasByPrimarykey.remove(getMappFieldValue(row, colName));
		}
	}
	
}