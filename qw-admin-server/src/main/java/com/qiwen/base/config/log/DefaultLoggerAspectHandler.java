package com.qiwen.base.config.log;

import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.OperateLog;
import com.qiwen.base.util.GsonUtil;
import com.qiwen.base.util.ReflectUtil;
import com.qiwen.base.util.StringUtil;
import com.qiwen.base.util.SystemUtil;
import com.qiwen.base.util.http.HttpUtil;
import com.qiwen.base.vo.LoginUserVO;
import com.qiwen.base.vo.OperateLogVO;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.util.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.util.MapUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * 提供日志读取、打印、存储公共逻辑。
 */
public class DefaultLoggerAspectHandler {

    private Logger log;

    public DefaultLoggerAspectHandler(Logger log) {
        this.log = log;
    }

    @Data
    @Builder
    private static class QWDescMetaInfo {
        private boolean requiredLog = true;
        private boolean requiredCustom;
        private boolean requiredSaveToDB = false;
        private String otherInfo;
    }

    public Object aroundHandler(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        Exception exception = null;
        OperateLogVO operateLogVO = null;

        QWDescMetaInfo descMetaInfo = getDescMetaInfo(joinPoint);
        if(descMetaInfo.requiredLog && descMetaInfo.requiredCustom) {
            operateLogVO = generateLog(joinPoint);
            HttpServletRequest httpRequest = WebUtils.getHttpRequest(SecurityUtils.getSubject());
            httpRequest.setAttribute(Desc.CUSTOM_ATTR_NAME, operateLogVO);
        }

        Instant startTime = Instant.now();

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error("\n异常方法: {} \n 异常代码:{} \n 异常信息:{} \n 参数:{}",
                    getProxyTargetSignatureInfo(joinPoint),
                    e.getClass().getName(),
                    getExceptionMessage(e),
                    joinPoint.getArgs());
            //抛出异常，交后续处理
            exception = e;
        }

        Long duration = Duration.between(startTime, Instant.now()).toMillis();
        log.info("执行 {} 方法耗时: {} (毫秒)", getProxyTargetSignatureInfo(joinPoint), duration);

        if(operateLogVO == null && descMetaInfo.requiredLog) {
            operateLogVO = generateLog(joinPoint);
        }

        if(operateLogVO != null) {
            printOperateLog(operateLogVO);
            if(descMetaInfo.requiredSaveToDB) {
                saveOperationLog(operateLogVO, Date.from(startTime), duration, exception);
            }
        } else {
            log.trace(descMetaInfo.otherInfo);
        }

        // 有异常, 则抛出
        if (exception != null) {
            throw exception;
        }

