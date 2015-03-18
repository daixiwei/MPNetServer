package com.mpnet.common.data;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.mpnet.exceptions.MPCodecException;
import com.mpnet.exceptions.MPRuntimeException;

/**
 * 
 * @ClassName: DefaultMPDataSerializer
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月27日 下午5:50:28
 *
 */
public class MPDataSerializer {
	private static final String		CLASS_MARKER_KEY	= "$C";
	private static final String		CLASS_FIELDS_KEY	= "$F";
	private static final String		FIELD_NAME_KEY		= "N";
	private static final String		FIELD_VALUE_KEY		= "V";
	private static MPDataSerializer	instance			= new MPDataSerializer();
	private static int				BUFFER_CHUNK_SIZE	= 512;
	
	public static MPDataSerializer getInstance() {
		return instance;
	}
	
	private MPDataSerializer() {}
	
	public int getUnsignedByte(byte b) {
		return 0xFF & b;
	}
	
	public IMPArray binary2array(byte[] data) {
		if (data.length < 3) {
			throw new IllegalStateException("Can't decode an MPArray. Byte data is insufficient. Size: " + data.length + " bytes");
		}
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		return decodeMPArray(buffer);
	}
	
	private IMPArray decodeMPArray(ByteBuffer buffer) {
		IMPArray mpArray = MPArray.newInstance();
		byte headerBuffer = buffer.get();
		if (headerBuffer != MPDataType.MP_ARRAY.getTypeID()) {
			throw new IllegalStateException("Invalid MPDataType. Expected: " + MPDataType.MP_ARRAY.getTypeID() + ", found: " + headerBuffer);
		}
		short size = buffer.getShort();
		if (size < 0) {
			throw new IllegalStateException("Can't decode MPArray. Size is negative = " + size);
		}
		
		try {
			for (int i = 0; i < size; i++) {
				MPDataWrapper decodedObject = decodeObject(buffer);
				
				if (decodedObject != null)
					mpArray.add(decodedObject);
				else {
					throw new IllegalStateException("Could not decode MPArray staticItem at index: " + i);
				}
			}
		} catch (MPCodecException codecError) {
			throw new IllegalArgumentException(codecError.getMessage());
		}
		return mpArray;
	}
	
	public IMPObject binary2object(byte[] data) {
		if (data.length < 3) {
			throw new IllegalStateException("Can't decode an MPObject. Byte data is insufficient. Size: " + data.length + " bytes");
		}
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
		buffer.put(data);
		buffer.flip();
		return decodeMPObject(buffer);
	}
	
	private IMPObject decodeMPObject(ByteBuffer buffer) {
		IMPObject mpObject = MPObject.newInstance();
		byte headerBuffer = buffer.get();
		if (headerBuffer != MPDataType.MP_OBJECT.getTypeID()) {
			throw new IllegalStateException("Invalid MPDataType. Expected: " + MPDataType.MP_OBJECT.getTypeID() + ", found: " + headerBuffer);
		}
		short size = buffer.getShort();
		if (size < 0) {
			throw new IllegalStateException("Can't decode MPObject. Size is negative = " + size);
		}
		
		try {
			for (int i = 0; i < size; i++) {
				short keySize = buffer.getShort();
				if ((keySize < 0) || (keySize > 255)) {
					throw new IllegalStateException("Invalid MPObject key length. Found = " + keySize);
				}
				byte[] keyData = new byte[keySize];
				buffer.get(keyData, 0, keyData.length);
				String key = new String(keyData);
				MPDataWrapper decodedObject = decodeObject(buffer);
				if (decodedObject != null)
					mpObject.put(key, decodedObject);
				else {
					throw new IllegalStateException("Could not decode value for key: " + keyData);
				}
			}
		} catch (MPCodecException codecError) {
			throw new IllegalArgumentException(codecError.getMessage());
		}
		return mpObject;
	}
	
