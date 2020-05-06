package com.qiwen.base.config.interceptor;

import com.qiwen.base.config.web.TextPlainView;
import com.qiwen.base.exception.*;
import com.qiwen.base.util.Result;
import com.qiwen.base.util.http.HttpUtil;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice( basePackages = { "com.qiwen.base.controller", "com.qiwen.yjyx.controller" })
public class QWGlobalExceptionHandler {

    private static Map<Class, String> EXCEPTION_SHORT_NAME_ALIAS = new HashMap<>();

    // 抛出异常之前, 在 Request 中存入此属性
    public static final String EXCEPTION_ATTACH_ATTRIBUTE = "QWGlobalExceptionHandlerAttachData";

    static {
        EXCEPTION_SHORT_NAME_ALIAS.put(SQLException.class, "数据库异常");
        EXCEPTION_SHORT_NAME_ALIAS.put(BusinessException.class, "业务异常");
        EXCEPTION_SHORT_NAME_ALIAS.put(BadRequestException.class, "非法请求异常");
        EXCEPTION_SHORT_NAME_ALIAS.put(ServerRejectException.class, "拒绝访问异常");
        EXCEPTION_SHORT_NAME_ALIAS.put(NotFoundException.class, "资源访问异常");
        EXCEPTION_SHORT_NAME_ALIAS.put(AuthenticationException.class, "权限校验异常");
        EXCEPTION_SHORT_NAME_ALIAS.put(SystemException.class, "系统异常");
    }

    @Data
    @Builder
    public static class AttachData {
        // 附带数据
        private String attachData;

        // 需要答应日志的附带信息
        private String logAttachData;

        // 是否需要打印异常信息
        private boolean requiresPrintLog = true;
    }
  
    @ExceptionHandler(SQLException.class)
    public ModelAndView handleSQLException(HttpServletRequest request, HttpServletResponse response, SQLException ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }

    @ExceptionHandler(BusinessException.class)
    public ModelAndView handleBusinessException(HttpServletRequest request, HttpServletResponse response, BusinessException ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }

    @ExceptionHandler(BadRequestException.class)
    public ModelAndView handleBadRequestException(HttpServletRequest request, HttpServletResponse response, BadRequestException ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }  
  
    @ExceptionHandler(ServerRejectException.class)
    public ModelAndView handleServerRejectException(HttpServletRequest request, HttpServletResponse response, ServerRejectException ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }  
  
    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handleNotFoundException(HttpServletRequest request, HttpServletResponse response, NotFoundException ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }  
  
    @ExceptionHandler(SystemException.class)
    public ModelAndView handleSystemException(HttpServletRequest request, HttpServletResponse response, SystemException ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }

    /**
     * 处理传参校验
     */
    @ExceptionHandler(BindException.class)
    public ModelAndView handleMethodArgumentNotValidException(
            HttpServletRequest request,
            HttpServletResponse response,
            BindingResult bindingResult,
            BindException ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, bindingResult, ex);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ModelAndView handleAuthenticationException(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }
  
    @ExceptionHandler(Exception.class)  
    public ModelAndView handleAllException(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
        logExceptionInfo(request, ex);
        return toJsonOrView(request, response, ex);
    }

    private void logExceptionInfo(HttpServletRequest request, Throwable ex) {
        AttachData attachData = getExceptionAttachData(request);

        // 需要打印异常详情
        if(attachData == null || attachData.requiresPrintLog) {
            StringBuffer sb = new StringBuffer();
            sb.append("原因: " + ExceptionUtils.getRootCause(ex) + "\r\n");
            sb.append("消息: " + ExceptionUtils.getRootCauseMessage(ex) + "\r\n");
            sb.append("堆栈信息: " + ExceptionUtils.getStackTrace(ex));
            log.error(sb.toString());
        }

        // 有异常自定义详情
        if(attachData != null && !StringUtils.isEmpty(attachData.logAttachData)) {
            log.error(attachData.logAttachData);
        }
    }

    /**
     * 获取附带信息
     * @param request
     */
    private AttachData getExceptionAttachData(HttpServletRequest request) {
        Object val = request.getAttribute(EXCEPTION_ATTACH_ATTRIBUTE);
        AttachData attachData = null;
        if(val instanceof AttachData) {
            attachData = (AttachData) val;
        }
        return attachData;
    }

