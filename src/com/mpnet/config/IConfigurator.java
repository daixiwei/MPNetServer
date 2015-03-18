package com.mpnet.config;

import java.io.IOException;

/**
 * 
 * @ClassName: IConfigurator 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午4:45:40 
 *
 */
public interface IConfigurator {
	public void loadConfiguration() throws Exception;
	
	public void saveServerSettings(boolean makeBackup) throws IOException;

	public ServerSettings getServerSettings();
}