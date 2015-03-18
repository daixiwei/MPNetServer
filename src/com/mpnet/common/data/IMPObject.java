package com.mpnet.common.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @ClassName: IMPObject
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:23:18
 *
 */
public interface IMPObject {
	public boolean isNull(String key);
	
	public boolean containsKey(String key);
	
	public boolean removeElement(String key);
	
	public Set<String> getKeys();
	
	public int size();
	
	public Iterator<Map.Entry<String, MPDataWrapper>> iterator();
	
	public byte[] toBinary();
	
	public String toJson();
	
	public MPDataWrapper get(String key);
	
	public Boolean getBool(String key);
	
	public Byte getByte(String key);
	
	public Integer getUnsignedByte(String key);
	
	public Short getShort(String key);
	
	public Integer getInt(String key);
	
	public Long getLong(String key);
	
	public Float getFloat(String key);
	
	public Double getDouble(String key);
	
	public String getUtfString(String key);
	
	public byte[] getByteArray(String key);
	
	public IMPArray getMPArray(String key);
	
	public IMPObject getMPObject(String key);
	
	public Object getClass(String key);
	
	public void putNull(String key);
	
	public void putBool(String key, boolean value);
	
	public void putByte(String key, byte value);
	
	public void putShort(String key, short value);
	
	public void putInt(String key, int value);
	
	public void putLong(String key, long value);
	
	public void putFloat(String key, float value);
	
	public void putDouble(String key, double value);
	
	public void putUtfString(String key, String value);
	
	public void putByteArray(String key, byte[] data);
	
	public void putMPArray(String key, IMPArray array);
	
	public void putMPObject(String key, IMPObject object);
	
	public void putClass(String key, Object value);
	
	public void put(String key, MPDataWrapper wrapper);
}