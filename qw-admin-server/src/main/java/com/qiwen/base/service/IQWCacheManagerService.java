package com.qiwen.base.service;

import com.qiwen.base.entity.pojo.QWCacheMeta;

import java.util.List;

/**
 * 提供简单的缓存管理相关服务
 */
public interface IQWCacheManagerService {

    /**
     * 查询所有的缓存元数据
     * @return
     */
    List<QWCacheMeta> findAllCacheMetas();

    /**
     * 清空指定的所有缓存集合
     * @param cacheNames
     */
    void clearCaches(List<String> cacheNames);
}
