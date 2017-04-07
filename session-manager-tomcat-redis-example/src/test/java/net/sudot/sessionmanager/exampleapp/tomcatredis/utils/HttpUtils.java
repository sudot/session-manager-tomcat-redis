package net.sudot.sessionmanager.exampleapp.tomcatredis.utils;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求工具
 * Created by tangjialin on 2016-11-09 0009.
 */
public class HttpUtils {
	private Logger logger = LoggerFactory.getLogger(getClass());
	public static final HttpUtils utils = new HttpUtils();
	public static final int TIME_OUT = 1000 * 40;
	public static final String ENCODING = "UTF-8";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String METHOD_HEAD = "HEAD";
	public static final String METHOD_OPTIONS = "OPTIONS";
	public static final String METHOD_PUT = "PUT";
	public static final String METHOD_DELETE = "DELETE";
	public static final String METHOD_TRACE = "TRACE";

	/**
	 * 获取浏览器请求标识
	 * @param request
	 * @return 返回浏览器请求标识
	 */
	public String getUserAgent(HttpServletRequest request) {
		return request.getHeader("User-Agent");
	}

	/**
	 * 以Get方式发送HTTP请求
	 * @param url 请求完整地址
	 * @return
	 */
	public String sendGet(String url) {
		return sendGet(url, TIME_OUT);
	}

	/**
	 * 以Get方式发送HTTP请求
	 * @param url     请求完整地址
	 * @param timeout 超时时间.单位:毫秒
	 * @return
	 */
	public String sendGet(String url, int timeout) {
		return send(url, null, METHOD_GET, timeout, null).getContext();
	}

	/**
	 * 以POST方式发送HTTPS请求
	 * @param url  请求地址
	 * @param data 发送的数据
	 * @return
	 */
	public String sendPost(String url, String data) {
		return sendPost(url, data, TIME_OUT);
	}

	/**
	 * 以POST方式发送HTTPS请求
	 * @param url     请求地址
	 * @param data    发送的数据
	 * @param timeout 超时时间.单位:毫秒
	 * @return
	 */
	public String sendPost(String url, String data, int timeout) {
		return send(url, data, METHOD_POST, timeout, null).getContext();
	}

	/**
	 * 以POST方式发送HTTPS请求
	 * @param url       请求地址
	 * @param data      发送的数据
	 * @param sessionId 请求的sessionId.用于绑定用户身份信息
	 * @return
	 */
	public HttpResult sendPost(String url, String data, String sessionId) {
		return send(url, data, METHOD_POST, TIME_OUT, sessionId);
	}

	/**
	 * 发送HTTP请求
	 * @param url       请求地址
	 * @param data      发送内容
	 * @param method    请求类型.详见{@link HttpURLConnection#setRequestMethod(java.lang.String)}
	 * @return
	 */
	public String send(String url, String data, String method) {
		HttpResult result = send(url, data, method, TIME_OUT, null);
		return result.getContext();
	}

	/**
	 * 发送HTTP请求
	 * @param url       请求地址
	 * @param data      发送内容
	 * @param method    请求类型.详见{@link HttpURLConnection#setRequestMethod(java.lang.String)}
	 * @param sessionId 请求的sessionId
	 * @return
	 */
	public String send(String url, String data, String method, String sessionId) {
		HttpResult result = send(url, data, method, TIME_OUT, sessionId);
		return result.getContext();
	}

	/**
	 * 发送HTTP请求
	 * @param url       请求地址
	 * @param data      发送内容
	 * @param method    请求类型.详见{@link HttpURLConnection#setRequestMethod(java.lang.String)}
	 * @param timeout   超时时间.单位:毫秒
	 * @param sessionId 请求的sessionId.用于绑定用户身份信息
	 * @return 返回HttpResult.
	 */
	public HttpResult send(String url, String data, String method, int timeout, String sessionId) {
		HttpResult httpResult = new HttpResult();
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			connection = connect(url, data, method, timeout, sessionId);
			inputStream = connection.getInputStream();
			httpResult.setContext(IOUtils.toString(inputStream, ENCODING));
			sessionId = getSessionCookie(connection);
			httpResult.setSessionId(sessionId);
		} catch (IOException e) {
			logger.error("请求异常:{}", url, e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.close(connection);
		}
		return httpResult;
	}

