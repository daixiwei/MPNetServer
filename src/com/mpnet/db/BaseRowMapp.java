package com.mpnet.db;

/**
 * 
 * @ClassName: BaseRowMapp
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月11日 下午3:48:33
 *
 */
public abstract class BaseRowMapp implements IRowMapp {
	private ITableMapp	tableMapp;
	
	/**
	 * Get table mapping
	 * 
	 * @return
	 */
	public ITableMapp getTableMapp() {
		return tableMapp;
	}
	
	/**
	 * Insert data to cache
	 */
	public synchronized void insert() {
		tableMapp.addRowMapp(this);
	}
	
	/**
	 * Update data to cache
	 * 
	 */
	public synchronized void update() {
		tableMapp.updateRowMapp(this);
	}
	
	/**
	 * Delete data to cache
	 */
	public synchronized void delete() {
		tableMapp.removeRowMapp(this);
	}
	
	/**
	 * Commit data to database.
	 * 
	 * @throws Exception
	 */
	public synchronized void commit() throws Exception {
		tableMapp.getDataMappManager().enforceCommit();
	}
	
	/**
	 * Set table mapping
	 * 
	 * @param tableMapp
	 */
	void setTableMapp(ITableMapp tableMapp) {
		this.tableMapp = tableMapp;
	}
}
