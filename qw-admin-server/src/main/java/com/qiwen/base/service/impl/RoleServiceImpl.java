package com.qiwen.base.service.impl;

import com.qiwen.base.entity.*;
import com.qiwen.base.repository.RolePrivilegeRepository;
import com.qiwen.base.repository.RoleRepository;
import com.qiwen.base.service.IRoleService;
import com.qiwen.base.util.QueryDSLUtil;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.qiwen.base.config.QWConstant.*;


@Service
public class RoleServiceImpl implements IRoleService {

    private final JPAQueryFactory factory;

    private final RolePrivilegeRepository rolePrivilegeRepo;

    private final RoleRepository roleRepo;

    public RoleServiceImpl(JPAQueryFactory factory, RolePrivilegeRepository rolePrivilegeRepo, RoleRepository roleRepo) {
        this.factory = factory;
        this.rolePrivilegeRepo = rolePrivilegeRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional
    @Override
    public void save(Role role) {
        role.setType("admin");
        roleRepo.save(role);
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY, CACHE_ROLE_KEY}, allEntries = true)
    @Transactional
    @Override
    public void deleteByIds(Long... ids) {
        QRole qRole = QRole.role;
        long row = factory.delete(qRole)
                .where(qRole.id.in(ids))
                .execute();
        if(row > 0) {
            // 删除用户角色相关信息
            QUserRole userRole = QUserRole.userRole;
            factory.delete(userRole)
                    .where(userRole.roleId.in(ids))
                    .execute();
            // 删除角色权限相关信息
            QRolePrivilege qRolePrivilege = QRolePrivilege.rolePrivilege;
            factory.delete(qRolePrivilege)
                    .where(qRolePrivilege.roleId.in(ids))
                    .execute();
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Role> findAll(Pageable pageable) {
        return roleRepo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Role> findById(Long id) {
        Optional<Role> roleOpt = roleRepo.findById(id);
        return roleOpt;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Role> findAll() {
        return roleRepo.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Role> findByCondition(String name, Pageable pageable) {
        Page<Role> page = roleRepo.findAll((r, q, b) -> {
            List<Predicate> pres = new ArrayList<>();
            if (!StringUtils.isEmpty(name)) {
                pres.add(b.like(r.get("name"), "%" + name + "%"));
            }
            return q.where(pres.toArray(new Predicate[pres.size()]))
                    .orderBy(b.desc(r.get("createDate")))
                    .getRestriction();
        }, pageable);
        return page;
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> findRoleIdsByUserId(Long userId) {
        QUserRole qUserRole = QUserRole.userRole;
        List<Long> roleIds = factory.select(qUserRole.roleId)
                .from(qUserRole)
                .where(qUserRole.userId.eq(userId))
                .fetch();
        return roleIds;
    }

    @Cacheable(value = CACHE_ROLE_KEY, key = "'userId:' + #userId")
    @Transactional(readOnly = true)
    @Override
    public List<Role> findByUserId(Long userId) {
        QUserRole qUserRole = QUserRole.userRole;
        QRole qRole = QRole.role;

        List<Long> roleIds = factory.select(qUserRole.roleId)
                .from(qUserRole)
                .where(qUserRole.userId.eq(userId))
                .fetch();
        if(roleIds == null) {
            return null;
        }

        List<Role> roles = factory.selectFrom(qRole)
                .where(qRole.id.in(roleIds))
                .fetch();

        return roles;
    }

    @Transactional(readOnly = true)
    @Override
    public Role findByName(String name) {
        QRole role = QRole.role;
        Role res = factory.selectFrom(role)
                .where(role.name.eq(name))
                .fetchFirst();
        return res;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Role> findAll(String type, Pageable pageable) {
        QRole qRole = QRole.role;
        JPAQuery<Role> query = factory.select(qRole)
                .where(qRole.type.eq(type))
                .orderBy(qRole.createBy.desc());
        Page<Role> page = QueryDSLUtil.page(query, pageable);
        return page;
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Role> findById(String type, Long id) {
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Role> findAll(String type) {
        QRole qRole = QRole.role;
        List<Role> roles = factory.selectFrom(qRole)
                            .where(qRole.type.eq(type))
                            .fetch();
        return roles;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Role> findByCondition(String name, String type, Pageable pageable) {
        Page<Role> page = roleRepo.findAll((r, q, b) -> {
            List<Predicate> pres = new ArrayList<>();
            pres.add(b.equal(r.get("type"), type));

            if (!StringUtils.isEmpty(name)) {
                pres.add(b.like(r.get("name"), "%" + name + "%"));
            }

            return q.where(pres.toArray(new Predicate[pres.size()]))
                    .orderBy(b.desc(r.get("createDate")))
                    .getRestriction();
        }, pageable);

        return page;
    }

    @Transactional(readOnly = true)
    @Override
    public Role findByName(String type, String name) {
        QRole role = QRole.role;
        Role res = factory.selectFrom(role)
                .where(role.name.eq(name).and(role.type.eq(type)))
                .fetchFirst();
        return res;
    }

    @CacheEvict(value = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY, CACHE_ROLE_KEY})
    @Transactional
    @Override
    public void modifyById(Role role) {
        role.setType("admin");
        Optional<Role> realRole = roleRepo.findById(role.getId());
        realRole.ifPresent(tr -> {
            tr.setName(role.getName());
            tr.setDescription(role.getDescription());
            tr.setType(role.getType());
            roleRepo.save(tr);
        });
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY, CACHE_ROLE_KEY}, allEntries = true)
    @Transactional()
    @Override
    public void modifyRolePrivileges(Long roleId, List<RolePrivilege> rolePrivileges) {
        QRolePrivilege qRolePrivilege = QRolePrivilege.rolePrivilege;
        factory.delete(qRolePrivilege)
                .where(qRolePrivilege.roleId.eq(roleId))
                .execute();
        rolePrivilegeRepo.saveAll(rolePrivileges);
    }
}
