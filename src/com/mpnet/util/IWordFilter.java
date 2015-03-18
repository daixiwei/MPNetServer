package com.mpnet.util;

import com.mpnet.core.ICoreService;
import com.mpnet.entities.User;
import java.util.Set;

/**
 * 
 * @ClassName: IWordFilter
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author daixiwei daixiwei15@126.com
 * @date 2015年2月6日 下午5:30:58
 *
 */
public interface IWordFilter extends ICoreService {
	
	public Set<String> getExpressionsList();
	
	public void addExpression(String paramString);
	
	public void removeExpression(String paramString);
	
	public void clearExpressions();
	
	public void loadExpressionList();
	
	public String getMaskCharacter();
	
	public void setMaskCharacter(String paramString);
	
	public String getWordsFile();
	
	public void setWordsFile(String paramString);
	
	public FilteredMessage apply(String paramString);
	
	public FilteredMessage apply(String paramString, User user);
	
	public void setActive(boolean paramBoolean);
}