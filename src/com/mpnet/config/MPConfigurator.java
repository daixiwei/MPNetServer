package com.mpnet.config;

import com.mpnet.common.util.FileUtils;
import com.mpnet.common.util.FilenameUtils;
import com.mpnet.util.executor.SmartExecutorConfig;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @ClassName: MPConfigurator
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:43:40
 *
 */
public final class MPConfigurator implements IConfigurator {
	private final String			BACKUP_FOLDER	= "_backups";
	private volatile ServerSettings	serverSettings;
	private final Logger			log;
	
	public MPConfigurator() {
		this.log = LoggerFactory.getLogger(getClass());
	}
	
	public void loadConfiguration() throws FileNotFoundException {
		this.serverSettings = loadServerSettings();
	}
	
	public synchronized ServerSettings getServerSettings() {
		return this.serverSettings;
	}
	
	public synchronized void saveServerSettings(boolean makeBackup) throws IOException {
		if (makeBackup) {
			makeBackup(DefaultConstants.SERVER_CFG_FILE);
		}
		OutputStream outStream = new FileOutputStream(DefaultConstants.SERVER_CFG_FILE);
		getServerXStreamDefinitions().toXML(this.serverSettings, outStream);
	}
	
	private ServerSettings loadServerSettings() throws FileNotFoundException {
		FileInputStream inStream = new FileInputStream(DefaultConstants.SERVER_CFG_FILE);
		
		return (ServerSettings) getServerXStreamDefinitions().fromXML(inStream);
	}
	
	private XStream getServerXStreamDefinitions() {
		XStream xstream = new XStream();
		xstream.alias("serverSettings", ServerSettings.class);
		
		xstream.alias("socket", ServerSettings.SocketAddress.class);
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "address");
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "port");
		xstream.useAttributeFor(ServerSettings.SocketAddress.class, "type");
		
		xstream.alias("extension", ServerSettings.ExtensionSettings.class);
		
		xstream.alias("databaseManager", ServerSettings.DatabaseManagerSettings.class);
		xstream.useAttributeFor(ServerSettings.DatabaseManagerSettings.class, "isActive");
		xstream.aliasAttribute(ServerSettings.DatabaseManagerSettings.class, "isActive", "active");
		
		xstream.alias("ipFilter", ServerSettings.IpFilterSettings.class);
		
		xstream.alias("systemThreadPoolSettings", SmartExecutorConfig.class);
		xstream.alias("extensionThreadPoolSettings", SmartExecutorConfig.class);
		
		xstream.alias("userSettings", ServerSettings.UserSettings.class);
		xstream.aliasField("applyWordsFilterToUserName", ServerSettings.UserSettings.class, "isFilterUserNames");
		
		xstream.alias("wordsFilter", ServerSettings.WordFilterSettings.class);
		xstream.useAttributeFor(ServerSettings.WordFilterSettings.class, "isActive");
		xstream.aliasAttribute(ServerSettings.WordFilterSettings.class, "isActive", "active");
		return xstream;
	}
	
	private void makeBackup(String filePath) throws IOException {
		String basePath = FilenameUtils.getPath(filePath);
		String backupBasePath = FilenameUtils.concat(basePath, BACKUP_FOLDER);
		String backupId = getDateTimeString();
		String backupFileName = FilenameUtils.concat(backupBasePath, backupId + "__" + FilenameUtils.getName(filePath));
		
		File sourceFile = new File(filePath);
		File backupFile = new File(backupFileName);
		File backupDir = new File(backupBasePath);
		
		if (!backupDir.exists()) {
			FileUtils.forceMkdir(backupDir);
		}
		FileUtils.copyFile(sourceFile, backupFile);
		
		log.info("backup server config ready!");
	}
	
	private static String getDateTimeString() {
		Calendar calendar = Calendar.getInstance();
		
		calendar.get(Calendar.MONTH);
		calendar.get(Calendar.DAY_OF_MONTH);
		StringBuilder sb = new StringBuilder();
		sb.append(calendar.get(Calendar.YEAR));
		sb.append("-");
		sb.append(calendar.get(Calendar.MONTH));
		sb.append("-");
		sb.append(calendar.get(Calendar.DAY_OF_MONTH));
		sb.append("-");
		sb.append(calendar.get(Calendar.HOUR_OF_DAY));
		sb.append("-");
		sb.append(calendar.get(Calendar.MINUTE));
		sb.append("-");
		sb.append(calendar.get(Calendar.SECOND));
		return sb.toString();
	}
}