package com.mpnet.common.data;

/**
 * 
 * @ClassName: MPObjectLite
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:28:00
 *
 */
public final class MPObjectLite extends MPObject {
	public static MPObject newInstance() {
		return new MPObjectLite();
	}
	
	@Override
	public Byte getByte(String key) {
		Integer i = super.getInt(key);
		
		return i != null ? Byte.valueOf(i.byteValue()) : null;
	}
	
	@Override
	public Short getShort(String key) {
		Integer i = super.getInt(key);
		
		return i != null ? Short.valueOf(i.shortValue()) : null;
	}
	
	@Override
	public Float getFloat(String key) {
		Double d = super.getDouble(key);
		
		return d != null ? Float.valueOf(d.floatValue()) : null;
	}
}