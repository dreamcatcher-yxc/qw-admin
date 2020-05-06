package com.qiwen.base.util.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * PICS 平台爬虫实现
 * @ClassName: PICSReptile
 * @Description: 
 * @Author xiuchu.yang
 * @DateTime 2018年12月26日 下午4:55:26
 */
public class HttpClientUtil {
	
	private final static Logger log = Logger.getLogger(HttpClientUtil.class.getName());
	
	private static final SimpleDateFormat GMT_SDF = new SimpleDateFormat("EEE MMM dd yyyy hh:mm:ss z", Locale.ENGLISH);
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private int timeout = 20000;
	
	private static final Gson GSON = new GsonBuilder().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	
	/**
	 * 封装 HttpResponse
	 * @ClassName: NGSNHttpResponse
	 * @Description: 
	 * @Author xiuchu.yang
	 * @DateTime 2018年12月26日 下午4:40:31
	 */
	public static class CustomHttpResponse extends BasicHttpResponse {
		
		private HttpResponse response = null;
		
		private StatusLine statusLine = null;
		
		private int statusCode = 0;
		
		private String defaultCharset = "UTF-8";
		
		private byte[] responseBody = null;
		
		public CustomHttpResponse(HttpResponse response) {
			this(response, "UTF-8");
		}
		
		public CustomHttpResponse(HttpResponse response, String defaultCharset) {
			super(response.getStatusLine());
			this.response = response;
			this.statusLine = response.getStatusLine();
			this.statusCode = this.statusLine.getStatusCode();
			this.defaultCharset = defaultCharset;
			
			if(this.statusCode == 200) {
				HttpEntity entity = this.response.getEntity();
				try {
					responseBody = EntityUtils.toByteArray(entity);
				} catch (ParseException | IOException e) {
					log.warning(String.format("获取 Response 响应体信息失败, cause: %s, message: %s", e.getCause(), e.getMessage()));
				}
			}
		}
		
		public <R> R if200(Function<CustomHttpResponse, R> function) {
			if(statusCode == 200) {
				return function.apply(this);
			} 
			return null;
		}
		
		public <R> R if302Or303(Function<CustomHttpResponse, R> function) {
			if(statusCode == 302 || statusCode == 303) {
				return function.apply(this);
			} 
			return null;
		}
		
		public <R> R if400(Function<CustomHttpResponse, R> function) {
			if(statusCode == 400) {
				return function.apply(this);
			} 
			return null;
		}
		

		public <R> R if401(Function<CustomHttpResponse, R> function) {
			if(statusCode == 401) {
				return function.apply(this);
			} 
			return null;
		}
		
		public <R> R if403(Function<CustomHttpResponse, R> function) {
			if(statusCode == 403) {
				return function.apply(this);
			} 
			return null;
		}
		
		public <R> R if404(Function<CustomHttpResponse, R> function) {
			if(statusCode == 404) {
				return function.apply(this);
			} 
			return null;
		}
		
		public <R> R if405(Function<CustomHttpResponse, R> function) {
			if(statusCode == 405) {
				return function.apply(this);
			} 
			return null;
		}
		
		public <R> R if500(Function<CustomHttpResponse, R> function) {
			if(statusCode == 500) {
				return function.apply(this);
			} 
			return null;
		}
		
		public <R> R iF(Function<CustomHttpResponse, R> function) {
			return function.apply(this);
		}
		
		public String[] getHeadersVals(String name) {
			Header[] headers = this.response.getHeaders(name);
			if(ArrayUtils.isEmpty(headers)) {
				return null;
			}
			String[] headerVals = new String[headers.length];
			int i = 0;
			for(Header header : headers) {
				headerVals[i++] = header.getValue();
			}
			return headerVals;
		}
		
		public String getHeaderVal(String name) { 
			Header header = this.response.getFirstHeader(name);
			return header != null ? header.getValue() : null;
		}
		
		public byte[] getBody() throws ParseException, IOException {
			if(this.statusCode != 200) {
				return null;
			}
			return this.responseBody;
		}
		
		public String getStringBody() throws ParseException, IOException {
			if(this.statusCode != 200) {
				return null;
			}
			return new String(this.responseBody, defaultCharset);
		} 
		
		public String getStringBody(String defaultCharset) throws ParseException, IOException {
			if(this.statusCode != 200) {
				return null;
			}
			return new String(this.responseBody, defaultCharset);
		} 
		
		public int getStatusCode() {
			return this.statusCode;
		}
	}