	/**
	 * 
	 * @param jsonStr
	 * @return
	 */
	public IMPObject json2object(String jsonStr) {
		if (jsonStr.length() < 2) {
			throw new IllegalStateException("Can't decode MPObject. JSON String is too short. Len: " + jsonStr.length());
		}
		
		JSONObject jso = JSONObject.fromObject(jsonStr);
		return decodeMPObject(jso);
	}
	
	/**
	 * 
	 * @param jsonStr
	 * @return
	 */
	public IMPArray json2array(String jsonStr) {
		if (jsonStr.length() < 2) {
			throw new IllegalStateException("Can't decode MPObject. JSON String is too short. Len: " + jsonStr.length());
		}
		
		JSONArray jsa = JSONArray.fromObject(jsonStr);
		return decodeMPArray(jsa);
	}
	
	private IMPArray decodeMPArray(JSONArray jsa) {
		IMPArray mpArray = MPArrayLite.newInstance();
		
		for (Iterator<?> iter = jsa.iterator(); iter.hasNext();) {
			Object value = iter.next();
			MPDataWrapper decodedObject = decodeJsonObject(value);
			if (decodedObject != null)
				mpArray.add(decodedObject);
			else {
				throw new IllegalStateException("(json2sfarray) Could not decode value for object: " + value);
			}
		}
		return mpArray;
	}
	
	private IMPObject decodeMPObject(JSONObject jso) {
		IMPObject mpObject = MPObjectLite.newInstance();
		
		for (Iterator<?> localIterator = jso.keySet().iterator(); localIterator.hasNext();) {
			Object key = localIterator.next();
			Object value = jso.get(key);
			MPDataWrapper decodedObject = decodeJsonObject(value);
			if (decodedObject != null)
				mpObject.put((String) key, decodedObject);
			else {
				throw new IllegalStateException("(json2mpobj) Could not decode value for key: " + key);
			}
		}
		return mpObject;
	}
	
	private MPDataWrapper decodeJsonObject(Object o) {
		if ((o instanceof Integer)) {
			return new MPDataWrapper(MPDataType.INT, o);
		}
		if ((o instanceof Long)) {
			return new MPDataWrapper(MPDataType.LONG, o);
		}
		if ((o instanceof Double)) {
			return new MPDataWrapper(MPDataType.DOUBLE, o);
		}
		if ((o instanceof Boolean)) {
			return new MPDataWrapper(MPDataType.BOOL, o);
		}
		if ((o instanceof String)) {
			return new MPDataWrapper(MPDataType.UTF_STRING, o);
		}
		if ((o instanceof JSONObject)) {
			JSONObject jso = (JSONObject) o;
			if (jso.isNullObject()) {
				return new MPDataWrapper(MPDataType.NULL, null);
			}
			return new MPDataWrapper(MPDataType.MP_OBJECT, decodeMPObject(jso));
		}
		if ((o instanceof JSONArray)) {
			return new MPDataWrapper(MPDataType.MP_ARRAY, decodeMPArray((JSONArray) o));
		}
		
		throw new IllegalArgumentException(String.format("Unrecognized DataType while " + "converting JSONObject 2 MPObject. Object: %s, Type: %s", o, o == null ? "null" : o.getClass()));
	}
	