	/**
	 * 获取服务器响应的session对应的Cookie信息
	 * @param connection HTTP连接对象
	 * @return 返回SESSIONID的cookie值
	 */
	protected String getSessionCookie(HttpURLConnection connection) {
		StringBuilder sessionId = new StringBuilder(200);
		Map<String, List<String>> headerFields = connection.getHeaderFields();
		if (headerFields == null || headerFields.isEmpty()) { return sessionId.toString(); }
		List<String> cookies = headerFields.get("Set-Cookie");
		if (cookies == null || cookies.isEmpty()) { return sessionId.toString(); }
		for (String cookie : cookies) {
			sessionId.append(", ").append(cookie);
		}
		sessionId.delete(0, 2);
		String key = "JSESSIONID=";
		int index = sessionId.indexOf(key);
		return index == -1 ? "" : sessionId.substring(index, index + key.length() + 32);
	}

	/**
	 * 发起连接
	 * @param url       请求地址
	 * @param data      发送内容
	 * @param method    请求类型.详见{@link HttpURLConnection#setRequestMethod(java.lang.String)}
	 * @param timeout   超时时间.单位:毫秒
	 * @param sessionId 请求的sessionId.用于绑定用户身份信息
	 * @return
	 */
	public HttpURLConnection connect(String url, String data, String method, int timeout, String sessionId) {
		HttpURLConnection connection = null;
		try {
			boolean isGet = "GET".equals(method);
			// GET请求,并且请求数据不为空
			if (isGet && data != null && !data.isEmpty()) {
				if (data.startsWith("?")) {
					data = data.substring(1, data.length());
				}
				url += (url.indexOf("?") == -1 ? "?" : "&") + data;
			}
			connection = (HttpURLConnection) new URL(url).openConnection();
			if (isGet) connection.setRequestMethod(method);
//			connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			if (sessionId != null) { connection.setRequestProperty("Cookie", sessionId); }
			if (!isGet && data != null && !data.isEmpty()) {
				byte[] bytes = data.getBytes(ENCODING);
				connection.setDoOutput(true);
				connection.setRequestProperty("Content-Length", String.valueOf(bytes.length));
				OutputStream os = null;
				try {
					os = connection.getOutputStream();
					os.write(bytes);
					os.flush();
					os.close();
				} finally {
					IOUtils.closeQuietly(os);
				}
			}
			return connection;
		} catch (IOException e) {
			logger.error("请求异常:{}", url, e);
		}
		return null;
	}

	/**
	 * 上传附件连接
	 * <h3>提交数据格式如下:</h3>
	 * <pre>
	 * --1476848718690
	 * Content-Disposition: form-data; name="参数名称"
	 *
	 * 参数值
	 * --1476848718690
	 * Content-Disposition: form-data; name="文件参数名称(file)"; filename="文件名称(point.json)"
	 * Content-Type:application/octet-stream
	 *
	 * [(文件内容)
	 * {"lng":114.075,"lat":22.547,"accuracy":5.00,"date":"2016-10-10 12:13:22"},
	 * {"lng":114.075,"lat":22.548,"accuracy":5.00,"date":"2016-10-10 12:25:22"}
	 * ]
	 * --1476848718690--
	 * </pre>
	 * @param url       请求地址
	 * @param fileMap   上传的文件列表
	 * @param data      上传的额外数据.格式:Map<参数名称,参数值>
	 * @param sessionId 请求的sessionId.用于绑定用户身份信息
	 * @return
	 */
	public HttpURLConnection formUploadConnection(String url, Map<String, File> fileMap, Map<String, String> data, String sessionId) {
		String BOUNDARY = String.valueOf(System.currentTimeMillis()); // BOUNDARY就是request头和上传文件内容的分隔符
		String LINE = "--";                                           // 分隔符标识位
		String BR = "\r\n";                                           // 换行符
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(TIME_OUT);
			conn.setReadTimeout(TIME_OUT);
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setRequestMethod(METHOD_POST);
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			if (sessionId != null) { conn.setRequestProperty("Cookie", sessionId); }

			conn.setDoOutput(true);
			OutputStream out = conn.getOutputStream();
			/**
			 // 添加上传参数begin:
			 // --xxxxxxxxxxxxxx(换行符)
			 // Content-Disposition: form-data; name="参数名"(换行符)
			 // (换行符)参数值(换行符)
			 // 添加上传参数end.
			 * 例如:
			 * --1476848718690
			 * Content-Disposition: form-data; name="参数名称"
			 *
			 * 参数值
			 */
			if (data != null && !data.isEmpty()) {
				StringBuilder stringBuilder = new StringBuilder();
				Iterator<Map.Entry<String, String>> iterator = data.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, String> entry = iterator.next();
					String entryKey = entry.getKey();
					String entryValue = entry.getValue();
					if (entryKey == null) { continue; }
					stringBuilder.append(LINE).append(BOUNDARY).append(BR);
					stringBuilder.append("Content-Disposition: form-data; name=\"").append(entryKey).append("\"").append(BR);
					stringBuilder.append(BR).append(entryValue).append(BR);
				}
				out.write(stringBuilder.toString().getBytes(ENCODING));
				out.flush();
			}

