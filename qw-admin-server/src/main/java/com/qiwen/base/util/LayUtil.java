package com.qiwen.base.util;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.Map;

/**
 * 用于处理和 layUI 前端想关的数据.
 */
public class LayUtil {
    public static Pageable toPageable(int page, int limit) {
        return PageRequest.of(page > 0 ? page - 1 : page, limit);
    }

    public static <T> String toLayPageJson(Page<T> page, String... msg) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", 0);
        if(!ArrayUtils.isEmpty(msg)) {
            if(msg.length == 1) {
                map.put("msg",  msg[0]);
            } else {
                map.put("msg", msg);
            }
        }
        map.put("data", page.getContent());
        map.put("count", page.getTotalElements());
        return GsonUtil.toJson(map);
    }
}
