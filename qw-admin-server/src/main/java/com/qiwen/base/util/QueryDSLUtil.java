package com.qiwen.base.util;

import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class QueryDSLUtil {

    public static <T> Page<T> page(JPAQuery<T> query, Pageable pageable) {
        long count = query.fetchCount();
        List<T> list = query.offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Page<T> page = new PageImpl(list, pageable, count);
        return page;
    }
}
