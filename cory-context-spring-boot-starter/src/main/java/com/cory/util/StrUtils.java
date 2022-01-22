package com.cory.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

/**
 * 字符串的帮助类，提供静态方法，不可以实例化。
 *
 */
public class StrUtils {
	/**
	 * 禁止实例化
	 */
	private StrUtils() {
	}

	/**
	 * 处理url
	 *
	 * url为null返回null，url为空串或以http://或https://开头，则加上http://，其他情况返回原参数。
	 *
	 * @param url
	 * @return
	 */
	public static String handelUrl(String url) {
		if (url == null) {
			return null;
		}
		url = url.trim();
		if (url.equals("") || url.startsWith("http://")
				|| url.startsWith("https://")) {
			return url;
		} else {
			return "http://" + url.trim();
		}
	}

	/**
	 * 转换成list, 用半角逗号分隔
	 * @param str
	 * @return
	 */
	public static List<String> toList(String str) {
		return toList(str, ",");
	}

	public static List<String> toList(String str, String delimiter) {
		List<String> list = new ArrayList<String>();
		String[] arr = splitAndTrim(str, delimiter);
		if (null == arr || arr.length == 0) {
			return list;
		}
		for (String s : arr) {
			list.add(s);
		}
		return list;
	}

	/**
	 * 转换成list, 用半角逗号分隔
	 * @param str
	 * @return
	 */
	public static List<Integer> toIntList(String str) {
		return toIntList(str, ",");
	}

	public static List<Integer> toIntList(String str, String delimiter) {
		List<String> strList = toList(str, delimiter);
		List<Integer> intList = new ArrayList<Integer>();
		for (String s : strList) {
			intList.add(Integer.valueOf(s));
		}
		return intList;
	}

	/**
	 * 分割并且去除空格
	 *
	 * @param str 待分割字符串
	 * @param delimiter 分割符
	 * @return 如果str为空，则返回null。
	 */
	public static String[] splitAndTrim(String str, String delimiter) {
		if (StringUtils.isBlank(str)) {
			return null;
		}
		String[] arr = StringUtils.split(str, delimiter);
		// trim
		for (int i = 0, len = arr.length; i < len; i++) {
			arr[i] = arr[i].trim();
		}
		return arr;
	}

	/**
	 * 用半角逗号分割并且去除空格
	 *
	 * @param str 待分割字符串
	 * @return 如果str为空，则返回null。
	 */
	public static String[] splitAndTrim(String str) {
		return splitAndTrim(str, ",");
	}

	/**
	 * 文本转html
	 *
	 * @param txt
	 * @return
	 */
	public static String txt2htm(String txt) {
		if (StringUtils.isBlank(txt)) {
			return txt;
		}
		StringBuilder sb = new StringBuilder((int) (txt.length() * 1.2));
		char c;
		boolean doub = false;
		for (int i = 0; i < txt.length(); i++) {
			c = txt.charAt(i);
			if (c == ' ') {
				if (doub) {
					sb.append(' ');
					doub = false;
				} else {
					sb.append("&nbsp;");
					doub = true;
				}
			} else {
				doub = false;
				switch (c) {
					case '&':
						sb.append("&amp;");
						break;
					case '<':
						sb.append("&lt;");
						break;
					case '>':
						sb.append("&gt;");
						break;
					case '"':
						sb.append("&quot;");
						break;
					case '\n':
						sb.append("<br/>");
						break;
					default:
						sb.append(c);
						break;
				}
			}
		}
		return sb.toString();
	}

	/**
	 * 剪切文本。如果进行了剪切，则在文本后加上"..."
	 *
	 * @param s
	 *            剪切对象。
	 * @param len
	 *            编码小于256的作为一个字符，大于256的作为两个字符。
	 * @return
	 */
	public static String textCut(String s, int len, String append) {
		if (s == null) {
			return null;
		}
		int slen = s.length();
		if (slen <= len) {
			return s;
		}
		// 最大计数（如果全是英文）
		int maxCount = len * 2;
		int count = 0;
		int i = 0;
		for (; count < maxCount && i < slen; i++) {
			if (s.codePointAt(i) < 256) {
				count++;
			} else {
				count += 2;
			}
		}
		if (i < slen) {
			if (count > maxCount) {
				i--;
			}
			if (!StringUtils.isBlank(append)) {
				if (s.codePointAt(i - 1) < 256) {
					i -= 2;
				} else {
					i--;
				}
				return s.substring(0, i) + append;
			} else {
				return s.substring(0, i);
			}
		} else {
			return s;
		}
	}

