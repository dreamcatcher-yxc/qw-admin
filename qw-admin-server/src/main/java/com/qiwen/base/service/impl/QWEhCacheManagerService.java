package com.qiwen.base.service.impl;

import com.qiwen.base.entity.pojo.QWCacheMeta;
import com.qiwen.base.service.IQWCacheManagerService;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import org.springframework.cache.Cache;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QWEhCacheManagerService implements IQWCacheManagerService {

    private final EhCacheCacheManager cacheManager;

    public QWEhCacheManagerService(EhCacheCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public List<QWCacheMeta> findAllCacheMetas() {
        List<String> names = new ArrayList<>();
        List<QWCacheMeta> metas = cacheManager.getCacheNames()
                .stream()
                .map(name -> {
                    Cache cache = this.cacheManager.getCache(name);
                    return (Ehcache) cache.getNativeCache();
                })
                .filter(cache -> Status.STATUS_ALIVE.equals(cache.getStatus()))
                .map(cache -> {
                    QWCacheMeta meta = new QWCacheMeta();
                    meta.setName(cache.getName());
                    meta.setMemoryStoreSize(cache.getMemoryStoreSize());
                    meta.setOffHeapStoreSize(cache.getOffHeapStoreSize());
                    meta.setDiskStoreSize(cache.getDiskStoreSize());
                    meta.setOnMemoryStoreBytes(cache.calculateInMemorySize());
                    meta.setOnOffHeapSizeBytes(cache.calculateOffHeapSize());
                    meta.setOnDiskStoreSizeBytes(cache.calculateOnDiskSize());
                    return meta;
                })
                .collect(Collectors.toList());
        return metas;
    }

    @Override
    public void clearCaches(List<String> cacheNames) {
        cacheManager.getCacheNames()
                .stream()
                .filter(cacheNames::contains)
                .forEach(name -> {
                    Cache cache = this.cacheManager.getCache(name);
                    cache.clear();
                });
    }
}
