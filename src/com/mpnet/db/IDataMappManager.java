package com.mpnet.db;

import com.mpnet.bitswarm.service.ISimpleService;
import com.mpnet.exceptions.MPException;

/**
 * 
 * @ClassName: IDataMappManager
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月3日 上午11:05:27
 *
 */
public interface IDataMappManager extends ISimpleService {
	/**
	 * 获得指定表格数据映射对象
	 * 
	 * @param table
	 *            表格名称
	 * @return
	 */
	public TableMapp getTableMapp(String table);
	
	/**
	 * 强制提交映射表数据到数据库
	 * 
	 * @throws MPException
	 */
	public void enforceCommit() throws Exception;
}
