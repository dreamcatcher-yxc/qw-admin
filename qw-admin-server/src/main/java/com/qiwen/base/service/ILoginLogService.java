package com.qiwen.base.service;

import com.qiwen.base.dto.LoginLogQueryDTO;
import com.qiwen.base.vo.LoginLogVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

/**
 * 登录日志服务层
 */
public interface ILoginLogService {

    void save(LoginLogVO loginLogVO);

    void deleteByIds(Long... ids);

    boolean updateLogoutInfo(String sessionId, Date date, String type);

    Optional<LoginLogVO> findById(Long id);

    Page<LoginLogVO> findByCondition(LoginLogQueryDTO queryDTO, Pageable pageable);

}
