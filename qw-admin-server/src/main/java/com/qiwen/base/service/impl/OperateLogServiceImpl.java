package com.qiwen.base.service.impl;

import com.qiwen.base.entity.OperateLog;
import com.qiwen.base.entity.QOperateLog;
import com.qiwen.base.repository.OperateLogRepository;
import com.qiwen.base.service.IOperateLogService;
import com.qiwen.base.util.QueryDSLUtil;
import com.qiwen.base.util.StringUtil;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OperateLogServiceImpl implements IOperateLogService {

    private final JPAQueryFactory factory;

    private final OperateLogRepository olRepo;

    public OperateLogServiceImpl(JPAQueryFactory factory, OperateLogRepository olRepo) {
        this.factory = factory;
        this.olRepo = olRepo;
    }

    @Transactional
    @Override
    public void save(OperateLog operatorLog) {
        operatorLog.setOlId(StringUtil.uuid());
        olRepo.save(operatorLog);
    }

    @Transactional
    @Override
    public void delete(Long... ids) {
        QOperateLog qol = QOperateLog.operateLog;
        factory.delete(qol)
                .where(qol.id.in(ids))
                .execute();
    }

    @Transactional(readOnly = true)
    @Override
    public OperateLog findById(Long id) {
        return olRepo.findById(id).get();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<OperateLog> findAll(OperateLog operateLog, Date start, Date end, Pageable pageable) {
        QOperateLog qol = QOperateLog.operateLog;
        JPAQuery<OperateLog> query = factory.selectFrom(qol);
        String operatorName = operateLog.getOperatorName();
        String operation = operateLog.getOperation();
        String ip = operateLog.getIp();
        String operateMethod = operateLog.getOperateMethod();
        String result = operateLog.getResult();

        if(!StringUtils.isEmpty(operatorName)) {
            query.where(qol.operatorName.like("%" + operatorName + "%"));
        }

        if(!StringUtils.isEmpty(operation)) {
            query.where(qol.operation.like("%" + operation + "%"));
        }

        if(!StringUtils.isEmpty(ip)) {
            query.where(qol.ip.like("%" + ip + "%"));
        }

        if(!StringUtils.isEmpty(operateMethod)) {
            query.where(qol.operateMethod.like("%" + operateMethod + "%"));
        }

        if(!StringUtils.isEmpty(result)) {
            query.where(qol.result.eq(result));
        }

        boolean d1 = start == null;
        boolean d2 = end == null;

        if(d1 ^ d2) {
            if(d1) {
                query.where(qol.operateTime.before(end));
            } else {
                query.where(qol.operateTime.after(start));
            }
        } else if(!d1) {
            query.where(qol.operateTime.between(start, end));
        }

        query.orderBy(qol.operateTime.desc());
        return QueryDSLUtil.page(query, pageable);
    }
}