	public byte[] object2binary(IMPObject object) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_SIZE);
		buffer.put((byte) MPDataType.MP_OBJECT.getTypeID());
		buffer.putShort((short) object.size());
		return obj2bin(object, buffer);
	}
	
	private byte[] obj2bin(IMPObject object, ByteBuffer buffer) {
		Set<String> keys = object.getKeys();
		for (String key : keys) {
			MPDataWrapper wrapper = object.get(key);
			// Object dataObj = wrapper.getObject();
			buffer = encodeMPObjectKey(buffer, key);
			buffer = encodeObject(buffer, wrapper);
		}
		
		int pos = buffer.position();
		byte[] result = new byte[pos];
		buffer.flip();
		buffer.get(result, 0, pos);
		return result;
	}
	
	public byte[] array2binary(IMPArray array) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CHUNK_SIZE);
		buffer.put((byte) MPDataType.MP_ARRAY.getTypeID());
		buffer.putShort((short) array.size());
		return arr2bin(array, buffer);
	}
	
	private byte[] arr2bin(IMPArray array, ByteBuffer buffer) {
		Iterator<MPDataWrapper> iter = array.iterator();
		while (iter.hasNext()) {
			MPDataWrapper wrapper = (MPDataWrapper) iter.next();
			// Object dataObj = wrapper.getObject();
			buffer = encodeObject(buffer, wrapper);
		}
		
		int pos = buffer.position();
		byte[] result = new byte[pos];
		buffer.flip();
		buffer.get(result, 0, pos);
		return result;
	}
	
	public String object2json(Map<String, Object> map) {
		return JSONObject.fromObject(map).toString();
	}
	
	public String array2json(List<Object> array) {
		return JSONArray.fromObject(array).toString();
	}
	
	public void flattenObject(Map<String, Object> map, MPObject mpObj) {
		for (Iterator<Entry<String, MPDataWrapper>> it = mpObj.iterator(); it.hasNext();) {
			Entry<String, MPDataWrapper> entry = it.next();
			String key = (String) entry.getKey();
			MPDataWrapper value = (MPDataWrapper) entry.getValue();
			if (value.getTypeId() == MPDataType.MP_OBJECT) {
				Map<String, Object> newMap = new HashMap<String, Object>();
				map.put(key, newMap);
				flattenObject(newMap, (MPObject) value.getObject());
			} else if (value.getTypeId() == MPDataType.MP_ARRAY) {
				List<Object> newList = new ArrayList<Object>();
				map.put(key, newList);
				flattenArray(newList, (MPArray) value.getObject());
			} else {
				map.put(key, value.getObject());
			}
		}
	}
	
	public void flattenArray(List<Object> array, MPArray mpArray) {
		for (Iterator<MPDataWrapper> it = mpArray.iterator(); it.hasNext();) {
			MPDataWrapper value = (MPDataWrapper) it.next();
			if (value.getTypeId() == MPDataType.MP_OBJECT) {
				Map<String, Object> newMap = new HashMap<String, Object>();
				array.add(newMap);
				flattenObject(newMap, (MPObject) value.getObject());
			} else if (value.getTypeId() == MPDataType.MP_ARRAY) {
				List<Object> newList = new ArrayList<Object>();
				array.add(newList);
				flattenArray(newList, (MPArray) value.getObject());
			} else {
				array.add(value.getObject());
			}
		}
	}
	
	private MPDataWrapper decodeObject(ByteBuffer buffer) throws MPCodecException {
		MPDataWrapper decodedObject = null;
		byte headerByte = buffer.get();
		
		if (headerByte == MPDataType.NULL.getTypeID()) {
			decodedObject = binDecode_NULL(buffer);
		} else if (headerByte == MPDataType.BOOL.getTypeID()) {
			decodedObject = binDecode_BOOL(buffer);
		} else if (headerByte == MPDataType.BYTE.getTypeID()) {
			decodedObject = binDecode_BYTE(buffer);
		} else if (headerByte == MPDataType.BYTE_ARRAY.getTypeID()) {
			decodedObject = binDecode_BYTE_ARRAY(buffer);
		} else if (headerByte == MPDataType.SHORT.getTypeID()) {
			decodedObject = binDecode_SHORT(buffer);
		} else if (headerByte == MPDataType.INT.getTypeID()) {
			decodedObject = binDecode_INT(buffer);
		} else if (headerByte == MPDataType.LONG.getTypeID()) {
			decodedObject = binDecode_LONG(buffer);
		} else if (headerByte == MPDataType.FLOAT.getTypeID()) {
			decodedObject = binDecode_FLOAT(buffer);
		} else if (headerByte == MPDataType.DOUBLE.getTypeID()) {
			decodedObject = binDecode_DOUBLE(buffer);
		} else if (headerByte == MPDataType.UTF_STRING.getTypeID()) {
			decodedObject = binDecode_UTF_STRING(buffer);
		} else if (headerByte == MPDataType.MP_ARRAY.getTypeID()) {
			buffer.position(buffer.position() - 1);
			decodedObject = new MPDataWrapper(MPDataType.MP_ARRAY, decodeMPArray(buffer));
		} else if (headerByte == MPDataType.MP_OBJECT.getTypeID()) {
			buffer.position(buffer.position() - 1);
			IMPObject mpObj = decodeMPObject(buffer);
			MPDataType type = MPDataType.MP_OBJECT;
			Object finalMPObj = mpObj;
			if ((mpObj.containsKey(CLASS_MARKER_KEY)) && (mpObj.containsKey(CLASS_FIELDS_KEY))) {
				type = MPDataType.CLASS;
				finalMPObj = mp2pojo(mpObj);
			}
			decodedObject = new MPDataWrapper(type, finalMPObj);
		} else {
			throw new MPCodecException("Unknow MPDataType ID: " + headerByte);
		}
		return decodedObject;
	}
	
	private ByteBuffer encodeObject(ByteBuffer buffer, MPDataWrapper wrapper) {
		MPDataType typeId = wrapper.getTypeId();
		Object object = wrapper.getObject();
		
		switch (typeId) {
			case NULL:
				buffer = binEncode_NULL(buffer);
				break;
			case BOOL:
				buffer = binEncode_BOOL(buffer, (Boolean) object);
				break;
			case BYTE:
				buffer = binEncode_BYTE(buffer, (Byte) object);
				break;
			case SHORT:
				buffer = binEncode_SHORT(buffer, (Short) object);
				break;
			case INT:
				buffer = binEncode_INT(buffer, (Integer) object);
				break;
			case LONG:
				buffer = binEncode_LONG(buffer, (Long) object);
				break;
			case FLOAT:
				buffer = binEncode_FLOAT(buffer, (Float) object);
				break;
			case DOUBLE:
				buffer = binEncode_DOUBLE(buffer, (Double) object);
				break;
			case UTF_STRING:
				buffer = binEncode_UTF_STRING(buffer, (String) object);
				break;
			case BYTE_ARRAY:
				buffer = binEncode_BYTE_ARRAY(buffer, (byte[]) object);
				break;
			case MP_ARRAY:
				buffer = addData(buffer, array2binary((MPArray) object));
				break;
			case MP_OBJECT:
				buffer = addData(buffer, object2binary((MPObject) object));
				break;
			case CLASS:
				buffer = addData(buffer, object2binary(pojo2mp(object)));
				break;
			default:
				throw new IllegalArgumentException("Unrecognized type in MPObject serialization: " + typeId);
		}
		
		return buffer;
	}
	
	private MPDataWrapper binDecode_NULL(ByteBuffer buffer) {
		return new MPDataWrapper(MPDataType.NULL, null);
	}
	
	private MPDataWrapper binDecode_BOOL(ByteBuffer buffer) throws MPCodecException {
		byte boolByte = buffer.get();
		Boolean bool = null;
		if (boolByte == 0)
			bool = new Boolean(false);
		else if (boolByte == 1)
			bool = new Boolean(true);
		else {
			throw new MPCodecException("Error decoding Bool type. Illegal value: " + bool);
		}
		return new MPDataWrapper(MPDataType.BOOL, bool);
	}
	
	private MPDataWrapper binDecode_BYTE(ByteBuffer buffer) {
		byte boolByte = buffer.get();
		return new MPDataWrapper(MPDataType.BYTE, Byte.valueOf(boolByte));
	}
	
	private MPDataWrapper binDecode_SHORT(ByteBuffer buffer) {
		short shortValue = buffer.getShort();
		return new MPDataWrapper(MPDataType.SHORT, Short.valueOf(shortValue));
	}
	
	private MPDataWrapper binDecode_INT(ByteBuffer buffer) {
		int intValue = buffer.getInt();
		return new MPDataWrapper(MPDataType.INT, Integer.valueOf(intValue));
	}
	
	private MPDataWrapper binDecode_LONG(ByteBuffer buffer) {
		long longValue = buffer.getLong();
		return new MPDataWrapper(MPDataType.LONG, Long.valueOf(longValue));
	}
	
	private MPDataWrapper binDecode_FLOAT(ByteBuffer buffer) {
		float floatValue = buffer.getFloat();
		return new MPDataWrapper(MPDataType.FLOAT, Float.valueOf(floatValue));
	}
	
	private MPDataWrapper binDecode_DOUBLE(ByteBuffer buffer) {
		double doubleValue = buffer.getDouble();
		return new MPDataWrapper(MPDataType.DOUBLE, Double.valueOf(doubleValue));
	}
	
	private MPDataWrapper binDecode_UTF_STRING(ByteBuffer buffer) throws MPCodecException {
		short strLen = buffer.getShort();
		if (strLen < 0) {
			throw new MPCodecException("Error decoding UtfString. Negative size: " + strLen);
		}
		
		byte[] strData = new byte[strLen];
		buffer.get(strData, 0, strLen);
		String decodedString = new String(strData);
		return new MPDataWrapper(MPDataType.UTF_STRING, decodedString);
	}
	
	private MPDataWrapper binDecode_BYTE_ARRAY(ByteBuffer buffer) throws MPCodecException {
		int arraySize = buffer.getInt();
		if (arraySize < 0) {
			throw new MPCodecException("Error decoding typed array size. Negative size: " + arraySize);
		}
		
		byte[] byteData = new byte[arraySize];
		buffer.get(byteData, 0, arraySize);
		return new MPDataWrapper(MPDataType.BYTE_ARRAY, byteData);
	}
	
	private ByteBuffer binEncode_NULL(ByteBuffer buffer) {
		return addData(buffer, new byte[1]);
	}
	
	private ByteBuffer binEncode_BOOL(ByteBuffer buffer, Boolean value) {
		byte[] data = new byte[2];
		data[0] = (byte) MPDataType.BOOL.getTypeID();
		data[1] = (byte) (value.booleanValue() ? 1 : 0);
		return addData(buffer, data);
	}
	
	private ByteBuffer binEncode_BYTE(ByteBuffer buffer, Byte value) {
		byte[] data = new byte[2];
		data[0] = (byte) MPDataType.BYTE.getTypeID();
		data[1] = value.byteValue();
		return addData(buffer, data);
	}
	
	private ByteBuffer binEncode_SHORT(ByteBuffer buffer, Short value) {
		ByteBuffer buf = ByteBuffer.allocate(3);
		buf.put((byte) MPDataType.SHORT.getTypeID());
		buf.putShort(value.shortValue());
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer binEncode_INT(ByteBuffer buffer, Integer value) {
		ByteBuffer buf = ByteBuffer.allocate(5);
		buf.put((byte) MPDataType.INT.getTypeID());
		buf.putInt(value.intValue());
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer binEncode_LONG(ByteBuffer buffer, Long value) {
		ByteBuffer buf = ByteBuffer.allocate(9);
		buf.put((byte) MPDataType.LONG.getTypeID());
		buf.putLong(value.longValue());
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer binEncode_FLOAT(ByteBuffer buffer, Float value) {
		ByteBuffer buf = ByteBuffer.allocate(5);
		buf.put((byte) MPDataType.FLOAT.getTypeID());
		buf.putFloat(value.floatValue());
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer binEncode_DOUBLE(ByteBuffer buffer, Double value) {
		ByteBuffer buf = ByteBuffer.allocate(9);
		buf.put((byte) MPDataType.DOUBLE.getTypeID());
		buf.putDouble(value.doubleValue());
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer binEncode_UTF_STRING(ByteBuffer buffer, String value) {
		if (value == null || value.length() == 0) {
			ByteBuffer buf = ByteBuffer.allocate(3);
			buf.put((byte) MPDataType.UTF_STRING.getTypeID());
			buf.putShort((short) 0);
			return addData(buffer, buf.array());
		}
		byte[] stringBytes = value.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(3 + stringBytes.length);
		buf.put((byte) MPDataType.UTF_STRING.getTypeID());
		buf.putShort((short) stringBytes.length);
		buf.put(stringBytes);
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer binEncode_BYTE_ARRAY(ByteBuffer buffer, byte[] value) {
		ByteBuffer buf = ByteBuffer.allocate(5 + value.length);
		buf.put((byte) MPDataType.BYTE_ARRAY.getTypeID());
		buf.putInt(value.length);
		buf.put(value);
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer encodeMPObjectKey(ByteBuffer buffer, String value) {
		ByteBuffer buf = ByteBuffer.allocate(2 + value.length());
		buf.putShort((short) value.length());
		buf.put(value.getBytes());
		return addData(buffer, buf.array());
	}
	
	private ByteBuffer addData(ByteBuffer buffer, byte[] newData) {
		if (buffer.remaining() < newData.length) {
			int newSize = BUFFER_CHUNK_SIZE;
			if (newSize < newData.length) {
				newSize = newData.length;
			}
			ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() + newSize);
			buffer.flip();
			newBuffer.put(buffer);
			buffer = newBuffer;
		}
		buffer.put(newData);
		return buffer;
	}
	
	// ---------------------------------- Class Mapping ---------------------------------
	// *********************************** Class to MPObject *********************************
	private static final IMPObject pojo2mp(Object pojo) {
		IMPObject mpObj = MPObject.newInstance();
		try {
			convertPojo(pojo, mpObj);
		} catch (Exception e) {
			throw new MPRuntimeException(e);
		}
		
		return mpObj;
	}
	
	private static final void convertPojo(Object pojo, IMPObject mpObj) throws Exception {
		Class<?> pojoClazz = pojo.getClass();
		String classFullName = pojoClazz.getCanonicalName();
		if (classFullName == null) {
			throw new IllegalArgumentException("Anonymous classes cannot be serialized!");
		}
		if (!(pojo instanceof Serializable)) {
			throw new IllegalStateException("Cannot serialize object: " + pojo + ", type: " + classFullName + " -- It doesn't implement the SerializableMPType interface");
		}
		
		IMPArray fieldList = MPArray.newInstance();
		mpObj.putUtfString(CLASS_MARKER_KEY, classFullName);
		mpObj.putMPArray(CLASS_FIELDS_KEY, fieldList);
		for (Field field : pojoClazz.getDeclaredFields()) {
			int modifiers = field.getModifiers();
			
			if ((Modifier.isTransient(modifiers)) || (Modifier.isStatic(modifiers))) {
				continue;
			}
			
			String fieldName = field.getName();
			Object fieldValue = field.get(pojo);
			IMPObject fieldDescriptor = MPObject.newInstance();
			fieldDescriptor.putUtfString(FIELD_NAME_KEY, fieldName);
			fieldDescriptor.put(FIELD_VALUE_KEY, wrapPojoField(fieldValue));
			fieldList.addMPObject(fieldDescriptor);
		}
	}
	
	private static final MPDataWrapper wrapPojoField(Object value) {
		if (value == null) {
			return new MPDataWrapper(MPDataType.NULL, null);
		}
		
		MPDataWrapper wrapper = null;
		if ((value instanceof Boolean)) {
			wrapper = new MPDataWrapper(MPDataType.BOOL, value);
		} else if ((value instanceof Byte)) {
			wrapper = new MPDataWrapper(MPDataType.BYTE, value);
		} else if ((value instanceof Short)) {
			wrapper = new MPDataWrapper(MPDataType.SHORT, value);
		} else if ((value instanceof Integer)) {
			wrapper = new MPDataWrapper(MPDataType.INT, value);
		} else if ((value instanceof Long)) {
			wrapper = new MPDataWrapper(MPDataType.LONG, value);
		} else if ((value instanceof Float)) {
			wrapper = new MPDataWrapper(MPDataType.FLOAT, value);
		} else if ((value instanceof Double)) {
			wrapper = new MPDataWrapper(MPDataType.DOUBLE, value);
		} else if ((value instanceof String)) {
			wrapper = new MPDataWrapper(MPDataType.UTF_STRING, value);
		} else if (value.getClass().isArray()) {
			wrapper = new MPDataWrapper(MPDataType.MP_ARRAY, unrollArray((Object[]) value));
		} else if ((value instanceof Collection)) {
			wrapper = new MPDataWrapper(MPDataType.MP_ARRAY, unrollCollection((Collection<?>) value));
		} else if ((value instanceof Map)) {
			wrapper = new MPDataWrapper(MPDataType.MP_OBJECT, unrollMap((Map<?, ?>) value));
		} else if ((value instanceof Serializable)) {
			wrapper = new MPDataWrapper(MPDataType.MP_OBJECT, pojo2mp(value));
		} else if (value instanceof IMPObject) {
			wrapper = new MPDataWrapper(MPDataType.MP_OBJECT, value);
		} else if (value instanceof IMPArray) {
			wrapper = new MPDataWrapper(MPDataType.MP_ARRAY, value);
		}
		return wrapper;
	}
	
	private static final IMPArray unrollArray(Object[] arr) {
		IMPArray array = MPArray.newInstance();
		for (Object item : arr) {
			array.add(wrapPojoField(item));
		}
		return array;
	}
	
	private static final IMPArray unrollCollection(Collection<?> collection) {
		IMPArray array = MPArray.newInstance();
		for (Iterator<?> localIterator = collection.iterator(); localIterator.hasNext();) {
			Object item = localIterator.next();
			array.add(wrapPojoField(item));
		}
		return array;
	}
	
	private static final IMPObject unrollMap(Map<?, ?> map) {
		IMPObject mpObj = MPObject.newInstance();
		Set<?> entries = map.entrySet();
		for (Iterator<?> iter = entries.iterator(); iter.hasNext();) {
			Map.Entry<?, ?> item = (Map.Entry<?, ?>) iter.next();
			Object key = item.getKey();
			if (!(key instanceof String))
				continue;
			mpObj.put((String) key, wrapPojoField(item.getValue()));
		}
		return mpObj;
	}
	
	// *********************************** MPObject to Class *********************************
	private static final Object mp2pojo(IMPObject mpObj) {
		Object pojo = null;
		if ((!mpObj.containsKey(CLASS_MARKER_KEY)) && (!mpObj.containsKey(CLASS_FIELDS_KEY))) {
			throw new MPRuntimeException("The MPObject passed does not represent any serialized class.");
		}
		
		try {
			String className = mpObj.getUtfString(CLASS_MARKER_KEY);
			Class<?> theClass = Class.forName(className);
			pojo = theClass.newInstance();
			if (!(pojo instanceof Serializable)) {
				throw new IllegalStateException("Cannot deserialize object: " + pojo + ", type: " + className + " -- It doesn't implement the Serializable interface");
			}
			convertMPObject(mpObj.getMPArray(CLASS_FIELDS_KEY), pojo);
			
		} catch (Exception e) {
			throw new MPRuntimeException(e);
		}
		return pojo;
	}
	
	private static final Object unwrapPojoField(MPDataWrapper wrapper) {
		Object obj = null;
		MPDataType type = wrapper.getTypeId();
		if (type.getTypeID() <= MPDataType.UTF_STRING.getTypeID()) {
			obj = wrapper.getObject();
		} else if (type == MPDataType.MP_ARRAY) {
			obj = rebuildArray((IMPArray) wrapper.getObject());
		} else if (type == MPDataType.MP_OBJECT) {
			obj = rebuildMap((IMPObject) wrapper.getObject());
		} else if (type == MPDataType.CLASS) {
			obj = wrapper.getObject();
		}
		return obj;
	}
	
	private static final Object rebuildArray(IMPArray mpArray) {
		Collection<Object> collection = new ArrayList<Object>();
		for (Iterator<MPDataWrapper> iter = mpArray.iterator(); iter.hasNext();) {
			Object item = unwrapPojoField(iter.next());
			collection.add(item);
		}
		return collection;
	}
	
	private static final Object rebuildMap(IMPObject mpObj) {
		Map<String, Object> map = new HashMap<String, Object>();
		for (String key : mpObj.getKeys()) {
			MPDataWrapper wrapper = mpObj.get(key);
			map.put(key, unwrapPojoField(wrapper));
		}
		
		return map;
	}
	
	private static final void convertMPObject(IMPArray fieldList, Object pojo) throws Exception {
		for (int j = 0; j < fieldList.size(); j++) {
			IMPObject fieldDescriptor = fieldList.getMPObject(j);
			String fieldName = fieldDescriptor.getUtfString(FIELD_NAME_KEY);
			MPDataWrapper wrapper = fieldDescriptor.get(FIELD_VALUE_KEY);
			setObjectField(pojo, fieldName, wrapper);
		}
	}
	
	private static final void setObjectField(Object pojo, String fieldName, MPDataWrapper wrapper) throws Exception {
		Class<?> pojoClass = pojo.getClass();
		Field field = pojoClass.getDeclaredField(fieldName);
		int fieldModifier = field.getModifiers();
		if (Modifier.isTransient(fieldModifier) || (Modifier.isStatic(fieldModifier) && Modifier.isFinal(fieldModifier))) {
			return;
		}
		Class<?> fieldClass = field.getType();
		if (fieldClass.equals(IMPObject.class) || fieldClass.equals(IMPArray.class)) {
			field.set(pojo, wrapper.getObject());
			return;
		}
		Object fieldValue = unwrapPojoField(wrapper);
		String fieldClassName = field.getType().getSimpleName();
		boolean isArray = field.getType().isArray();
		if (isArray) {
			if (!(fieldValue instanceof Collection)) {
				throw new MPRuntimeException("Problem during MPObject => POJO conversion. Found array field in POJO: " + fieldName + ", but data is not a Collection!");
			}
			
			Collection<?> collection = (Collection<?>) fieldValue;
			fieldValue = collection.toArray();
			int arraySize = collection.size();
			
			Object typedArray = Array.newInstance(field.getType().getComponentType(), arraySize);
			System.arraycopy(fieldValue, 0, typedArray, 0, arraySize);
			
			fieldValue = typedArray;
		} else if (fieldValue instanceof Collection) {
			Collection<?> collection = (Collection<?>) fieldValue;
			
			if ((fieldClassName.equals("ArrayList")) || (fieldClass.equals("List"))) {
				fieldValue = new ArrayList<Object>(collection);
			} else if ((fieldClassName.equals("Deque")) || (fieldClass.equals("ArrayDeque"))) {
				fieldValue = new ArrayDeque<Object>(collection);
			} else if (fieldClassName.equals("LinkedBlockingDeque")) {
				fieldValue = new LinkedBlockingDeque<Object>(collection);
			}
		} else if (fieldValue instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) fieldValue;
			
			if (fieldClassName.equals("HashMap")) {
				fieldValue = new HashMap<Object, Object>(map);
			} else if (fieldClassName.equals("ConcurrentHashMap")) {
				fieldValue = new ConcurrentHashMap<Object, Object>(map);
			}
		}
		field.set(pojo, fieldValue);
	}
	
}