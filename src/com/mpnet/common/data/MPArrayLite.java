package com.mpnet.common.data;

/**
 * 
 * @ClassName: MPArrayLite 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:44:09 
 *
 */
public class MPArrayLite extends MPArray {
	public static MPArrayLite newInstance() {
		return new MPArrayLite();
	}

	public Byte getByte(int index) {
		Integer i = super.getInt(index);

		return i != null ? Byte.valueOf(i.byteValue()) : null;
	}

	public Short getShort(int index) {
		Integer i = super.getInt(index);

		return i != null ? Short.valueOf(i.shortValue()) : null;
	}

	public Float getFloat(int index) {
		Double d = super.getDouble(index);

		return d != null ? Float.valueOf(d.floatValue()) : null;
	}
}