package com.qiwen.base.service;

import com.qiwen.base.entity.User;
import com.qiwen.base.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IUserService {

    User findById(long id);

    User findByUsernameAndPassword(String username, String password);

    Page<User> findAll(Pageable pageable);

    Page<User> findAll(Pageable pageable, Long... excludeIds);

    Page<User> findByCondition(String username, Pageable pageable);

    User findByUsername(String username);

    void save(User user);

    void save(List<User> users);

    void deleteByIds(Long... ids);

    void modifyById(User user, boolean autoReplaceHeader);

    void modifyUserRoles(Long userId, List<UserRole> userRoles);

    void modifyPersonalInfoById(User user, boolean onlyChangeHeader);

    void modifyPassword(Long userId, String srcPassword, String newPassword);
}
