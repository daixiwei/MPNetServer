package com.mpnet.util;

import com.mpnet.common.util.FileUtils;
import com.mpnet.config.GlobalSettings;
import com.mpnet.core.BaseCoreService;
import com.mpnet.entities.User;
import com.mpnet.exceptions.ExceptionMessageComposer;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MPWordFilter extends BaseCoreService implements IWordFilter {
	private static final String						PUNCTUATION		= ",.;:-_ ";
	private final Logger							logger;
	private final ConcurrentMap<String, Pattern>	dictionary;
	private String									wordsFile;
	private String									maskCharacter	= "*";
	
	public MPWordFilter() {
		setName("MPWordsFilter");
		this.logger = LoggerFactory.getLogger(getClass());
		this.dictionary = new ConcurrentHashMap<String, Pattern>();
	}
	
	public MPWordFilter(String wordsFile) {
		this();
		this.wordsFile = wordsFile;
	}
	
	public void init(Object o) {
		this.name = getId();
		
		loadExpressionList(true);
	}
	
	public void setActive(boolean flag) {
		this.active = flag;
	}
	
	public void destroy(Object o) {
		super.destroy(o);
	}
	
	public FilteredMessage apply(String message) {
		FilteredMessage filtered = applyWhiteListFilter(message);
		return filtered;
	}
	
	public FilteredMessage apply(String message, User user) {
		FilteredMessage filtered = apply(message);
		return filtered;
	}
	
	public void addExpression(String word) {
		this.dictionary.putIfAbsent(word, Pattern.compile(word));
	}
	
	public void clearExpressions() {
		this.dictionary.clear();
	}
	
	public String getWordsFile() {
		return this.wordsFile;
	}
	
	public void setWordsFile(String wordsFile) {
		this.wordsFile = wordsFile;
	}
	
	public Set<String> getExpressionsList() {
		return new HashSet<String>(this.dictionary.keySet());
	}
	
	public void removeExpression(String word) {
		this.dictionary.remove(word);
	}
	
	public String getMaskCharacter() {
		return this.maskCharacter;
	}
	
	public void setMaskCharacter(String mask) {
		this.maskCharacter = mask;
	}
	
	public void loadExpressionList() {
		loadExpressionList(false);
	}
	
	private void loadExpressionList(boolean isInit) {
		clearExpressions();
		try {
			if (this.wordsFile != null) {
				processWordsFile();
				
				if (!isInit)
					this.logger.info("WordsFilter expression file reloaded: " + this.wordsFile);
			} else if (!isInit) {
				this.logger.warn("Reloading WordsFilter expression failed: no file specified, is the filter turned on? ");
			}
		} catch (IOException e) {
			ExceptionMessageComposer message = new ExceptionMessageComposer(e, GlobalSettings.FRIENDLY_LOGGING);
			message.setDescription("the specified words file was not found: " + this.wordsFile);
			message.setPossibleCauses("please double check that the file is really in the location specified in the configuration");
			
			this.logger.warn(message.toString());
		}
	}
	
	private void processWordsFile() throws IOException {
		File theWordFile = new File(this.wordsFile);
		List<String> textLines = FileUtils.readLines(theWordFile);
		
		for (String expression : textLines) {
			addExpression(expression);
		}
	}
	
	private FilteredMessage applyBlackListFilter(String message) {
		FilteredMessage filteredMessage = new FilteredMessage();
		
		StringBuilder buffer = new StringBuilder(message);
		int occurrences = 0;
		
		for (Pattern expression : this.dictionary.values()) {
			Matcher patternMatcher = expression.matcher(buffer);
			
			while (patternMatcher.find()) {
				maskBadWord(buffer, patternMatcher.start(), patternMatcher.end());
				occurrences++;
			}
		}
		
		filteredMessage.setMessage(buffer.toString());
		filteredMessage.setOccurrences(occurrences);
		
		return filteredMessage;
	}
	
	private void maskBadWord(StringBuilder str, int startPos, int endPos) {
		str.replace(startPos, endPos, getStringMask(endPos - startPos));
	}
	
	private FilteredMessage applyWhiteListFilter(String message) {
		FilteredMessage filteredMessage = new FilteredMessage();
		FilteredMessage blackListed = applyBlackListFilter(message);
		
		StringBuilder negativeVersion = new StringBuilder();
		
		boolean prevCharWasBad = false;
		int occurences = 0;
		int pos = 0;
		
		for (char ch : blackListed.getMessage().toCharArray()) {
			if (ch == this.maskCharacter.charAt(0)) {
				negativeVersion.append(message.charAt(pos));
				prevCharWasBad = false;
			} else if (PUNCTUATION.indexOf(ch) > -1) {
				negativeVersion.append(ch);
				prevCharWasBad = false;
			} else {
				negativeVersion.append(this.maskCharacter);
				
				if (!prevCharWasBad) {
					occurences++;
					prevCharWasBad = true;
				}
			}
			
			pos++;
		}
		
		filteredMessage.setMessage(negativeVersion.toString());
		filteredMessage.setOccurrences(occurences);
		
		return filteredMessage;
	}
	
	private String getStringMask(int len) {
		StringBuilder buf = new StringBuilder();
		
		for (int j = 0; j < len; j++) {
			buf.append(this.maskCharacter);
		}
		return buf.toString();
	}
}