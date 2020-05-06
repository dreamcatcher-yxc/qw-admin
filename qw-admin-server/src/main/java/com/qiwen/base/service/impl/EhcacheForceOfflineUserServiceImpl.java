package com.qiwen.base.service.impl;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.dto.OfflineUserDTO;
import com.qiwen.base.service.IForceOfflineUserService;
import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ehcache 存储实现
 */
@Slf4j
@Service
public class EhcacheForceOfflineUserServiceImpl implements IForceOfflineUserService {

    private final Cache offlineUserCache;

    public EhcacheForceOfflineUserServiceImpl(CacheManager cacheManager) {
        offlineUserCache = cacheManager.getCache(QWConstant.CACHE_OFFLINE_USER_KEY);
    }

    @Override
    public void save(OfflineUserDTO offlineUserDTO) {
        offlineUserCache.put(offlineUserDTO.getUsername(), offlineUserDTO);
    }

    @Override
    public void deleteByUsernames(String... usernames) {
        if(ArrayUtils.isNotEmpty(usernames)) {
            Arrays.stream(usernames)
                    .forEach(username -> offlineUserCache.evict(username));
        }
    }

    @Override
    public boolean handleOfflineUser(String username,  boolean needAutoCountDownAndDelete) {
        Cache.ValueWrapper val = offlineUserCache.get(username);
        if(val == null) {
            return true;
        }
        OfflineUserDTO offlineUserDTO = (OfflineUserDTO) val.get();
        int offlineType = offlineUserDTO.getOfflineType();
        if(offlineType == 1) {
            int count = offlineUserDTO.getCount();
            if(needAutoCountDownAndDelete) {
                // 删除
                if(count == 1) {
                    deleteByUsernames(offlineUserCache.getName());
                }
                // 更新
                else {
                    offlineUserDTO.setCount(count - 1);
                    offlineUserCache.put(offlineUserDTO.getUsername(), offlineUserDTO);
                }

            }
            return count <= 0;
        }
        // 其他类型暂时不做处理, 都返回 true。
        return true;
    }

    @Override
    public List<OfflineUserDTO> findByUsernames(String... usernames) {
        if(ArrayUtils.isNotEmpty(usernames)) {
            return Arrays.stream(usernames)
                    .map(username -> (OfflineUserDTO)(offlineUserCache.get(username)))
                    .collect(Collectors.toList());
        }
        Object nativeCache = offlineUserCache.getNativeCache();
        if(nativeCache instanceof Ehcache) {
            Ehcache ehcache = (Ehcache) nativeCache;
            List keys = ehcache.getKeys();
            if(CollectionUtils.isNotEmpty(keys)) {
                Map<Object, Element> all = ehcache.getAll(keys);
                return  all.entrySet().stream()
                        .map(entry -> (OfflineUserDTO) (entry.getValue().getObjectValue()))
                        .collect(Collectors.toList());
            }
            return null;
        }
        log.error("当前缓存实现类为{}", nativeCache.getClass().getName());
        return null;
    }
}
