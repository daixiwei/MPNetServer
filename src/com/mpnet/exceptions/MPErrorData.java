package com.mpnet.exceptions;


/**
 * 
 * @ClassName: MPErrorData 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年3月3日 下午3:27:53 
 *
 */
public class MPErrorData {
	IErrorCode code;

	public MPErrorData(IErrorCode code) {
		this.code = code;
	}

	public IErrorCode getCode() {
		return this.code;
	}

	public void setCode(IErrorCode code) {
		this.code = code;
	}

}