        return result;
    }

    /**
     * 在控制台打印操作日志信息
     * @param operateLogVO
     */
    private void printOperateLog(OperateLogVO operateLogVO) {
        StringBuilder logInfo = new StringBuilder();
        logInfo.append("\n操作描述: ")
                .append(operateLogVO.getOperation())
                .append("\n请求方法: ")
                .append(operateLogVO.getOperateMethod())
                .append("\n请求IP: ")
                .append(operateLogVO.getIp())
                .append("\n请求参数: ")
                .append(GsonUtil.toJson(operateLogVO.getOperateParam(), true));
        log.info(logInfo.toString());
    }

    /**
     * 组合目标签名信息
     * @param point
     * @return
     */
    private String getProxyTargetSignatureInfo(ProceedingJoinPoint point) {
        return point.getTarget().getClass().getName() + "." + point.getSignature().getName();
    }

    /**
     * 保存操作日志
     * @param operateLogVO
     * @param startTime
     * @param duration
     * @param exception
     * @throws Exception
     */
    private void saveOperationLog(OperateLogVO operateLogVO, Date startTime, Long duration, Exception exception) {
        OperateLog operateLog = ReflectUtil.O2ObySameField(operateLogVO, OperateLog.class);
        Map<String, Object> operateParam = operateLogVO.getOperateParam();
        if(operateParam != null) {
            operateLog.setOperateParam(GsonUtil.toJson(operateParam, false));
        }

        if (null != operateLogVO) {
            operateLog.setOperateTime(startTime);
            operateLog.setDuration(duration.intValue());

            if (null == exception) {
                operateLog.setResult("S");
                operateLog.setMessage("成功");
            } else {
                operateLog.setResult("F");
                operateLog.setMessage(getExceptionMessage(exception));
            }

            try {
                LogManager.me().executeLog(LogTaskFactory.bussinessLog(operateLog));
            } catch (Exception e) {
                log.error("日志保存错误：{}", e.getMessage());
            }
        }
    }

    /**
     * 获取必要的日志打印元信息
     * @param joinPoint
     * @return
     * @throws NoSuchMethodException
     */
    private QWDescMetaInfo getDescMetaInfo(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
        Object target = joinPoint.getTarget();
        Desc classDesc = target.getClass().getAnnotation(Desc.class);
        Boolean flag = true;
        String msg = "";

        if (classDesc != null && !classDesc.requiredLog()) {
            flag = false;
            msg = null;
        }

        if(!flag) {
            return QWDescMetaInfo.builder()
                    .requiredLog(false)
                    .otherInfo(msg)
                    .build();
        }

        Signature sig = joinPoint.getSignature();

        if (!(sig instanceof MethodSignature)) {
//            throw new IllegalArgumentException("该注解只能用于方法");
            return QWDescMetaInfo.builder()
                    .requiredLog(false)
                    .otherInfo(msg)
                    .build();
        }

        String methodName = joinPoint.getSignature().getName();
        MethodSignature msig = (MethodSignature) sig;
        Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
        Desc methodDesc = currentMethod.getAnnotation(Desc.class);

        if (methodDesc == null || !methodDesc.requiredLog()) {
            //没有定义Desc则不记录日志
            flag = false;
            msg = String.format("%s > %s 不需要记录日志", target.getClass().getName(), methodName);
        }

        return QWDescMetaInfo.builder()
                .requiredLog(flag)
                .requiredCustom(methodDesc != null ? methodDesc.customLog() : false)
                .requiredSaveToDB(methodDesc != null ? methodDesc.requiredSaveToDB() : false)
                .otherInfo(msg)
                .build();
    }

    /**
     * 生成日志信息
     * @param joinPoint
     * @return
     */
    private OperateLogVO generateLog(ProceedingJoinPoint joinPoint) {
        OperateLogVO operateLogVO = null;

        try {
            String ip = HttpUtil.getIpAddress();
            LoginUserVO user = SystemUtil.currentLoginUser();
            String userName, nickName;
            Long userId = -1L;

            if(user != null) {
                nickName = user.getNickname();
                userName = StringUtils.isBlank(nickName) ? user.getUsername() : nickName;
                userId = user.getId();
            } else {
                userName = "未知用户";
            }

            //获取拦截的方法名
            Signature sig = joinPoint.getSignature();
            MethodSignature msig = (MethodSignature) sig;
            Object target = joinPoint.getTarget();
            Method currentMethod = target.getClass().getMethod(msig.getName(), msig.getParameterTypes());
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();

            // 获取类上的日志相关信息.
            String classDescription = "";
            Desc classDesc = target.getClass().getAnnotation(Desc.class);

            if (null != classDesc) {
                classDescription = StringUtil.getFirstNotEmptyStr(classDesc.logGroupName(), classDesc.value(), className);
            }

            // 获取方法日志信息.
            Desc methodDesc = currentMethod.getAnnotation(Desc.class);
            String methodDescription = StringUtil.getFirstNotEmptyStr(classDesc.log(), classDesc.value(), methodName);

            Map<String, Object> params = new LinkedHashMap<>();

            if (canToJson(joinPoint.getArgs())) {
                params = getArgs(joinPoint, currentMethod);
                // params 参数过滤.
                params = filterMapData(params, methodDesc.logIncludeParams(), methodDesc.logExcludeParams());
                // HttpServletRequest 里面的参数过滤.
                String requestParamsKey = "HttpServletRequest";
                Map<String, Object> newRequestParameters = filterMapData((Map<String, Object>) params.get(requestParamsKey), methodDesc.logIncludeParams(),  methodDesc.logExcludeParams());
                Object fooVar = newRequestParameters != null ? params.replace(requestParamsKey, newRequestParameters) : params.remove(requestParamsKey);
            }

            // 构建访问日志纪录
            operateLogVO = new OperateLogVO();
            operateLogVO.setOperation(classDescription + " > " + methodDescription);
            operateLogVO.setOperatorName(userName);
            operateLogVO.setOperatorId(userId);
            operateLogVO.setIp(ip);
            operateLogVO.setOperateMethod(className + "." + methodName);
            operateLogVO.setOperateParam(params);
        } catch (Exception e) {
            log.error("\n打印控制器信息错误:: 异常类: {} \n 异常方法:{} \n 异常信息:{} \n}",
                    joinPoint.getClass().getName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage());
        }

        return operateLogVO;
    }

    /**
     * 从 map 中过滤不需要的内容
     * @param srcMap 元数据映射集
     * @param includeKeys 包含的 key 集合
     * @param excludeKeys 不包含的 key 集合
     * @return
     */
    private Map<String, Object> filterMapData(Map<String, Object> srcMap, String[] includeKeys, String[] excludeKeys) {
        if(MapUtils.isEmpty(srcMap)) {
            return null;
        }
        Map<String, Object> newMap = new HashMap(srcMap.size());
        if (!ArrayUtils.isEmpty(includeKeys)) {
            List<String> includeList = Arrays.asList(includeKeys);
            srcMap.forEach((key, val) -> {
                if (!includeList.contains(key)) {
                    log.trace("{} => {} 不计入操作日志", key, val);
                    return;
                }
                newMap.put(key, val);
            });
        } else if (!ArrayUtils.isEmpty(excludeKeys)) {
            List<String> excludeList = Arrays.asList(excludeKeys);
            srcMap.forEach((key, val) -> {
                if (excludeList.contains(key)) {
                    log.info("{} => {} 不计入操作日志", key, val);
                    return;
                }
                newMap.put(key, val);
            });
        } else {
            return srcMap; // 不用过滤
        }
        return newMap;
    }

    private Map<String, Object> getArgs(ProceedingJoinPoint joinPoint, Method currentMethod) {
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = null;

        try {
            parameterNames = ((MethodSignature) (joinPoint.getSignature())).getParameterNames();
        } catch (Exception e) {
            log.error("{} 方法获取参数名称失败!", currentMethod.getName());
        }

        Parameter[] parameters = currentMethod.getParameters();
        Map<String, Object> params = new HashMap<>();
        params.put("HttpServletRequest", HttpUtil.getRequestParameters());
        Map<String, Object> methodParams = new HashMap<>();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (!(arg instanceof BindingResult)
                    && !(arg instanceof ModelMap)
                    && !(arg instanceof Model)
                    && !(arg instanceof HttpServletRequest)
                    && !(arg instanceof HttpSession)
                    && !(arg instanceof HttpServletResponse)
                    && !(arg instanceof MultipartFile)
                    && !(arg instanceof MultipartFile[])) {
                String paramName = getParameterNameByIndex(currentMethod, parameterNames, parameters, i);
                methodParams.put(paramName, arg);
            }
        }

        params.put(currentMethod.getName(), methodParams);
        return params;
    }

    // 获取参数名称
    private String getParameterNameByIndex(Method currentMethod, String[] parameterNames, Parameter[] parameters, int i) {
        Annotation[][] paramAnns = currentMethod.getParameterAnnotations();
        String paramName = "";
        Annotation[] tArr = i < paramAnns.length ? paramAnns[i] : null;
        Annotation ta = null;

        if(!ArrayUtils.isEmpty(tArr)) {
            // 查找每个方法上的 RequestMapping 或者 PathVariable 上的 name 属性,
            // 如果上述的属性查找不到, 则直接去当前参数名的名字.
            for(int x = 0; x < tArr.length; x++) {
                ta = tArr[x];
                if(x !=  tArr.length - 1) {
                    // ta 没有指向该参数上的最后一个注解.
                    paramName = ta instanceof RequestParam ?
                            ((RequestParam)ta).name() : ta instanceof PathVariable ?
                            ((PathVariable)ta).name() : "";
                } else {
                    paramName = ta instanceof RequestParam ?
                            ((RequestParam)ta).name() : ta instanceof PathVariable ?
                            ((PathVariable)ta).name() : parameterNames != null && parameterNames.length > i ?
                            parameterNames[i] : "";
                }
                // 尽量找到方法方法该位置的名称.
                if(!"".equals(paramName)) {
                    break;
                }
                if(x == tArr.length - 1) {
                    paramName = parameters[i].getName();
                    break;
                }
            }
        } else {
            paramName = parameterNames != null && parameterNames.length > i ? parameterNames[i] : parameters[i].getName();
        }
        return paramName;
    }

    private String getExceptionMessage(Exception e) {
        String message = e.getMessage();
        boolean isConstraintViolation = (e instanceof ConstraintViolationException);

        if (isConstraintViolation) {
            StringBuilder sb = new StringBuilder();
            String delimiter = ";";
            for (ConstraintViolation<?> violation : ((ConstraintViolationException) e).getConstraintViolations()) {
                sb.append(violation.getMessage()).append(delimiter);
            }
            sb.delete(sb.length() - delimiter.length(), sb.length());
            message = sb.toString();
        }

        return message;
    }

    private boolean canToJson(Object[] objs) {
        boolean result = true;

        if (ArrayUtils.isNotEmpty(objs)) {
            for (Object obj : objs) {
                if (obj instanceof DataBinder || obj instanceof WebDataBinder) {
                    return false;
                }
            }
        }

        return result;
    }
}
