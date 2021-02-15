package com.cory.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 对于这种值的字段：a=123|b=456
 * 
 * @author Cory
 * 
 */
public class StringMap {
	
	private static final String DELIMITER = "\\|";
	private static final String PURE_DELIMITER = "|";
	
	private Map<String, String> internalMap = new HashMap<String, String>();

	public StringMap(String flatStringMap) {
		if (flatStringMap == null || flatStringMap.length() <= 0) {
			return;
		}
		
		String[] fieldVals = flatStringMap.split(DELIMITER);
		
		if (fieldVals != null && fieldVals.length > 0) {
			for (int k = 0; k < fieldVals.length; k++) {
				String[] pairs = fieldVals[k].split("=");
				if (pairs.length == 2) {
					internalMap.put(pairs[0], pairs[1]);
				} else {
					internalMap.put(pairs[0], "");
				}
			}
		}
	}

	public StringMap(Map<String, String> stringMap) {
		internalMap = stringMap;
	}

	public String get(String key) {
		return internalMap.get(key);
	}

	public void set(String key, String value) {
		internalMap.put(key, value);
	}

	public String getFlatStringMap() {
		if (null == internalMap || internalMap.size() == 0) {
			return "";
		}
		
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<String, String>> it = internalMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> entry = it.next();
			if (sb.length() > 0) {
				sb.append(PURE_DELIMITER);
			}
			sb.append(entry.getKey());
			sb.append("=");
			sb.append(entry.getValue());
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		String s = null;
		StringMap sm = new StringMap(s);
		System.out.println(sm.getFlatStringMap());
	}
}