	/**
	 * 创建 http 请求配置
	 * @Title: createConfig
	 * @Description:
	 * @Author xiuchu.yang
	 * @DateTime 2018年12月25日 下午6:47:12
	 * @param timeout 超时时间, 单位 ms
	 * @param redirectsEnabled 当收到的响应式重定向请求的时候，是否自动重定向，httpClient 仅 GET 请求会自动重定向
	 * @return 请求配置
	 */
	private static RequestConfig createConfig(int timeout, boolean redirectsEnabled) {
		return RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout).setRedirectsEnabled(redirectsEnabled).build();
	}


	/**
	 * 创建 https 连接
	 * @Title: createSSLClientDefault
	 * @Description:
	 * @Author xiuchu.yang
	 * @DateTime 2018年12月25日 下午6:44:56
	 * @return
	 */
	private static  CloseableHttpClient createSSLClientDefault() {
		try {
			@SuppressWarnings("deprecation")
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException  e) {
			log.warning(String.format("创建非安全 https 连接失败, cause: %s, message: %s", e.getCause(), e.getMessage()));
		}
		return HttpClients.createDefault();
	}

    /**
     * 发送 GET 请求
     * @Title: httpsGet
     * @Description:
     * @Author xiuchu.yang
     * @DateTime 2018年12月26日 下午4:54:41
     * @param request
     * @param headerStrs
     * @return
     */
	private static void setHttpHeaders(HttpMessage request, String[] headerStrs) {
        if (!ArrayUtils.isEmpty(headerStrs)) {
            String splitReg = "\\s*=\\s*";
            for (String paramStr : headerStrs) {
                String[] singleParamArr = paramStr.split(splitReg);
                if (singleParamArr.length >= 2) {
                    request.setHeader(singleParamArr[0], singleParamArr[1]);
                }
            }
        }
    }
	
	/**
	 * 发送 GET 请求
	 * @Title: httpsGet
	 * @Description: 
	 * @Author xiuchu.yang
	 * @DateTime 2018年12月26日 下午4:54:41
	 * @param path
	 * @param headerStrs 请求头数组, 格式为: {"Content-Type=text/html"...}, 无请求头直接传递 null 即可
	 * @param paramStrs 表单数据, 格式为: {"Content-Type=text/html"...}, 无表单数据直接传递 null 即可
	 * @return
	 */
	public static CustomHttpResponse get(String path, String[] headerStrs, String[] paramStrs) {
		CloseableHttpClient httpClient = null;
		String splitReg = "\\s*=\\s*";
		String url = path;

		try {
			URIBuilder builder = new URIBuilder(url);

			// 参数设置
			if (!ArrayUtils.isEmpty(paramStrs)) {
				for (String paramStr : paramStrs) {
					String[] singleParamArr = paramStr.split(splitReg);
					if (singleParamArr.length >= 2) {
						builder.setParameter(singleParamArr[0], singleParamArr[1]);
					}
				}
			}

			// 构建请求体
			HttpGet request = new HttpGet(builder.build());
			// 取消自动重定向
			request.setConfig(createConfig(20000, false));
			// 设置请求头
            setHttpHeaders(request, headerStrs);

            // 创建请求
            if(url.startsWith("https://")) {
                httpClient = createSSLClientDefault();
            } else {
                httpClient = HttpClients.createDefault();
            }

			HttpResponse response = httpClient.execute(request);

			return new CustomHttpResponse(response);
		} catch (IOException | URISyntaxException e) {
			log.warning(String.format("GET 请求过程出错, cause: %s, message: %s", e.getCause(), e.getMessage()));
		} finally {
			try {
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
			}
		}

		return null;
	}
	
	/**
	 * 发送 POST 请求
	 * @Title: httpsPost
	 * @Description: 
	 * @Author xiuchu.yang
	 * @DateTime 2018年12月26日 下午4:52:38
	 * @param path
	 * @param headerStrs 请求头数组, 格式为: {"Content-Type=text/html"...}, 无请求头直接传递 null 即可
	 * @param paramStrs 表单数据, 格式为: {"Content-Type=text/html"...}, 无表单数据直接传递 null 即可
	 * @return
	 */
	public static CustomHttpResponse post(String path, String[] headerStrs, String[] paramStrs) {
		CloseableHttpClient httpClient = null;
		String splitReg = "\\s*=\\s*";
		String url = path;
		
		try {
			URIBuilder builder = new URIBuilder(url);

			// 构建请求体
			HttpPost request = new HttpPost(builder.build());
			// 取消自动重定向
			request.setConfig(createConfig(20000, false));
			// 设置请求头
            setHttpHeaders(request, headerStrs);

            boolean isFormEntity = true;
            if(!ArrayUtils.isEmpty(headerStrs)) {
                for (String headerStr : headerStrs) {
                    if(headerStr.toLowerCase().startsWith("content-type")) {
                        String contentType = headerStr.split(splitReg)[1];
                        if(ContentType.DEFAULT_TEXT.getMimeType().equals(contentType)
                                || ContentType.TEXT_XML.getMimeType().equals(contentType)
                                || ContentType.APPLICATION_JSON.getMimeType().equals(contentType)
                                || ContentType.APPLICATION_XML.getMimeType().equals(contentType)) {
                            isFormEntity = false;
                        }
                    }
                }
            }

			// 参数设置
            if(isFormEntity) {
                if (!ArrayUtils.isEmpty(paramStrs)) {
                    List<NameValuePair> params = new ArrayList<>();
                    for (String paramStr : paramStrs) {
                        String[] singleParamArr = paramStr.split(splitReg);
                        if (singleParamArr.length >= 2) {
                            String name = singleParamArr[0];
                            String value = singleParamArr[1];
                            NameValuePair param = new BasicNameValuePair(name, value);
                            params.add(param);
                        }
                    }
                    request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
                }
            } else {
                StringEntity entity = new StringEntity(StringUtils.join(paramStrs, ""), "UTF-8");
                request.setEntity(entity);
            }

			// 创建请求
            if(url.startsWith("https://")) {
                httpClient = createSSLClientDefault();
            } else {
                httpClient = HttpClients.createDefault();
            }

            HttpResponse response = httpClient.execute(request);

			return new CustomHttpResponse(response);
		} catch (IOException | URISyntaxException e) {
			log.warning(String.format("POST 请求过程出错, cause: %s, message: %s", e.getCause(), e.getMessage()));
		} finally {
			try {
				if (httpClient != null) {
					httpClient.close();
				}
			} catch (IOException e) {
			}
		}

		return null;
	}

}
