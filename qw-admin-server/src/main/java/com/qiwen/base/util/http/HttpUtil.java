package com.qiwen.base.util.http;

import com.qiwen.base.exception.SystemException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public class HttpUtil {

    private static final  String UNKONWN = "unknown";

    public static String getRemoteHost(){
        return HttpUtil.getRequest().getRemoteHost();
    }

    public static String getIpAddress() {
        HttpServletRequest request = HttpUtil.getRequest();

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKONWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKONWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKONWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || UNKONWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || UNKONWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    public static Object getHttpSessionAttribute(String attributeName) {

        HttpSession session = HttpUtil.getRequest().getSession();
        return session.getAttribute(attributeName);
    }
    /**
     * 获取所有请求的值
     */
    public static Map<String, String> getRequestParameters() {
        HashMap<String, String> values = new HashMap<>();
        HttpServletRequest request = HttpUtil.getRequest();
        Enumeration enums = request.getParameterNames();
        while ( enums.hasMoreElements()){
            String paramName = (String) enums.nextElement();
            String paramValue = request.getParameter(paramName);
            values.put(paramName, paramValue);
        }
        return values;
    }

    /**
     * 获取 HttpServletRequest
     */
    public static HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
    }

    /**
     * 获取 包装防Xss Sql注入的 HttpServletRequest
     * @return request
     */
    public static HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return new WafRequestWrapper(request);
    }

    public static URLConnection generateUrlConnection(String urlNameString) {
        URLConnection connection;
        try {
            URL realUrl = new URL(urlNameString);

            connection = realUrl.openConnection();

            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        } catch (Exception e) {
            log.error("创建URLConnection失败:{}", e.getMessage(), e);
            throw new SystemException("创建URLConnection失败");
        }

        return connection;
    }

    public static String combineUrlParam(String url, Map<String, String> paramMap) {
        final StringBuilder paramStr = new StringBuilder();

        if(!CollectionUtils.isEmpty(paramMap)) {
            paramMap.forEach((key, value) -> paramStr.append(key + "=" + value).append("&"));
            paramStr.deleteCharAt(paramStr.length()-1);
        }

        return (StringUtils.isEmpty(url) ? "": url) + (!CollectionUtils.isEmpty(paramMap) ? "?" + paramStr.toString() : "");
    }

    public static String readInputStream(InputStream inputStream) {
        StringBuilder result = new StringBuilder();

        try(BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            log.error("InputStream流读取失败:{}", e.getMessage(), e);
            throw new SystemException("InputStream流读取失败");
        }

        return result.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url             发送请求的 URL
     * @param param           请求参数
     * @param throwException  是否抛出异常
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, Map<String, String> param, boolean throwException) {
        String result = "";
        try{
            String  completeUrl = combineUrlParam(url, param);

            URLConnection conn = generateUrlConnection(completeUrl);

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // 获取URLConnection对象对应的输出流
            try(PrintWriter out = new PrintWriter(conn.getOutputStream())) {
                // 发送请求参数
                out.print(param);
                // flush输出流的缓冲
                out.flush();
            }

            result = readInputStream(conn.getInputStream());
        } catch (Exception e) {
            log.info("发送 POST 请求出现异常！", e);

            if(throwException) {
                throw new SystemException("发送 POST 请求出现异常！");
            }
        }

        return result;
    }

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url             发送请求的 URL
     * @param param           请求参数
     * @param throwException  是否抛出异常
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url, Map<String, String> param, boolean throwException) {
        String result = "";
        try{
            String  completeUrl = combineUrlParam(url, param);

            URLConnection connection = generateUrlConnection(completeUrl);

            connection.connect();

            Map<String, List<String>> headerFields = connection.getHeaderFields();
            headerFields.forEach((key,value) -> log.info(key + " : " + value) );

            result = readInputStream(connection.getInputStream());
        } catch (Exception e) {
            log.info("发送GET请求出现异常！", e);
            if(throwException) {
                throw new SystemException("发送GET请求出现异常！");
            }
        }
        return result;
    }

    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url 发送请求的URL
     * @param param 请求参数
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGetHasException(String url, Map<String, String> param) {
        return sendGet(url, param, true);
    }

    public static String sendPostHasException(String url, Map<String, String> param) {
        return sendPost(url, param, true);
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String header = request.getHeader("X-Requested-With");
        return StringUtils.isNotBlank(header) && (header.equalsIgnoreCase("X-Requested-With") || header.equalsIgnoreCase("XMLHttpRequest"));
    }

    /**
     * 直接返回 JSON 数据.
     * @param response
     * @param jsonStr
     */
    public static void responseJsonData(HttpServletResponse response, String jsonStr) {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.write(jsonStr);
        } catch (IOException e) {
            log.error("IO错误", e);
        }
    }

    /**
     * 判断是否是移动端微信客户端发起的请求
     * @param request
     * @return
     */
    public static boolean isMobileWeChatClient(HttpServletRequest request) {
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            log.info("{} => {}", name, request.getHeader(name));
        }
        String userAgent = request.getHeader("user-agent").toLowerCase();
        return userAgent.contains("micromessenger");
    }

    /**
     * 判断是否是移动端微信客户端发起的请求
     * @param request
     * @return
     */
    public static boolean isPCWeChatClient(HttpServletRequest request) {
//        Enumeration<String> names = request.getHeaderNames();
//        while (names.hasMoreElements()) {
//            String name = names.nextElement();
//            log.info("{} => {}", name, request.getHeader(name));
//        }
        String userAgent = request.getHeader("user-agent").toLowerCase();
        return userAgent.contains("micromessenger") && userAgent.contains("windowwechats");
    }
}
