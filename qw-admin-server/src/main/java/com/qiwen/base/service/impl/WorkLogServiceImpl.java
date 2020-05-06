package com.qiwen.base.service.impl;

import com.qiwen.base.entity.QWorkLog;
import com.qiwen.base.entity.WorkLog;
import com.qiwen.base.repository.WorkLogRepository;
import com.qiwen.base.service.IWorkLogService;
import com.qiwen.base.util.StringUtil;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class WorkLogServiceImpl implements IWorkLogService {

    private final JPAQueryFactory factory;

    private final WorkLogRepository workLogRepo;

    public WorkLogServiceImpl(JPAQueryFactory factory, WorkLogRepository workLogRepo) {
        this.factory = factory;
        this.workLogRepo = workLogRepo;
    }

    @Transactional
    @Override
    public void save(WorkLog workLog) {
        workLog.setWorkLogId(StringUtil.uuid());
        workLogRepo.save(workLog);
    }

    @Transactional
    @Override
    public void deleteByWorkLogIds(String... ids) {
        QWorkLog qwl = QWorkLog.workLog;
        factory.delete(qwl)
                .where(qwl.workLogId.in(ids))
                .execute();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<WorkLog> findAll(Date startDate, Date endDate, String jobClass, Pageable pageable) {
        return workLogRepo.findAll((r, q, b) -> {
            List<Predicate> predicates = new ArrayList<Predicate>();

            if(startDate != null && endDate != null) {
                predicates.add(b.between(r.get("createDate"), startDate, endDate));
            } else if(startDate != null && endDate == null) {
                predicates.add(b.greaterThanOrEqualTo(r.get("createDate"), startDate));
            } else if(startDate == null && endDate != null) {
                predicates.add(b.lessThanOrEqualTo(r.get("createDate"), endDate));
            }

            if(!StringUtils.isEmpty(jobClass)) {
                predicates.add(b.equal(r.get("jobClass"), jobClass));
            }
//            return b.or(predicates.toArray(new Predicate[predicates.size()]));
            return q.where(predicates.toArray(new Predicate[predicates.size()])).orderBy(b.desc(r.get("createDate"))).getRestriction();
        }, pageable);
    }

    @Override
    public WorkLog findById(String id) {
        QWorkLog qwl = QWorkLog.workLog;
        WorkLog workLog = factory.selectFrom(qwl)
                .where(qwl.workLogId.eq(id))
                .fetchFirst();
        return workLog;
    }
}
