package com.mpnet.common.data;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * 
 * @ClassName: MPArray 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午2:34:44 
 *
 */
public class MPArray implements IMPArray {
	private List<MPDataWrapper> dataHolder;
	private boolean isChange=false;
	
	public MPArray() {
		dataHolder = new ArrayList<MPDataWrapper>();
	}

	public static IMPArray newFromBinaryData(byte[] bytes) {
		return MPDataSerializer.getInstance().binary2array(bytes);
	}

	public static IMPArray newFromJsonData(String jsonStr) {
		return MPDataSerializer.getInstance().json2array(jsonStr);
	}

	public static MPArray newInstance() {
		return new MPArray();
	}

	public byte[] toBinary() {
		return MPDataSerializer.getInstance().array2binary(this);
	}

	public String toJson() {
		return MPDataSerializer.getInstance().array2json(flatten());
	}

	public boolean isNull(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);

		if (wrapper == null) {
			return false;
		}
		return wrapper.getTypeId() == MPDataType.NULL;
	}

	public MPDataWrapper get(int index) {
		return (MPDataWrapper) this.dataHolder.get(index);
	}

	public Boolean getBool(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Boolean) wrapper.getObject() : null;
	}

	public Byte getByte(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Byte) wrapper.getObject() : null;
	}

	public Integer getUnsignedByte(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? Integer.valueOf(MPDataSerializer.getInstance().getUnsignedByte(
				((Byte) wrapper.getObject()).byteValue())) : null;
	}

	public Short getShort(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Short) wrapper.getObject() : null;
	}

	public Integer getInt(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Integer) wrapper.getObject() : null;
	}

	public Long getLong(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Long) wrapper.getObject() : null;
	}

	public Float getFloat(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Float) wrapper.getObject() : null;
	}

	public Double getDouble(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (Double) wrapper.getObject() : null;
	}

	public String getUtfString(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (String) wrapper.getObject() : null;
	}

	public byte[] getByteArray(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (byte[]) wrapper.getObject() : null;
	}

	public IMPArray getMPArray(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (IMPArray) wrapper.getObject() : null;
	}

	public IMPObject getMPObject(int index) {
		MPDataWrapper wrapper = (MPDataWrapper) this.dataHolder.get(index);
		return wrapper != null ? (IMPObject) wrapper.getObject() : null;
	}
	
	public Object getClass(int index){
		MPDataWrapper wrapper = (MPDataWrapper) dataHolder.get(index);
		return wrapper != null ? wrapper.getObject() : null;
	}
	
	public void addBool(boolean value) {
		addObject(Boolean.valueOf(value), MPDataType.BOOL);
	}

	public void addByte(byte value) {
		addObject(Byte.valueOf(value), MPDataType.BYTE);
	}

	public void addByteArray(byte[] value) {
		addObject(value, MPDataType.BYTE_ARRAY);
	}

	public void addDouble(double value) {
		addObject(Double.valueOf(value), MPDataType.DOUBLE);
	}


	public void addFloat(float value) {
		addObject(Float.valueOf(value), MPDataType.FLOAT);
	}


	public void addInt(int value) {
		addObject(Integer.valueOf(value), MPDataType.INT);
	}


	public void addLong(long value) {
		addObject(Long.valueOf(value), MPDataType.LONG);
	}


	public void addNull() {
		addObject(null, MPDataType.NULL);
	}

	public void addMPArray(IMPArray value) {
		addObject(value, MPDataType.MP_ARRAY);
	}

	public void addMPObject(IMPObject value) {
		addObject(value, MPDataType.MP_OBJECT);
	}

	public void addShort(short value) {
		addObject(Short.valueOf(value), MPDataType.SHORT);
	}


	public void addUtfString(String value) {
		addObject(value, MPDataType.UTF_STRING);
	}
	
	public void addClass(Object value){
		addObject(value, MPDataType.CLASS);
	}
	
	public void add(MPDataWrapper wrappedObject) {
		this.dataHolder.add(wrappedObject);
	}

	public boolean contains(Object obj) {
		if (((obj instanceof IMPArray)) || ((obj instanceof IMPObject))) {
			throw new UnsupportedOperationException("IMPArray and IMPObject are not supported by this method.");
		}
		boolean found = false;

		for (Iterator<MPDataWrapper> iter = dataHolder.iterator(); iter.hasNext();) {
			Object item = ((MPDataWrapper) iter.next()).getObject();

			if (!item.equals(obj))
				continue;
			found = true;
			break;
		}

		return found;
	}

	public Object getElementAt(int index) {
		Object item = null;
		MPDataWrapper wrapper = (MPDataWrapper) dataHolder.get(index);

		if (wrapper != null)
			item = wrapper.getObject();
		return item;
	}

	public Iterator<MPDataWrapper> iterator() {
		return this.dataHolder.iterator();
	}

	public void removeElementAt(int index) {
		this.dataHolder.remove(index);
	}

	public int size() {
		return this.dataHolder.size();
	}

	public String toString() {
		return "[MPArray, size: " + size() + "]";
	}

	private void addObject(Object value, MPDataType typeId) {
		dataHolder.add(new MPDataWrapper(typeId, value));
		isChange=true;
	}

	public boolean equals(Object obj) {
		boolean isEquals = isChange;
		isChange=false;
		return isEquals;
	}
	
	private List<Object> flatten() {
		List<Object> list = new ArrayList<Object>();
		MPDataSerializer.getInstance().flattenArray(list, this);
		return list;
	}
}