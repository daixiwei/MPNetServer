package com.mpnet.common.data;

/**
 * 
 * @ClassName: MPDataType 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:27:28 
 *
 */
public enum MPDataType{
	  NULL(0), 
	  BOOL(1), 
	  BYTE(2), 
	  SHORT(3), 
	  INT(4), 
	  LONG(5), 
	  FLOAT(6), 
	  DOUBLE(7), 
	  UTF_STRING(8), 
	  BYTE_ARRAY(10), 
	  MP_ARRAY(17), 
	  MP_OBJECT(18),
	  CLASS(19);

	private int typeID;

	private MPDataType(int typeID) {
		this.typeID = typeID;
	}

	public static MPDataType fromTypeId(int typeId) {
		for (MPDataType item : values()) {
			if (item.getTypeID() == typeId) {
				return item;
			}
		}

		throw new IllegalArgumentException("Unknown typeId for MPDataType");
	}

	public int getTypeID() {
		return this.typeID;
	}
}