package com.qiwen.base.service.impl;

import com.qiwen.base.entity.MenuPrivilege;
import com.qiwen.base.entity.QMenuPrivilege;
import com.qiwen.base.repository.MenuPrivilegeRepository;
import com.qiwen.base.service.IMenuPrivilegeService;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.qiwen.base.config.QWConstant.CACHE_MENU_KEY;
import static com.qiwen.base.config.QWConstant.CACHE_PRIVILEGE_KEY;

@Service
public class MenuPrivilegeServiceImpl implements IMenuPrivilegeService {

    private final JPAQueryFactory factory;

    private final MenuPrivilegeRepository menuPrivilegeRepo;

    public MenuPrivilegeServiceImpl(JPAQueryFactory factory, MenuPrivilegeRepository menuPrivilegeRepo) {
        this.factory = factory;
        this.menuPrivilegeRepo = menuPrivilegeRepo;
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY}, allEntries = true)
    @Transactional
    @Override
    public void save(MenuPrivilege menuPrivilege) {
        // 如果该条记录已经存在则不保存
        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        long count = factory.selectFrom(qMenuPrivilege)
                .where(
                        qMenuPrivilege.menuId.eq(menuPrivilege.getMenuId())
                                .and(qMenuPrivilege.privilegeName.eq(menuPrivilege.getPrivilegeName()))
                ).fetchCount();
        if(count <= 0) {
            menuPrivilegeRepo.save(menuPrivilege);
        }
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY}, allEntries = true)
    @Transactional
    @Override
    public void save(List<MenuPrivilege> menuPrivileges) {
        // 删除 menuId = menuPrivilege.getMenuId() 的记录.
        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        factory.delete(qMenuPrivilege)
                .where(qMenuPrivilege.menuId.eq(menuPrivileges.get(0).getMenuId()))
                .execute();
        menuPrivilegeRepo.saveAll(menuPrivileges);
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY}, allEntries = true)
    @Transactional
    @Override
    public void deleteByPrivilegeName(String privilegeName) {
        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        factory.delete(qMenuPrivilege)
                .where(qMenuPrivilege.privilegeName.eq(privilegeName))
                .execute();
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY, CACHE_PRIVILEGE_KEY}, allEntries = true)
    @Transactional
    @Override
    public void deleteByMenuId(Long menuId) {
        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        factory.delete(qMenuPrivilege)
                .where(qMenuPrivilege.menuId.eq(menuId))
                .execute();
    }
}