			/**
			 // 添加上传文件内容begin:
			 // --xxxxxxxxxxxxxx(换行符)
			 // Content-Disposition: form-data; name="参数名"; filename="文件名"(换行符)
			 // (换行符)文件内容(换行符)
			 // 添加上传文件内容end.
			 * 例如:
			 * --1476848718690
			 * Content-Disposition: form-data; name="文件参数名称(file)"; filename="文件名称(point.json)"
			 * Content-Type:application/octet-stream
			 *
			 * [(文件内容)
			 * {"lng":114.075,"lat":22.547,"accuracy":5.00,"date":"2016-10-10 12:13:22"},
			 * {"lng":114.075,"lat":22.548,"accuracy":5.00,"date":"2016-10-10 12:25:22"}
			 * ]
			 */
			if (fileMap != null && !fileMap.isEmpty()) {
				Iterator<Map.Entry<String, File>> iterator = fileMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, File> entry = iterator.next();
					String entryKey = entry.getKey();
					File file = entry.getValue();
					if (entryKey == null || file == null) { continue; }
					String filename = file.getName();
					String contentType = new MimetypesFileTypeMap().getContentType(file);
					if (contentType == null || contentType.isEmpty()) {
						contentType = "application/octet-stream";
					} else if (filename.endsWith(".png")) {
						contentType = "image/png";
					}
					StringBuilder fileStringBuilder = new StringBuilder(400);
					fileStringBuilder.append(LINE).append(BOUNDARY).append(BR);
					fileStringBuilder.append("Content-Disposition: form-data; name=\"").append(entryKey).append("\"; filename=\"").append(filename).append("\"").append(BR);
					fileStringBuilder.append("Content-Type:").append(contentType).append(BR);
					fileStringBuilder.append(BR);
					out.write(fileStringBuilder.toString().getBytes(ENCODING)); // 将文件内容前的内容写入流
					fileStringBuilder = null;
					InputStream in = null;
					try { // 文件内容
						in = new FileInputStream(file);
						IOUtils.copyLarge(in, out);
						out.write(BR.getBytes(ENCODING)); // 追加的换行符
						out.flush();
					} finally {
						IOUtils.closeQuietly(in);
					}
				}
			}
			// 结尾符
			// --xxxxxxxxxxxxxx--
			byte[] endData = (LINE + BOUNDARY + LINE).getBytes(ENCODING);
			out.write(endData);
			out.flush();
			IOUtils.closeQuietly(out);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
		return conn;
	}

	/**
	 * 以POST方式上传文件
	 * @param url       请求地址
	 * @param fileMap   上传的文件
	 * @param data      跟随文件上传的附带内容
	 * @param sessionId 请求的sessionId.用于绑定用户身份信息,为空不绑定
	 * @return 返回HttpResult.
	 */
	public HttpResult formUpload(String url, Map<String, File> fileMap, Map<String, String> data, String sessionId) {
		HttpResult httpResult = new HttpResult();
		HttpURLConnection connection = null;
		InputStream inputStream = null;
		try {
			connection = formUploadConnection(url, fileMap, data, sessionId);
			inputStream = connection.getInputStream();
			httpResult.setContext(IOUtils.toString(inputStream, ENCODING));
			httpResult.setSessionId(getSessionCookie(connection));
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage(), e);
		} finally {
			IOUtils.closeQuietly(inputStream);
			IOUtils.close(connection);
		}
		return httpResult;
	}

	/**
	 * 构造表单提交数据
	 * @param parameters 提交参数封装
	 * @param len        预估数据长度.(为了减少数组扩容拷贝开销,请指定合适的数据长度,尽量接近返回值长度)
	 * @return 返回参数构造后的结果
	 */
	public String formatFormParameters(Map<String, String> parameters, int len) {
		StringBuilder sb = new StringBuilder(len <= 256 ? 256 : len);
		Iterator<Map.Entry<String, String>> entryIterator = parameters.entrySet().iterator();
		while (entryIterator.hasNext()) {
			Map.Entry<String, String> entry = entryIterator.next();
			sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
		}
		return sb.toString();
	}

	/**
	 * HTTP请求结果
	 */
	public class HttpResult {
		/**
		 * 返回的SessionId.格式:JSESSIONID=SessionId
		 * 当且仅当要求返回时才会有该值存在
		 */
		private String sessionId;
		/** 请求的返回值 */
		private String context;

		public String getSessionId() {
			return sessionId;
		}

		public void setSessionId(String sessionId) {
			this.sessionId = sessionId;
		}

		public String getContext() {
			return context;
		}

		public void setContext(String context) {
			this.context = context;
		}
	}
}