	/**
	 * p换行
	 * @param inputString
	 * @return
	 */
	public static String removeHtmlTagP(String inputString) {
		if (inputString == null)
			return null;
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		Pattern p_script;
		java.util.regex.Matcher m_script;
		Pattern p_style;
		java.util.regex.Matcher m_style;
		Pattern p_html;
		java.util.regex.Matcher m_html;
		try {
			//定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
			//定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签
			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签
			htmlStr.replace("</p>", "\n");
			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签
			textStr = htmlStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return textStr;// 返回文本字符串
	}

	public static String removeHtmlTag(String inputString) {
		if (inputString == null)
			return null;
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		Pattern p_script;
		java.util.regex.Matcher m_script;
		Pattern p_style;
		java.util.regex.Matcher m_style;
		Pattern p_html;
		java.util.regex.Matcher m_html;
		try {
			//定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
			//定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
			p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
			m_script = p_script.matcher(htmlStr);
			htmlStr = m_script.replaceAll(""); // 过滤script标签
			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签
			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签
			textStr = htmlStr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return textStr;// 返回文本字符串
	}

	/**
	 * 检查字符串中是否包含被搜索的字符串。被搜索的字符串可以使用通配符'*'。
	 *
	 * @param str
	 * @param search
	 * @return
	 */
	public static boolean contains(String str, String search) {
		if (StringUtils.isBlank(str) || StringUtils.isBlank(search)) {
			return false;
		}
		String reg = StringUtils.replace(search, "*", ".*");
		Pattern p = Pattern.compile(reg);
		return p.matcher(str).matches();
	}

	public static boolean containsKeyString(String str) {
		if (StringUtils.isBlank(str)) {
			return false;
		}
		if (str.contains("'") || str.contains("\"") || str.contains("\r")
				|| str.contains("\n") || str.contains("\t")
				|| str.contains("\b") || str.contains("\f")) {
			return true;
		}
		return false;
	}


	public static String addCharForString(String str, int strLength,char c,int position) {
		int strLen = str.length();
		if (strLen < strLength) {
			while (strLen < strLength) {
				StringBuffer sb = new StringBuffer();
				if(position==1){
					//右補充字符c
					sb.append(c).append(str);
				}else{
					//左補充字符c
					sb.append(str).append(c);
				}
				str = sb.toString();
				strLen = str.length();
			}
		}
		return str;
	}

	// 将""和'转义
	public static String replaceKeyString(String str) {
		if (containsKeyString(str)) {
			return str.replace("'", "\\'").replace("\"", "\\\"").replace("\r",
					"\\r").replace("\n", "\\n").replace("\t", "\\t").replace(
					"\b", "\\b").replace("\f", "\\f");
		} else {
			return str;
		}
	}

	//单引号转化成双引号
	public static String replaceString(String str) {
		if (containsKeyString(str)) {
			return str.replace("'", "\"").replace("\"", "\\\"").replace("\r",
					"\\r").replace("\n", "\\n").replace("\t", "\\t").replace(
					"\b", "\\b").replace("\f", "\\f");
		} else {
			return str;
		}
	}

	public static String getSuffix(String str) {
		int splitIndex = str.lastIndexOf(".");
		return str.substring(splitIndex + 1);
	}

	public static Integer[] toIntegerArry(String str, String delimiter) {
		if (null == str) {
			return null;
		}
		str = str.trim();
		if (str.equals("")) {
			return null;
		}
		String[] arr = str.split(delimiter);
		Integer[] intArr = new Integer[arr.length];
		for (int i=0; i<arr.length; i++) {
			try {
				intArr[i] = Integer.valueOf(arr[i].trim());
			} catch (NumberFormatException e) {
				intArr[i] = 0;
			}
		}
		return intArr;
	}

	public static String toString(Integer[] arr) {
		return toString(arr, ",");
	}

	public static String toString(Integer[] arr, String contactor) {
		if (null == arr || arr.length == 0) {
			return "";
		}
		StringBuffer buf = new StringBuffer();
		for (int i : arr) {
			buf.append(i);
			buf.append(contactor);
		}
		buf.deleteCharAt(buf.length() - 1);
		return buf.toString();
	}

	public static Integer[] toIntegerArry(String str) {
		return toIntegerArry(str, ",");
	}

	public static String toString(String str, String fromEncoding, String toEncoding) {
		if (str == null || str.equals("")) {
			return "";
		}
		try {
			return new String(str.getBytes(fromEncoding), toEncoding);
		} catch (Exception ex) {
			return "";
		}
	}

	/**
	 * 连接两个字符串，如果为空，则转成空字符串，不要出现null
	 * <p>
	 * 不去掉字符串里的空格，因为有些字符串本来就需要空格
	 * <p>
	 * 如果其中一个为空字符串，或者Null，那么直接返回另外一个，就不要加连接符了
	 *
	 * @param str1
	 * @param str2
	 * @param delimiter 连接符
	 * @return
	 */
	public static String contact(String str1, String str2, String delimiter) {
		if (null == str1) {
			str1 = "";
		}

		if (null == str2) {
			str2 = "";
		}

		if (str1.length() == 0) {
			return str2;
		}

		if (str2.length() == 0) {
			return str1;
		}

		return str1 + delimiter + str2;
	}

	/**
	 * 连接两个字符串，如果为空，则转成空字符串，不要出现null
	 * <p>
	 * 不去掉字符串里的空格，因为有些字符串本来就需要空格
	 * <p>
	 * 如果其中一个为空字符串，或者Null，那么直接返回另外一个，就不要加连接符了
	 * <p>
	 * 用英文逗号连接，如果要用其它连接符，请用带连接符的方法
	 *
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static String contact(String str1, String str2) {
		return contact(str1, str2, ",");
	}

	/**
	 * 判断一个对象是否为null,或者0长度
	 * @param obj
	 * @return
	 */
	public static boolean isEmpty(Object obj) {
		if (obj != null && obj.toString().length() > 0) {
			return false;
		}
		return true;
	}

	public static String firstCapital(String str) {
		if (StringUtils.isEmpty(str)) {
			return str;
		}
		str = str.trim();
		if (str.length() == 1) {
			return str.toUpperCase();
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	/**
	 * 将字符串转为一个json的map，要求字符串是json格式的数组
	 * @param str 格式为：{key: value, key: value}的json字符串
	 * @return
	 */
	public static Map<String, Object> toJsonMap(String str) {
		Map<String, Object> map = new HashMap<>();
		if (StringUtils.isNotBlank(str)) {
			map = JSON.parseObject(str.trim());
		}
		return map;
	}

	public static void main(String args[]) {
		//System.out.println(replaceKeyString("&nbsp;\r" + "</p>"));
		System.out.println(firstCapital("abc"));
	}

}
