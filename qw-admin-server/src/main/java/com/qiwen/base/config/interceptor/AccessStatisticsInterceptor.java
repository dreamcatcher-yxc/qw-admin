//package com.qiwen.base.config.interceptor;
//
//import lombok.Data;
//import lombok.ToString;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
////@Component
//public class AccessStatisticsInterceptor extends HandlerInterceptorAdapter {
//
//    @Data
//    @ToString
//    public static class AccessStatistics {
//        private Date preHandleDate;
//        private Date postHandleDate;
//        private Date afterCompletionDate;
//    }
//
//    private Map<Long, AccessStatistics> map = new HashMap<>();
//
//    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        AccessStatistics as = new AccessStatistics();
//        long threadId = Thread.currentThread().getId();
//        Date preHandleDate = new Date();
//        log.info("{} thread invoke preHandle date is {}", threadId, sdf.format(preHandleDate));
//        as.setPreHandleDate(preHandleDate);
//        map.put(threadId, as);
//        return true;
//    }
//
//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        long threadId = Thread.currentThread().getId();
//        AccessStatistics as = map.get(threadId);
//        if(as != null) {
//            Date preHandleDate = as.getPreHandleDate();
//            Date postHandleDate = new Date();
//            log.info("{} thread invoke postHandle date is {}, waste time is {} ms", threadId, sdf.format(postHandleDate), postHandleDate.getTime() - preHandleDate.getTime());
//            as.setPostHandleDate(postHandleDate);
//        }
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        long threadId = Thread.currentThread().getId();
//        AccessStatistics as = map.get(threadId);
//        if(as != null) {
//            Date preHandleDate = as.getPreHandleDate();
//            Date afterCompletionDate = new Date();
//            log.info("{} thread invoke afterCompletion date is {}, waste time is {} ms, url is {}", threadId, sdf.format(afterCompletionDate), afterCompletionDate.getTime() - preHandleDate.getTime(), request.getRequestURI());
//            map.remove(threadId);
//        }
//    }
//}
