package com.mpnet.common.data;

import java.util.Iterator;

/**
 * 
 * @ClassName: IMPArray
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月5日 下午3:23:14
 *
 */
public interface IMPArray {
	public boolean contains(Object o);
	
	public Iterator<MPDataWrapper> iterator();
	
	public Object getElementAt(int index);
	
	public MPDataWrapper get(int index);
	
	public void removeElementAt(int index);
	
	public int size();
	
	public byte[] toBinary();
	
	public String toJson();
	
	public void addNull();
	
	public void addBool(boolean value);
	
	public void addByte(byte value);
	
	public void addShort(short value);
	
	public void addInt(int value);
	
	public void addLong(long value);
	
	public void addFloat(float value);
	
	public void addDouble(double value);
	
	public void addUtfString(String value);
	
	public void addByteArray(byte[] data);
	
	public void addMPArray(IMPArray array);
	
	public void addMPObject(IMPObject object);
	
	public void addClass(Object value);
	
	public void add(MPDataWrapper wrapper);
	
	public boolean isNull(int index);
	
	public Boolean getBool(int index);
	
	public Byte getByte(int index);
	
	public Integer getUnsignedByte(int index);
	
	public Short getShort(int index);
	
	public Integer getInt(int index);
	
	public Long getLong(int index);
	
	public Float getFloat(int index);
	
	public Double getDouble(int index);
	
	public String getUtfString(int index);
	
	public byte[] getByteArray(int index);
	
	public IMPArray getMPArray(int index);
	
	public IMPObject getMPObject(int index);
	
	public Object getClass(int index);
}