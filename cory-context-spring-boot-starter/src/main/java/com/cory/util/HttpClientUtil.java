package com.cory.util;

import com.alibaba.fastjson.JSON;
import com.cory.constant.Constants;
import com.cory.constant.ErrorCode;
import com.cory.exception.CoryException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil {

	public enum HttpMethod {
		GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH, TRACE,
	}

	private static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

	private static final int SOCKET_TIME_OUT_IN_SECOND = 10;
	private static final int CONNECT_TIME_OUT_IN_SECOND = 5;

	/**
	 * 将URL下载并保存到文件
	 * @param url 要下载的url
	 * @param storeFile 保存到的文件
	 * @return
	 * @throws CoryException
	 */
	public static void downloadFile(String url, File storeFile) throws CoryException {
		HttpClient httpClient = newDefaultHttpClient();
		HttpGet get = null;
		BufferedInputStream i = null;
		BufferedOutputStream o = null;
		try {
			get = newHttpGet(url);
			HttpResponse response = httpClient.execute(get);
			i = new BufferedInputStream(response.getEntity().getContent());
			o = new BufferedOutputStream(new FileOutputStream(storeFile));

			byte[] buf = new byte[2048];
			int len;
			while ((len = i.read(buf)) > 0) {
				o.write(buf, 0, len);
			}
			o.flush();
		} catch (Exception e) {
			log.error("download file fail, url: {}, store file: {}", url, storeFile.getAbsolutePath(), e);
			throw new CoryException(ErrorCode.GENERIC_ERROR, e.getLocalizedMessage());
		} finally {
			if (null != i) {
				try {
					i.close();
				} catch (IOException e) {
				}
			}
			if (null != o) {
				try {
					o.close();
				} catch (IOException e) {
				}
			}
		}
		if (null != get) {
			get.abort();
		}
	}

	/**
	 * @param url 绝对路径，包括前面的域名等，因为会有多个域名
	 * @return
	 * @throws CoryException
	 */
	public static String get(String url) throws CoryException {
		String value = null;

		HttpClient httpClient = newDefaultHttpClient();
		HttpGet get = null;
		try {
			get = newHttpGet(url);
			HttpResponse response = httpClient.execute(get);
			value = EntityUtils.toString(response.getEntity(), Constants.UTF8);
		} catch (Exception e) {
			log.error("get fail, url: {}", url, e);
			throw new CoryException(ErrorCode.GENERIC_ERROR, e.getLocalizedMessage());
		}
		if (null != get) {
			get.abort();
		}
		return value;
	}

	/**
	 * post方法，使用表单方式传数据
	 *
	 * @param url -- 绝对路径，包括前面的域名等，因为会有多个域名
	 * @param params
	 * @return
	 */
	public static String post(String url, Map<String, String> params) throws CoryException {
		try {
			return EntityUtils.toString(doPost(url, params), Constants.UTF8);
		} catch (Exception e) {
			log.error("post fail, url: {}, params: {}", url, JSON.toJSONString(params), e);
			throw new CoryException(ErrorCode.GENERIC_ERROR, e.getLocalizedMessage());
		}
	}

	/**
	 * post方法，但使用json的方式传数据
	 *
	 * @param url -- 绝对路径，包括前面的域名等，因为会有多个域名
	 * @param params
	 * @return
	 */
	public static String postJson(String url, Map<String, String> params) throws CoryException {
		HttpClient httpClient = newDefaultHttpClient();
		HttpPost httpPost = newHttpPost(url);

		try {
			StringEntity entity = new StringEntity(JSON.toJSONString(params), ContentType.APPLICATION_JSON);
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			return EntityUtils.toString(response.getEntity(), Constants.UTF8);
		} catch (Exception e) {
			log.error("post json fail, url: {}, params: {}", url, JSON.toJSONString(params), e);
			throw new CoryException(ErrorCode.GENERIC_ERROR, e.getLocalizedMessage());
		}
	}

	/**
	 * @param url -- 绝对路径，包括前面的域名等，因为会有多个域名
	 * @param params
	 * @return a JSON string
	 */
	public static byte[] postWithByteArrayResponse(String url, Map<String, String> params) throws CoryException {
		try {
			return EntityUtils.toByteArray(doPost(url, params));
		} catch (Exception e) {
			log.error("post with byte array response fail, url: {}, params: {}", url, JSON.toJSONString(params), e);
			throw new CoryException(ErrorCode.GENERIC_ERROR, e.getLocalizedMessage());
		}
	}

	private static HttpEntity doPost(String url, Map<String, String> params) throws CoryException {
		HttpClient httpClient = newDefaultHttpClient();
		HttpPost httpPost = newHttpPost(url);

		List<BasicNameValuePair> pairs = buildParamList(params);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, Constants.UTF8);
			httpPost.setEntity(entity);
			HttpResponse response = httpClient.execute(httpPost);
			return response.getEntity();
		} catch (Exception e) {
			log.error("do post fail, url: {}, params: {}", url, JSON.toJSONString(params), e);
			throw new CoryException(ErrorCode.GENERIC_ERROR, e.getLocalizedMessage());
		}
	}

	public static HttpGet newHttpGet(String url) {
		HttpGet get = new HttpGet(url);
		return get;
	}

	public static HttpPost newHttpPost(String url) {
		HttpPost post = new HttpPost(url);
		return post;
	}

	public static HttpClient newDefaultHttpClient() {
		return newDefaultHttpClient(CONNECT_TIME_OUT_IN_SECOND, SOCKET_TIME_OUT_IN_SECOND);
	}

	public static CloseableHttpClient newDefaultHttpClient(int connectTimeoutInSecond, int socketTimeoutInSecond) {
		SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(socketTimeoutInSecond * 1000).build();
		return HttpClientBuilder
				.create()
				.setConnectionTimeToLive(connectTimeoutInSecond, TimeUnit.SECONDS)
				.setDefaultSocketConfig(socketConfig)
				.build();
	}

	public static String appendParamsToUrl(String url, Map<String, String> params) {
		if (null == params || params.size() == 0) {
			return url;
		}

		StringBuffer buf = new StringBuffer();
		params.entrySet().forEach(entry -> {
			String value = entry.getValue();
			if (StringUtils.isNotBlank(value)) {
				try {
					value = URLEncoder.encode(value, Constants.UTF8);
				} catch (UnsupportedEncodingException e) {
				}
			} else {
				value = "";
			}
			buf.append(entry.getKey() + "=" + value + "&");
		});
		buf.deleteCharAt(buf.length() - 1);
		String join = url.contains("?") ? "&" : "?";
		return url + join + buf.toString();
	}

	private static List<BasicNameValuePair> buildParamList(Map<String, String> params) {
		List<BasicNameValuePair> pairs = new ArrayList<>();
		if(null != params && params.size() > 0) {
			Set<String> keys = params.keySet();
			for (String key : keys) {
				pairs.add(new BasicNameValuePair(key, params.get(key)));
			}
		}
		return pairs;
	}

	/**
	 * @param url
	 * @param method
	 * @param callback
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @deprecated 推荐使用{@link HttpClientUtil#call(String, HttpMethod, Param)}
	 */
	@Deprecated
	public static <T> T call(String url, String method, Callback<T> callback) throws IOException {
		return call(url, method, null, null, CONNECT_TIME_OUT_IN_SECOND, SOCKET_TIME_OUT_IN_SECOND, callback);
	}

	/**
	 * @param url
	 * @param method
	 * @param params
	 * @param callback
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @deprecated 推荐使用{@link HttpClientUtil#call(String, HttpMethod, Param)}
	 */
	@Deprecated
	public static <T> T call(String url, String method, Map<String, String> params, Callback<T> callback) throws IOException {
		return call(url, method, params, null, CONNECT_TIME_OUT_IN_SECOND, SOCKET_TIME_OUT_IN_SECOND, callback);
	}

	/**
	 * @param url
	 * @param method
	 * @param params
	 * @param connectTimeoutInSecond
	 * @param socketTimeoutInSecond
	 * @param callback
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @deprecated 推荐使用{@link HttpClientUtil#call(String, HttpMethod, Param)}
	 */
	@Deprecated
	public static <T> T call(String url, String method, Map<String, String> params, int connectTimeoutInSecond, int socketTimeoutInSecond, Callback<T> callback) throws IOException {
		return call(url, method, params, null, connectTimeoutInSecond, socketTimeoutInSecond, callback);
	}

	/**
	 * @param url
	 * @param method
	 * @param params
	 * @param headers
	 * @param connectTimeoutInSecond
	 * @param socketTimeoutInSecond
	 * @param callback
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @deprecated 推荐使用{@link HttpClientUtil#call(String, HttpMethod, Param)}
	 */
	@Deprecated
	public static <T> T call(String url, String method, Map<String, String> params, Map<String, String> headers, int connectTimeoutInSecond, int socketTimeoutInSecond, Callback<T> callback) throws IOException {
		return call(url, method, params, headers, null, 0, connectTimeoutInSecond, socketTimeoutInSecond, callback);
	}

	/**
	 * 发起http请求
	 * @param url
	 * @param method
	 * @param optionalParams 可选参数
	 * @return
	 */
	public static String call(String url, HttpMethod method, Param optionalParams) throws IOException {
		Integer contentLength = optionalParams.getContentLength();
		Integer connectTimeoutInSecond = optionalParams.getConnectTimeoutInSecond();
		Integer socketTimeoutInSecond = optionalParams.getSocketTimeoutInSecond();
		return call(url,
				method.name(),
				optionalParams.getParams(),
				optionalParams.getHeaders(), null,
				null == contentLength ? 0 : contentLength,
				null == connectTimeoutInSecond ? CONNECT_TIME_OUT_IN_SECOND : connectTimeoutInSecond,
				null == socketTimeoutInSecond ? SOCKET_TIME_OUT_IN_SECOND : socketTimeoutInSecond,
				r -> {
					try {
						return EntityUtils.toString(r.getEntity(), Constants.UTF8);
					} catch (IOException e) {
						log.error("entity to string fail, url: {}, method: {}, params: {}", url, method, JSON.toJSONString(optionalParams), e);
						throw new RuntimeException(e);
					}
				}, optionalParams.isPostJson());
	}

	/**
	 * http call可选参数
	 */
	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Param {
		/**
		 * 参数。如果是get请求会添加到url后面
		 */
		private Map<String, String> params;

		private Map<String, String> headers;

		private Integer contentLength;

		/**
		 * 是否是post json方式，只有method为POST时才生效
		 */
		private boolean isPostJson;

		/**
		 * 连接超时时间
		 */
		private Integer connectTimeoutInSecond;

		/**
		 * 处理超时时间
		 */
		private Integer socketTimeoutInSecond;
	}

	/**
	 * @param url
	 * @param method
	 * @param params
	 * @param headers
	 * @param inputStream
	 * @param contentLength
	 * @param connectTimeoutInSecond
	 * @param socketTimeoutInSecond
	 * @param callback
	 * @param <T>
	 * @return
	 * @throws IOException
	 * @deprecated 推荐使用{@link HttpClientUtil#call(String, HttpMethod, Param)}
	 */
	@Deprecated
	public static <T> T call(String url, String method, Map<String, String> params, Map<String, String> headers, InputStream inputStream, int contentLength, int connectTimeoutInSecond, int socketTimeoutInSecond, Callback<T> callback) throws IOException {
		return call(url, method, params, headers, inputStream, contentLength, connectTimeoutInSecond, socketTimeoutInSecond, callback, false);
	}

	private static <T> T call(String url, String method, Map<String, String> params, Map<String, String> headers, InputStream inputStream, int contentLength, int connectTimeoutInSecond, int socketTimeoutInSecond, Callback<T> callback, boolean postJson) throws IOException {
		CloseableHttpClient httpClient = null;
		HttpUriRequest httpRequest = null;
		CloseableHttpResponse httpResponse = null;
		try {
			httpClient = newDefaultHttpClient(connectTimeoutInSecond, socketTimeoutInSecond);
			httpRequest = buildHttpRequest(url, method, params, inputStream, contentLength, postJson);
			addHeaders(httpRequest, headers);
			httpResponse = httpClient.execute(httpRequest);
			if (null != callback) {
				return callback.callback(httpResponse);
			}
			return (T) EntityUtils.toString(httpResponse.getEntity(), Constants.UTF8);
		} catch (IOException e) {
			log.debug("http call fail. url: {}, method: {}, params: {}, headers: {}, connectTimeOut: {}, socketTimeOut: {}",
					url, JSON.toJSONString(params), JSON.toJSONString(headers), connectTimeoutInSecond, socketTimeoutInSecond, e);
			throw e;
		} finally {
			if (null != httpResponse) {
				try {
					EntityUtils.consumeQuietly(httpResponse.getEntity());
				} catch (Exception e) {}
				try {
					httpResponse.close();
				} catch (Exception e) {}
			}
			if (null != httpRequest) {
				try {
					httpRequest.abort();
				} catch (Exception e) {}
			}
			if (null != httpClient) {
				try {
					httpClient.close();
				} catch (Exception e) {}
			}
		}
	}

	private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
		if (null == headers || headers.size() == 0) {
			return;
		}
		headers.entrySet().forEach(header -> httpRequest.addHeader(header.getKey(), header.getValue()));
	}

	private static HttpUriRequest buildHttpRequest(String url, String method, Map<String, String> params, InputStream inputStream, int contentLength) throws IOException {
		return buildHttpRequest(url, method, params, inputStream, contentLength, false);
	}

	private static HttpUriRequest buildHttpRequest(String url, String method, Map<String, String> params, InputStream inputStream, int contentLength, boolean postJson) throws IOException {
		/*
		String fullUrlWithParams = appendParamsToUrl(url, params);
		if (
				StringUtils.equals(HttpMethod.GET.name(), method) ||
				StringUtils.equals(HttpMethod.DELETE.name(), method) ||
				StringUtils.equals(HttpMethod.PUT.name(), method) ||
				StringUtils.equals(HttpMethod.OPTIONS.name(), method) ||
				StringUtils.equals(HttpMethod.HEAD.name(), method)
			) {
			return new HttpGet(fullUrlWithParams);
		} else if (StringUtils.equals(HttpMethod.POST.name(), method)) {
			List<BasicNameValuePair> paramList = buildParamList(params);
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(paramList, Constants.UTF8));
			return httpPost;
		} else {
			throw new IOException("UnsupportedMethod: " + method);
		}
		*/
		String fullUrlWithParams = appendParamsToUrl(url, params);
		if (StringUtils.equals(HttpMethod.POST.name(), method)) {
			HttpPost post = new HttpPost(url);
			if (postJson) {
				StringEntity entity = new StringEntity(JSON.toJSONString(params), ContentType.APPLICATION_JSON);
				post.setEntity(entity);
			} else {
				List<BasicNameValuePair> pairs = buildParamList(params);
				HttpEntity entity = new UrlEncodedFormEntity(pairs, Constants.UTF8);
				post.setEntity(entity);
			}
			if (null != inputStream) {
				HttpEntity entity = new InputStreamEntity(inputStream, contentLength);
				post.setEntity(entity);
			}
			return post;
		} else if (StringUtils.equals(HttpMethod.PUT.name(), method)) {
			HttpPut put = new HttpPut(url);
			List<BasicNameValuePair> pairs = buildParamList(params);
			HttpEntity entity = new UrlEncodedFormEntity(pairs, Constants.UTF8);
			put.setEntity(entity);
			if (null != inputStream) {
				entity = new InputStreamEntity(inputStream, contentLength);
				put.setEntity(entity);
			}
			return put;
		} else if (StringUtils.equals(HttpMethod.GET.name(), method)) {
			return new HttpGet(fullUrlWithParams);
		} else if (StringUtils.equals(HttpMethod.HEAD.name(), method)) {
			return new HttpHead(fullUrlWithParams);
		} else if (StringUtils.equals(HttpMethod.OPTIONS.name(), method)) {
			return new HttpOptions(fullUrlWithParams);
		} else if (StringUtils.equals(HttpMethod.DELETE.name(), method)) {
			return new HttpDelete(fullUrlWithParams);
		} else if (StringUtils.equals(HttpMethod.PATCH.name(), method)) {
			return new HttpPatch(fullUrlWithParams);
		} else if (StringUtils.equals(HttpMethod.TRACE.name(), method)) {
			return new HttpTrace(fullUrlWithParams);
		} else {
			throw new IOException("UnsupportedMethod: " + method);
		}
	}

	public static boolean isGzipSupport(String acceptEncoding) {
		return (acceptEncoding != null && (acceptEncoding.indexOf("gzip") != -1));
	}

	public static boolean isGzippedResponse(HttpResponse httpResponse) {
		if (null == httpResponse) {
			return false;
		}
		Header[] headers = httpResponse.getHeaders(Constants.HEADER_CONTENT_ENCODING);
		if (null == headers || headers.length == 0) {
			return false;
		}
		for (Header header : headers) {
			if (null != header && null != header.getValue() && (header.getValue().indexOf("gzip") != -1)) {
				return true;
			}
		}
		return false;
	}

	public interface Callback<T> {
		T callback(HttpResponse httpResponse);
	}

	public static void main(String[] args) {
		//使用样例

		String url = "https://www.baidu.com";
		String out = HttpClientUtil.get(url);
		System.out.println("step 1 -------------------------------");
		System.out.println(out);

		Map<String, String> params = new HashMap<>();
		params.put("a", "b");
		out = HttpClientUtil.post(url, params);
		System.out.println("step 2 -------------------------------");
		System.out.println(out);

		out = HttpClientUtil.postJson(url, params);
		System.out.println("step 3 -------------------------------");
		System.out.println(out);

		try {
			out = HttpClientUtil.call(url, HttpClientUtil.HttpMethod.GET, HttpClientUtil.Param
					.builder()
					.params(params)
					.headers(params)
					.contentLength(10000)
					.isPostJson(true)
					.connectTimeoutInSecond(3)
					.socketTimeoutInSecond(5)
					.build());
			System.out.println("step 4 -------------------------------");
			System.out.println(out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
