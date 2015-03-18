package com.mpnet.extensions.filter;

import com.mpnet.extensions.ExtensionLogLevel;
import com.mpnet.extensions.MPExtension;

/**
 * 
 * @ClassName: MPExtensionFilter 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 上午10:31:14 
 *
 */
public abstract class MPExtensionFilter implements IFilter {
	private String name;
	protected MPExtension parentExtension;

	public void init(MPExtension ext) {
		this.parentExtension = ext;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	protected void trace(Object[] args) {
		this.parentExtension.trace(args);
	}

	protected void trace(ExtensionLogLevel level, Object[] args) {
		this.parentExtension.trace(level, args);
	}
}