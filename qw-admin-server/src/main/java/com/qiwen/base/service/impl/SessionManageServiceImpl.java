//package com.qiwen.base.service.impl;
//
//import com.qiwen.base.config.Constant;
//import com.qiwen.base.service.ISessionManagerService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.shiro.session.Session;
//import org.springframework.cache.Cache;
//import org.springframework.cache.CacheManager;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Objects;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//public class SessionManageServiceImpl implements ISessionManagerService {
//
//    private static final String CACHE_KEYS_KEY = "SESSION_MANAGER_CACHE_KEY";
//
//    private final CacheManager cacheManager;
//
//    private final Cache cacheBean;
//
//    public SessionManageServiceImpl(CacheManager cacheManager) {
//        this.cacheManager = cacheManager;
//        this.cacheBean = this.cacheManager.getCache(Constant.CACHE_SESSION_KEY);
//    }
//
//    private synchronized List<String> getCacheKeyList() {
//        List<String> cacheKeys = this.cacheBean.get(CACHE_KEYS_KEY, List.class);
//        if(Objects.isNull(cacheKeys)) {
//            if(Objects.isNull(cacheKeys)) {
//                cacheKeys = new ArrayList();
//                this.cacheBean.put(CACHE_KEYS_KEY, cacheKeys);
//            }
//        }
//        return cacheKeys;
//    }
//
//    private synchronized void validateEffectiveKey() {
//        List<String> cacheKeyList = getCacheKeyList();
//        if(cacheKeyList.isEmpty()) {
//            return;
//        }
//
//        List<String> livedKeyList = new ArrayList<>();
//        for(String key : cacheKeyList) {
//            if(this.cacheBean.get(key) != null) {
//                livedKeyList.add(key);
//            }
//        }
//
//        if(livedKeyList.size() < cacheKeyList.size()) {
//            this.cacheBean.put(CACHE_KEYS_KEY, livedKeyList);
//            log.info("删除{}条失效缓存键值", cacheKeyList.size() - livedKeyList.size());
//        }
//    }
//
//    private synchronized void addCacheKey(String key) {
//        List<String> cacheKeyList = getCacheKeyList();
//        cacheKeyList.add(key);
//        this.cacheBean.put(CACHE_KEYS_KEY, cacheKeyList);
//    }
//
//    private synchronized void removeCacheKey(String key) {
//        List<String> cacheKeyList = getCacheKeyList();
//        boolean isSuccess = cacheKeyList.remove(key);
//        if(isSuccess) {
//            this.cacheBean.put(CACHE_KEYS_KEY, cacheKeyList);
//        } else {
//            log.warn("remove key { } not exist, may be out of data...");
//        }
//    }
//
//    @Override
//    public void addHttpSession(Session session) {
//        String sessionId = (String) session.getId();
//        Cache.ValueWrapper resultValWrapper = this.cacheBean.putIfAbsent(sessionId, session);
//
//        if(resultValWrapper == null) {
//            addCacheKey(sessionId);
//        }
//    }
//
//    @Override
//    public void removeSession(Session session) {
//        String sessionId = (String) session.getId();
//        // 这里可能会出问题，但是不是线程安全性问题，几率很小
//        if(this.cacheBean.get(session) != null) {
//            this.cacheBean.evict(sessionId);
//            removeCacheKey(sessionId);
//        }
//    }
//
//    @Override
//    public List<Session> findSessions(Function<Session, Boolean> filter) {
//        List<String> cacheKeyList = getCacheKeyList();
//        if(cacheKeyList.isEmpty()) {
//            return Collections.EMPTY_LIST;
//        }
//        List<Session> sessions = cacheKeyList.stream()
//                .filter(cacheKey -> this.cacheBean.get(cacheKey) != null)
//                .map(cacheKey -> this.cacheBean.get(cacheKey, Session.class))
//                .filter(session -> filter.apply(session))
//                .collect(Collectors.toList());
//        return sessions;
//    }
//}
