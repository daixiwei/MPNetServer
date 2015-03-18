package com.mpnet.util;

import java.nio.ByteBuffer;
import com.mpnet.config.DefaultConstants;

/**
 * 
 * @ClassName: NetworkServices
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:16:43
 *
 */
public class NetworkServices {
	public static ByteBuffer allocateBuffer(int size, String type) {
		ByteBuffer bb = null;
		
		if (type.equalsIgnoreCase(DefaultConstants.BUFFER_TYPE_DIRCT)) {
			bb = ByteBuffer.allocateDirect(size);
		} else if (type.equalsIgnoreCase(DefaultConstants.BUFFER_TYPE_HEAP)) {
			bb = ByteBuffer.allocate(size);
		}
		return bb;
	}
}