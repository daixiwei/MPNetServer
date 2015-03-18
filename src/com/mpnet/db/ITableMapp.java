package com.mpnet.db;

import java.util.List;

/**
 * 
 * @ClassName: ITableMapp
 * @Description: The table mapping class.
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月11日 下午2:33:05
 *
 */
public interface ITableMapp {
	
	/**
	 * Get RowMapp by id
	 * 
	 * @param id
	 * @return
	 */
	public IRowMapp getRowMappByID(Integer id);
	
	/**
	 * Get all data
	 * 
	 * @return
	 */
	public List<IRowMapp> getAllData();
	
	/**
	 * 获得指定数据映射,查询性能较高需要在mappingdb.xml中配置
	 * 
	 * @param key
	 *            the find key
	 * @param value
	 *            the pk value
	 * @return
	 */
	public IRowMapp getRowMappByPrimarykey(String key, Object value);
	
	/**
	 * 获得指定数据映射,数据多时查询性能不高
	 * 
	 * @param colName
	 * @param value
	 * @return
	 */
	public List<IRowMapp> getRowMappByColValue(String colName, Object value);
	
	/**
	 * 添加行数据到映射表中
	 * 
	 * @param row
	 */
	public void addRowMapp(IRowMapp row);
	
	/**
	 * 删除指定映射表中行数据
	 * 
	 * @param row
	 */
	public void removeRowMapp(IRowMapp row);
	
	/**
	 * 更新指定映射表中行数据
	 * 
	 * @param row
	 */
	public void updateRowMapp(IRowMapp row);
	
	/**
	 * 
	 * @return
	 */
	public IDataMappManager getDataMappManager();
}