    public ModelAndView toJsonOrView(
            HttpServletRequest request,
            HttpServletResponse response,
            BindingResult bindingResult,
            Exception ex) throws IOException {
        ModelAndView modelAndView;

        if(HttpUtil.isAjaxRequest(request)) {
            List<ObjectError> allErrors = bindingResult.getAllErrors();

            if (!CollectionUtils.isEmpty(allErrors)) {
                List<String> errors = allErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList());
                return new ModelAndView(
                        new TextPlainView(
                                Result.valError("参数校验失败")
                                        .putData("errors", errors)
                                        .json()
                        )
                );
            }

            return new ModelAndView(
                    new TextPlainView(
                            Result.valError("参数校验失败").json()
                    )
            );
        }

        return new ModelAndView("/error/5xx");
    }

//    private final Pattern payloadRegexPattern = Pattern.compile("\\{\\{\\s*([^\\}]*?)\\s*\\}\\}");
//
//    /**
//     * 从 Exception 上获取附加信息 {{ value }}
//     * @return
//     */
//    private String getPayloadFromExceptionMessage(String message) {
//        Matcher matcher = payloadRegexPattern.matcher(message);
//        if(matcher.find()) {
//            return matcher.group(1);
//        }
//        return null;
//    }
//
//    private String removePayloadOnExceptionMessage(String message) {
//        Matcher matcher = payloadRegexPattern.matcher(message);
//        if(matcher.find()) {
//            String group = matcher.group();
//            int start = matcher.start();
//            int end = matcher.end();
//            if(end >= message.length()) {
//                return message.substring(0, start + 1);
//            } else {
//                return message.substring(0, start) + message.substring(end);
//            }
//        }
//        return message;
//    }

    public ModelAndView toJsonOrView(HttpServletRequest request, HttpServletResponse response, Exception ex) throws IOException {
            ModelAndView modelAndView;
            Throwable rootCauseException = ExceptionUtils.getRootCause(ex);
            rootCauseException = rootCauseException == null ? ex : rootCauseException;
            boolean isAuthenticationException = rootCauseException instanceof AuthenticationException;

            if(HttpUtil.isAjaxRequest(request)) {
                String rootCauseMsg = ex == null ? "系统异常" : ExceptionUtils.getMessage(rootCauseException);
                String[] tArr = rootCauseMsg.split("\\s*\\:\\s*");

                if(tArr.length > 1) {
                    String exShortClsName = null;
                    for (Class exClazz : EXCEPTION_SHORT_NAME_ALIAS.keySet()) {
                        if(exClazz.isAssignableFrom(rootCauseException.getClass())) {
                            exShortClsName = EXCEPTION_SHORT_NAME_ALIAS.get(rootCauseException.getClass());
                        }
                    }
                    rootCauseMsg = exShortClsName == null ? tArr[1] : (exShortClsName + ":" + tArr[1]);
                }

                // 处理错误信息
                Result error = Result.error(rootCauseMsg);
                error.setState(Result.AUTHENTICATION_ERROR);
                AttachData exceptionAttachData = getExceptionAttachData(request);
                String payloadFromExceptionMessage = exceptionAttachData != null ? exceptionAttachData.getAttachData() : null;

                if(payloadFromExceptionMessage != null) {
                    error.setDesc(rootCauseMsg);
                    if("@RequiresAuthentication".equals(payloadFromExceptionMessage)) {
                        error.put("error_code", "403_1");
                    } else if("@RequiresUser".equals(payloadFromExceptionMessage)) {
                        error.put("error_code", "403_2");
                    } else if("@RequiresGuest".equals(payloadFromExceptionMessage)){
                        error.put("error_code", "403_3");
                    } else if("@RequiresPermissions".equals(payloadFromExceptionMessage)){
                        error.put("error_code", "403_4");
                    } else if("@RequiresRoles".equals(payloadFromExceptionMessage)){
                        error.put("error_code", "403_5");
                    }
                }
                modelAndView = new ModelAndView(new TextPlainView(error.json()));
            } else {
                if(isAuthenticationException) {
                    modelAndView = new ModelAndView("/error/403");
                } else {
                    modelAndView = new ModelAndView("/error/5xx");
                }
            }
            return modelAndView;
    }
}