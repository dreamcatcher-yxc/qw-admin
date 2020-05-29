package com.qiwen.base.util;

import com.qiwen.base.entity.OperateLog;
import com.qiwen.base.entity.User;
import com.qiwen.base.util.http.HttpUtil;
import com.qiwen.base.vo.LoginUserVO;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

/**
 * 抽取 Controller 中日常需要使用到的工具类方法
 */
public class ControllerUtil {

    private ControllerUtil() {}

    public static final String DEFAULT_SPLIT_REGEX = "\\s*\\,\\s*";

    /**
     *  字符串转数组
     * @param str
     * @param converter
     * @param <T>
     * @return
     */
    public static <T> T[]  splitStr2Array(String str, Function<String, T> converter) {
        return splitStr2Array(str, null, converter);
    }

    /**
     *  字符串转集合
     * @param str
     * @param converter
     * @param <T>
     * @return
     */
    public static <T> List<T> splitStr2List(String str, Function<String, T> converter) {
        T[] arr = splitStr2Array(str, null, converter);
        if(arr == null) {
            return null;
        }
        return Arrays.asList(arr);
    }

    /**
     *  字符串转数组
     * @param str
     * @param splitRegex
     * @param converter
     * @param <T>
     * @return
     */
    public static <T> T[]  splitStr2Array(String str, String splitRegex, Function<String, T> converter) {
        if(StringUtils.isEmpty(str)) {
            return null;
        }

        if(splitRegex == null) {
            splitRegex = DEFAULT_SPLIT_REGEX;
        }

        String[] arr = str.split(splitRegex);
        T[] res = null;
        int index = 0;
        int size = arr.length;

        for (String ele : arr) {
            T t = converter.apply(ele);

            if(res == null) {
                if(t instanceof Long) {
                    res = (T[]) Array.newInstance(Long.class, size);
                } else if(t instanceof Integer) {
                    res = (T[]) Array.newInstance(Integer.class, size);
                } else if(t instanceof Short) {
                    res = (T[]) Array.newInstance(Short.class, size);
                } else if(t instanceof Byte) {
                    res = (T[]) Array.newInstance(Byte.class, size);
                } else if(t instanceof Float) {
                    res = (T[]) Array.newInstance(Float.class, size);
                } else if(t instanceof Double) {
                    res = (T[]) Array.newInstance(Double.class, size);
                } else if(t instanceof Boolean) {
                    res = (T[]) Array.newInstance(Boolean.class, size);
                } else if(t instanceof Character) {
                    res = (T[]) Array.newInstance(Character.class, size);
                } else if(t instanceof String) {
                    res = (T[]) Array.newInstance(String.class, size);
                } else if(t instanceof Date) {
                    res = (T[]) Array.newInstance(Date.class, size);
                } else {
                    String msg = "类型仅支持[Long, Integer, Short, Boolean, Float, Double, Character, Byte, String, Date]";
                    throw new TypeNotPresentException(msg, new RuntimeException(msg));
                }
            }

            res[index++] = t;
        }

        return res;
    }

    /**
     * 生成 App 端日志对象.
     * @param request
     * @param operation
     * @param duration
     * @param operationMethod
     * @param operationParam
     * @param result
     * @param message
     */
    public static OperateLog newAppOperateLogBean(HttpServletRequest request,
                                                  String operation,
                                                  Integer duration,
                                                  String operationMethod,
                                                  Map<String, Object> operationParam,
                                                  String result,
                                                  String message) {
        String realOperation = StringUtils.isEmpty(operation) ? "" : operation;
        String operatorName = "";
        Date operationTime = new Date();
        Integer realDuration = duration == null ? 0 : duration;
        String ip = HttpUtil.getIpAddress();
        String realOperationMethod = operationMethod == null ? "" : operationMethod;
        String realResult = result == null ? "S" : result;
        String realMessage = message == null ? "" : message;
        operationParam = operationParam == null ? new LinkedHashMap<>() : operationParam;

        if(request != null) {
            operationParam.put("app-uuid", request.getHeader("nbgyh-client-id"));
            operatorName = request.getHeader("nbgyh-client-name");
        }

        OperateLog ol = new OperateLog();
        ol.setOperation(realOperation);
        ol.setOperatorId(-1L);
        ol.setOperatorName(operatorName == null ? "unknown" : operatorName);
        ol.setOperateTime(operationTime);
        ol.setDuration(realDuration);
        ol.setIp(ip);
        ol.setOperateMethod(realOperationMethod);
        ol.setOperateParam(GsonUtil.toJson(operationParam));
        ol.setResult(realResult);
        ol.setMessage(realMessage);

        return ol;
    }

    /**
     * 生成服务器端日志对象.
     * @param operation
     * @param duration
     * @param operationMethod
     * @param operationParam
     * @param result
     * @param message
     */
    public static OperateLog newAdminOperateLogBean(String operation,
                                                  Integer duration,
                                                  String operationMethod,
                                                  Map<String, Object> operationParam,
                                                  String result,
                                                  String message) {
        String realOperation = StringUtils.isEmpty(operation) ? "" : operation;
        Long operatorId = -1L;
        String operatorName = "";
        Date operationTime = new Date();
        Integer realDuration = duration == null ? 0 : duration;
        String ip = HttpUtil.getIpAddress();
        String realOperationMethod = operationMethod == null ? "" : operationMethod;
        String realResult = result == null ? "S" : result;
        String realMessage = message == null ? "" : message;
        operationParam = operationParam == null ? new LinkedHashMap<>() : operationParam;

        LoginUserVO user = SystemUtil.currentLoginUser();
        if(user != null) {
            operatorId = user.getId();
            operatorName = SystemUtil.getUserName();
        }

        OperateLog ol = new OperateLog();
        ol.setOperation(realOperation);
        ol.setOperatorId(operatorId);
        ol.setOperatorName(operatorName);
        ol.setOperateTime(operationTime);
        ol.setDuration(realDuration);
        ol.setIp(ip);
        ol.setOperateMethod(realOperationMethod);
        ol.setOperateParam(GsonUtil.toJson(operationParam));
        ol.setResult(realResult);
        ol.setMessage(realMessage);

        return ol;
    }
}
