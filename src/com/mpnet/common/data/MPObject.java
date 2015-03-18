package com.mpnet.common.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @ClassName: MPObject
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:28:06
 *
 */
public class MPObject implements IMPObject {
	private Map<String, MPDataWrapper>	dataHolder;
	private boolean						isChange;
	
	public static IMPObject newFromBinaryData(byte[] bytes) {
		return MPDataSerializer.getInstance().binary2object(bytes);
	}
	
	public static IMPObject newFromJsonData(String jsonStr) {
		return MPDataSerializer.getInstance().json2object(jsonStr);
	}
	
	public static MPObject newInstance() {
		return new MPObject();
	}
	
	public MPObject() {
		dataHolder = new ConcurrentHashMap<String, MPDataWrapper>();
	}
	
	public Iterator<Map.Entry<String, MPDataWrapper>> iterator() {
		return this.dataHolder.entrySet().iterator();
	}
	
	public boolean containsKey(String key) {
		return this.dataHolder.containsKey(key);
	}
	
	public boolean removeElement(String key) {
		return this.dataHolder.remove(key) != null;
	}
	
	public int size() {
		return this.dataHolder.size();
	}
	
	public byte[] toBinary() {
		return MPDataSerializer.getInstance().object2binary(this);
	}
	
	public String toJson() {
		return MPDataSerializer.getInstance().object2json(flatten());
	}
	
	public boolean isNull(String key) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(key);
		
		if (wrapper == null) {
			return false;
		}
		return wrapper.getTypeId() == MPDataType.NULL;
	}
	
	public MPDataWrapper get(String key) {
		return (MPDataWrapper) this.dataHolder.get(key);
	}
	
	public Boolean getBool(String key) {
		MPDataWrapper o = (MPDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Boolean) o.getObject();
	}
	
	public Byte getByte(String key) {
		MPDataWrapper o = (MPDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Byte) o.getObject();
	}
	
	public byte[] getByteArray(String key) {
		MPDataWrapper o = (MPDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (byte[]) o.getObject();
	}
	
	public Double getDouble(String key) {
		MPDataWrapper o = (MPDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Double) o.getObject();
	}
	
	public Float getFloat(String key) {
		MPDataWrapper o = (MPDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Float) o.getObject();
	}
	
	public Integer getInt(String key) {
		MPDataWrapper o = (MPDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Integer) o.getObject();
	}
	
	public Set<String> getKeys() {
		return dataHolder.keySet();
	}
	
	public Long getLong(String key) {
		MPDataWrapper o = (MPDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Long) o.getObject();
	}
	
	public IMPArray getMPArray(String key) {
		MPDataWrapper o = (MPDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (IMPArray) o.getObject();
	}
	
	public IMPObject getMPObject(String key) {
		MPDataWrapper o = (MPDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (IMPObject) o.getObject();
	}
	
	public Object getClass(String key) {
		MPDataWrapper o = (MPDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return o.getObject();
	}
	
	public Short getShort(String key) {
		MPDataWrapper o = (MPDataWrapper) dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (Short) o.getObject();
	}
	
	public Integer getUnsignedByte(String key) {
		MPDataWrapper o = (MPDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return Integer.valueOf(MPDataSerializer.getInstance().getUnsignedByte(((Byte) o.getObject()).byteValue()));
	}
	
	public String getUtfString(String key) {
		MPDataWrapper o = (MPDataWrapper) this.dataHolder.get(key);
		
		if (o == null) {
			return null;
		}
		return (String) o.getObject();
	}
	
	public void putBool(String key, boolean value) {
		putObj(key, Boolean.valueOf(value), MPDataType.BOOL);
	}
	
	public void putByte(String key, byte value) {
		putObj(key, Byte.valueOf(value), MPDataType.BYTE);
	}
	
	public void putByteArray(String key, byte[] value) {
		putObj(key, value, MPDataType.BYTE_ARRAY);
	}
	
	public void putDouble(String key, double value) {
		putObj(key, Double.valueOf(value), MPDataType.DOUBLE);
	}
	
	public void putFloat(String key, float value) {
		putObj(key, Float.valueOf(value), MPDataType.FLOAT);
	}
	
	public void putInt(String key, int value) {
		putObj(key, Integer.valueOf(value), MPDataType.INT);
	}
	
	public void putLong(String key, long value) {
		putObj(key, Long.valueOf(value), MPDataType.LONG);
	}
	
	public void putNull(String key) {
		this.dataHolder.put(key, new MPDataWrapper(MPDataType.NULL, null));
	}
	
	public void putMPArray(String key, IMPArray value) {
		putObj(key, value, MPDataType.MP_ARRAY);
	}
	
	public void putMPObject(String key, IMPObject value) {
		putObj(key, value, MPDataType.MP_OBJECT);
	}
	
	public void putShort(String key, short value) {
		putObj(key, Short.valueOf(value), MPDataType.SHORT);
	}
	
	public void putUtfString(String key, String value) {
		putObj(key, value, MPDataType.UTF_STRING);
	}
	
	public void putClass(String key, Object value) {
		putObj(key, value, MPDataType.CLASS);
	}
	
	public void put(String key, MPDataWrapper wrappedObject) {
		putObj(key, wrappedObject, null);
	}
	
	public String toString() {
		return "[MPObject, size: " + size() + "]";
	}
	
	private void putObj(String key, Object value, MPDataType typeId) {
		if (key == null) {
			throw new IllegalArgumentException("MPObject requires a non-null key for a 'put' operation!");
		}
		if (key.length() > 255) {
			throw new IllegalArgumentException("MPObject keys must be less than 255 characters!");
		}
		if (value == null) {
			throw new IllegalArgumentException("MPObject requires a non-null value! If you need to add a null use the putNull() method.");
		}
		if ((value instanceof MPDataWrapper))
			dataHolder.put(key, (MPDataWrapper) value);
		else
			dataHolder.put(key, new MPDataWrapper(typeId, value));
		isChange = true;
	}
	
	public boolean equals(Object obj) {
		boolean isEquals = isChange;
		isChange = false;
		return isEquals;
	}
	
	private Map<String, Object> flatten() {
		Map<String, Object> map = new HashMap<String, Object>();
		MPDataSerializer.getInstance().flattenObject(map, this);
		return map;
	}
}