package com.cory.util;

import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class HtmlUtil {

	public static String removeAllHtmlTags(String html) {
		if (null == html) {
			return null;
		}
		if (StringUtils.isBlank(html)) {
			return html.trim();
		}

		//定义script的正则表达式，去除js可以防止注入
		String scriptRegex="<script[^>]*?>[\\s\\S]*?<\\/script>";
		//定义style的正则表达式，去除style样式，防止css代码过多时只截取到css样式代码
		String styleRegex="<style[^>]*?>[\\s\\S]*?<\\/style>";
		//定义HTML标签的正则表达式，去除标签，只提取文字内容
		String htmlRegex="<[^>]+>";
		//定义空格,回车,换行符,制表符
		String spaceRegex = "\\s*|\t|\r|\n";

		return html
				// 过滤script标签
				.replaceAll(scriptRegex, "")
				// 过滤style标签
				.replaceAll(styleRegex, "")
				// 过滤html标签
				.replaceAll(htmlRegex, "")
				// 过滤空格等
				.replaceAll(spaceRegex, "")
				.trim();
	}

	public static void main(String[] args){
		String htmlStr= "<script type>var i=1; alert(i)</script><style> .font1{font-size:12px}</style><span>少年中国说。</span>红日初升，其道大光。<h3>河出伏流，一泻汪洋。</h3>潜龙腾渊， 鳞爪飞扬。乳 虎啸  谷，百兽震惶。鹰隼试翼，风尘吸张。奇花初胎，矞矞皇皇。干将发硎，有作其芒。天戴其苍，地履其黄。纵有千古，横有" +
				"八荒。<a href=\"www.baidu.com\">前途似海，来日方长</a>。<h1>美哉我少年中国，与天不老！</h1><p>壮哉我中国少年，与国无疆！</p>";
		System.out.println(removeAllHtmlTags(htmlStr));
	}
}
