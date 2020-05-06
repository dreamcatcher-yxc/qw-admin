package com.qiwen.base.service;

import com.qiwen.base.entity.Role;
import com.qiwen.base.entity.RolePrivilege;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IRoleService {

    String SYSTEM_ROLE = "system";

    String CUSTOMER_ROLE = "customer";

    void save(Role role);

    void deleteByIds(Long... ids);

    Page<Role> findAll(Pageable pageable);

    Optional<Role> findById(Long id);

    List<Role> findAll();

    Page<Role> findByCondition(String name, Pageable pageable);

    List<Long> findRoleIdsByUserId(Long userId);

    List<Role> findByUserId(Long userId);

    Role findByName(String name);

    Page<Role> findAll(String type, Pageable pageable);

    Optional<Role> findById(String type, Long id);

    List<Role> findAll(String type);

    Page<Role> findByCondition(String name, String type, Pageable pageable);

    Role findByName(String type, String name);

    void modifyById(Role role);

    void modifyRolePrivileges(Long roleId, List<RolePrivilege> rolePrivileges);
}
