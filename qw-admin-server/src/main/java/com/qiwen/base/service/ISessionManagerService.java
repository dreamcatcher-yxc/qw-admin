package com.qiwen.base.service;

import org.apache.shiro.session.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 提供系统统一的 session 管理服务
 */
public interface ISessionManagerService {

    /**
     * 添加 session
     * @param session
     */
    void addHttpSession(Session session);

    /**
     * 删除 session
     * @param session
     */
    void removeSession(Session session);

    /**
     * 查询 session
     * @param filter
     * @return
     */
    List<Session> findSessions(Function<Session, Boolean> filter);

    /**
     * 带分页条件的查询
     * @param filter
     * @param pageable
     * @param comparator 可以为 null, 为 null 时将忽略排序条件
     * @return
     */
    Page<Session> findSessions(Function<Session, Boolean> filter, Pageable pageable, Comparator<Session> comparator);

    /**
     * 校验所有 session 状态
     */
    void validateSessions();
}
