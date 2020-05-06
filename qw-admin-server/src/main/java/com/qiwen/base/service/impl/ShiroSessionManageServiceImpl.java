package com.qiwen.base.service.impl;//package com.qiwen.base.service.impl;

import com.qiwen.base.service.ISessionManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class ShiroSessionManageServiceImpl implements ISessionManagerService {

    private static final String CACHE_KEYS_KEY = "SESSION_MANAGER_CACHE_KEY";

    private final DefaultWebSessionManager sessionManager;

    private final SessionDAO sessionDAO;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public ShiroSessionManageServiceImpl(DefaultWebSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionDAO = this.sessionManager.getSessionDAO();
    }

    /**
     * Shiro 中次功能不需要
     *
     * @param session
     */
    @Override
    public void addHttpSession(Session session) {
        throw new UnsupportedOperationException();
    }

    /**
     * Shiro 中此功能不需要实现
     *
     * @param session
     */
    @Override
    public void removeSession(Session session) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Session> findSessions(Function<Session, Boolean> filter) {
        Collection<Session> activeSessions = sessionDAO.getActiveSessions();
        if (CollectionUtils.isEmpty(activeSessions)) {
            return Collections.EMPTY_LIST;
        }
        List<Session> sessions = activeSessions.stream()
                .filter(session -> filter.apply(session))
                .collect(Collectors.toList());
        return sessions;
    }

    @Override
    public Page<Session> findSessions(Function<Session, Boolean> filter, Pageable pageable, Comparator<Session> comparator) {
        Collection<Session> activeSessions = sessionDAO.getActiveSessions();
        if (CollectionUtils.isEmpty(activeSessions)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        Stream<Session> stream = activeSessions.stream();
        if (comparator != null) {
            stream = stream.sorted(comparator);
        }
        long totalCount = stream.filter(session -> filter.apply(session))
                .count();

        stream = activeSessions.stream();
        if (comparator != null) {
            stream = stream.sorted(comparator);
        }
        List<Session> sessionList = stream.filter(session -> filter.apply(session))
                .skip(pageable.getOffset())
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());

        return new PageImpl<>(sessionList, pageable, totalCount);
    }

    @Override
    public void validateSessions() {
        this.sessionManager.validateSessions();
    }
}
