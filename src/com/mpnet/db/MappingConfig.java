package com.mpnet.db;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @ClassName: MappingConfig
 * @Description: The database mapping config class.
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月3日 上午11:39:57
 *
 */
public class MappingConfig {
	boolean						autoCommit			= false;
	int							initialDelayTime	= 60;
	int							intervalTime		= 60;
	volatile List<TableSetting>	tables				= new ArrayList<TableSetting>();
	
	/**
	 * 
	 * @ClassName: TableSetting
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年3月3日 下午1:15:20
	 *
	 */
	static final class TableSetting {
		String				name		= "";
		String				className	= "com.mpnet.db.RowMapp";
		List<FindPKConfig>	findpks		= new ArrayList<FindPKConfig>();
	}
	
	/**
	 * 
	 * @ClassName: FindPKConfig
	 * @Description: TODO(这里用一句话描述这个类的作用)
	 * @author daixiwei daixiwei15@126.com
	 * @date 2015年3月3日 下午1:15:17
	 *
	 */
	static final class FindPKConfig {
		String	key	= "";
		int		colIndex;
	}
}
