package com.mpnet.controllers;

import com.mpnet.bitswarm.io.IRequest;
import com.mpnet.exceptions.MPRequestValidationException;

/**
 * 
 * @ClassName: IControllerCommand 
 * @Description: TODO(这里用一句话描述这个类的作用) 
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 上午10:42:44 
 *
 */
public interface IControllerCommand{
	/**
	 * 
	 * @param request
	 * @return
	 * @throws MPRequestValidationException
	 */
	public boolean validate(IRequest request) throws MPRequestValidationException;

	/**
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Object preProcess(IRequest request) throws Exception;

	/**
	 * 
	 * @param request
	 * @throws Exception
	 */
	public void execute(IRequest request) throws Exception;
}