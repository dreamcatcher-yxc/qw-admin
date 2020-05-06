package com.qiwen.base.service.impl;

import com.qiwen.base.dto.LoginLogQueryDTO;
import com.qiwen.base.entity.LoginLog;
import com.qiwen.base.entity.QLoginLog;
import com.qiwen.base.repository.LoginLogRepository;
import com.qiwen.base.service.IIp2RegionService;
import com.qiwen.base.service.ILoginLogService;
import com.qiwen.base.util.ReflectUtil;
import com.qiwen.base.vo.LoginLogVO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.lionsoul.ip2region.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.*;

@Slf4j
@Service
public class LoginLogServiceImpl implements ILoginLogService {

    private final LoginLogRepository loginLogRepo;

    private final JPAQueryFactory factory;

    private final IIp2RegionService ip2RegionService;

    public LoginLogServiceImpl(LoginLogRepository logRepo, JPAQueryFactory factory, IIp2RegionService iIp2RegionService) {
        this.loginLogRepo = logRepo;
        this.factory = factory;
        this.ip2RegionService = iIp2RegionService;
    }

    @SneakyThrows
    @Transactional
    @Override
    public void save(LoginLogVO loginLogVO) {
        String ip = loginLogVO.getIp();
        boolean isIpAddress = Util.isIpAddress(ip);
        String region = isIpAddress ? ip2RegionService.ip2Region(ip).toString() : "unknow";
        loginLogVO.setLoginRegion(region);
        LoginLog loginLog = ReflectUtil.O2ObySameField(loginLogVO, LoginLog.class);
        this.loginLogRepo.save(loginLog);
    }

    @Transactional
    @Override
    public void deleteByIds(Long... ids) {
        QLoginLog loginLog = QLoginLog.loginLog;
        factory.delete(loginLog)
                .where(loginLog.id.in(ids))
                .execute();
    }

    @Transactional
    @Override
    public boolean updateLogoutInfo(String sessionId, Date date, String type) {
        QLoginLog qLoginLog = QLoginLog.loginLog;
        LoginLog loginLog = factory.selectFrom(qLoginLog)
                .where(qLoginLog.sessionId.eq(sessionId))
                .fetchFirst();
        if(loginLog != null) {
            long effectCount = factory.update(qLoginLog)
                    .set(qLoginLog.logoutDate, date)
                    .set(qLoginLog.logoutType, type)
                    .where(qLoginLog.sessionId.eq(sessionId))
                    .execute();
            return effectCount > 0;
        }
        return false;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<LoginLogVO> findById(Long id) {
        Optional<LoginLog> loginLogOp = loginLogRepo.findById(id);
        if(loginLogOp.isPresent()) {
            return Optional.ofNullable(ReflectUtil.O2ObySameField(loginLogOp.get(), LoginLogVO.class));
        }
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<LoginLogVO> findByCondition(LoginLogQueryDTO queryDTO, Pageable pageable) {
        Page<LoginLog> loginLogs = loginLogRepo.findAll((r, q, b) -> {
            List<Predicate> predicates = new ArrayList<>();
            Date startDate = queryDTO.getStartDate(),
                    endDate = queryDTO.getEndDate();

            if (startDate != null && endDate != null) {
                predicates.add(b.between(r.get("loginDate"), startDate, endDate));
            } else if (startDate != null && endDate == null) {
                predicates.add(b.greaterThanOrEqualTo(r.get("loginDate"), startDate));
            } else if (startDate == null && endDate != null) {
                predicates.add(b.lessThanOrEqualTo(r.get("loginDate"), endDate));
            }

            String loginName = queryDTO.getLoginName();

            if (!StringUtils.isEmpty(loginName)) {
                predicates.add(b.like(r.get("loginName"), "%" + loginName + "%"));
            }
            return q.where(predicates.toArray(new Predicate[predicates.size()])).orderBy(b.desc(r.get("loginDate"))).getRestriction();
        }, pageable);
        List<LoginLogVO> loginLogVOS = ReflectUtil.replaceListElementType(loginLogs.getContent(), LoginLogVO.class);
        PageImpl<LoginLogVO> page = new PageImpl<>(loginLogVOS == null ? Collections.emptyList() : loginLogVOS, pageable, loginLogs.getTotalElements());
        return page;
    }
}
