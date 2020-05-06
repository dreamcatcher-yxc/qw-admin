package com.qiwen.base.service.impl;

import com.qiwen.base.entity.*;
import com.qiwen.base.repository.MenuRepository;
import com.qiwen.base.service.IMenuService;
import com.qiwen.base.vo.MenuVO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.qiwen.base.config.QWConstant.CACHE_MENU_KEY;
import static com.qiwen.base.config.QWConstant.CACHE_PRIVILEGE_KEY;

@Service
public class MenuServiceImpl implements IMenuService {

    private final MenuRepository menuRepo;

    private final JPAQueryFactory factory;

    public MenuServiceImpl(MenuRepository menuRepo, JPAQueryFactory factory) {
        this.menuRepo = menuRepo;
        this.factory = factory;
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY}, allEntries = true)
    @Transactional
    @Override
    public void save(Menu menu) {
        this.menuRepo.save(menu);
        // 查找父节点, 如果存在并且 isLeaf = 1 则修改为 isLeaf = 0
        // 并且删除父节点和权限的映射记录.
        QMenu qMenu = QMenu.menu;
        Menu parentMenu = factory.selectFrom(qMenu)
                .where(qMenu.treeId.eq(menu.getTreeParentId()))
                .fetchFirst();
        if(Objects.isNull(parentMenu) || 0 == parentMenu.getIsLeaf()) {
            return;
        }
        parentMenu.setIsLeaf(0);
        menuRepo.save(parentMenu);

        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        Long parentMenuId = factory.select(qMenu.id)
                .from(qMenu)
                .where(qMenu.treeId.eq(menu.getTreeParentId()))
                .fetchOne();
        factory.delete(qMenuPrivilege)
                .where(qMenuPrivilege.menuId.eq(parentMenuId))
                .execute();
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY}, allEntries = true)
    @Transactional
    @Override
    public void modifyNameById(Long id, String name) {
        Optional<Menu> menuOpt = menuRepo.findById(id);
        menuOpt.ifPresent(menu -> {
            if(!name.trim().equals(menu.getName())) {
                menu.setName(name);
                menuRepo.save(menu);
            }
        });
    }

    @CacheEvict(cacheNames = {CACHE_MENU_KEY}, allEntries = true)
    @Transactional
    @Override
    public void modifyById(Menu menu) {
        Optional<Menu> menuopt = menuRepo.findById(menu.getId());
        menuopt.ifPresent(realmenu -> {
            realmenu.setName(menu.getName());
            if(StringUtils.isNotEmpty(menu.getUrl())) {
                realmenu.setUrl(menu.getUrl());
            }
            if(StringUtils.isNotEmpty(menu.getIcon())) {
                realmenu.setIcon(menu.getIcon());
            }
            realmenu.setDescription(menu.getDescription());
            menuRepo.save(realmenu);
        });
    }

    @CacheEvict(cacheNames = {CACHE_PRIVILEGE_KEY, CACHE_MENU_KEY}, allEntries = true)
    @Transactional
    @Override
    public void deleteByIds(Long... ids) {
        QMenu qMenu = QMenu.menu;
        QMenuPrivilege menuPrivilege = QMenuPrivilege.menuPrivilege;
        factory.delete(qMenu)
                .where(qMenu.id.in(ids))
                .execute();
        factory.delete(menuPrivilege)
                .where(menuPrivilege.menuId.in(ids));
    }

    @CacheEvict(cacheNames = {CACHE_PRIVILEGE_KEY, CACHE_MENU_KEY}, allEntries = true)
    @Transactional
    @Override
    public void deleteByIdsAndAdjust(List<Long> ids, List<Menu> adjustMenus) {
        QMenu qMenu = QMenu.menu;
        QMenuPrivilege menuPrivilege = QMenuPrivilege.menuPrivilege;
        factory.delete(qMenu)
                .where(qMenu.id.in(ids))
                .execute();
        factory.delete(menuPrivilege)
                .where(menuPrivilege.menuId.in(ids));

        if(CollectionUtils.isEmpty(adjustMenus)) {
            return;
        }

        List<Long> adjustIds = adjustMenus.stream()
                .map(menu -> menu.getId())
                .collect(Collectors.toList());
        List<Menu> realAdjustMenus = menuRepo.findAllById(adjustIds);

        for (Menu realAdjustMenu : realAdjustMenus) {
            for (Menu adjustMenu : adjustMenus) {
                if(realAdjustMenu.getId().equals(adjustMenu.getId())) {
                    realAdjustMenu.setTreeId(adjustMenu.getTreeId());
                    realAdjustMenu.setTreeParentId(adjustMenu.getTreeParentId());
                    realAdjustMenu.setIsLeaf(adjustMenu.getIsLeaf());
                    realAdjustMenu.setOrderIndex(adjustMenu.getOrderIndex());
                    break;
                }
            }
        }
        menuRepo.saveAll(realAdjustMenus);

        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        // 删除关联 menu_privilege 中关联的记录.
        factory.delete(qMenuPrivilege)
                .where(qMenuPrivilege.menuId.in(ids))
                .execute();

        // 从 menu_privilege 中删除 adjustMenus 中所有的父节点存在的记录.
        List<Long> needDeleteMenuIds = realAdjustMenus.stream()
                .filter(realMenu -> 0 == realMenu.getIsLeaf())
                .map(realMenu -> realMenu.getId())
                .collect(Collectors.toList());

        if(CollectionUtils.isEmpty(needDeleteMenuIds)) {
            return;
        }

        factory.delete(qMenuPrivilege)
                .where(qMenuPrivilege.menuId.in(needDeleteMenuIds))
                .execute();
    }

    @CacheEvict(value = CACHE_MENU_KEY, allEntries = true)
    @Transactional
    @Override
    public void adjustByMenus(List<Menu> menus, boolean removeRelation) {
        if(CollectionUtils.isEmpty(menus)) {
            return;
        }

        List<Long> adjustIds = menus.stream()
                .map(menu -> menu.getId())
                .collect(Collectors.toList());
        List<Menu> realAdjustMenus = menuRepo.findAllById(adjustIds);

        for (Menu realAdjustMenu : realAdjustMenus) {
            for (Menu adjustMenu : menus) {
                if(realAdjustMenu.getId().equals(adjustMenu.getId())) {
                    realAdjustMenu.setTreeId(adjustMenu.getTreeId());
                    realAdjustMenu.setTreeParentId(adjustMenu.getTreeParentId());
                    realAdjustMenu.setIsLeaf(adjustMenu.getIsLeaf());
                    realAdjustMenu.setOrderIndex(adjustMenu.getOrderIndex());
                    break;
                }
            }
        }

        menuRepo.saveAll(realAdjustMenus);

        if(removeRelation) {
            // 删除菜单权限表的非叶子节点依然存在的记录.
            QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
            List<Long> needRemoveMenuIds = menus.stream()
                    .filter(menu -> 0 == menu.getIsLeaf())
                    .map(menu -> menu.getId())
                    .collect(Collectors.toList());
            factory.delete(qMenuPrivilege)
                    .where(qMenuPrivilege.menuId.in(needRemoveMenuIds))
                    .execute();
        }
    }

    @Cacheable(value = CACHE_MENU_KEY, key = "'all'")
    @Override
    public List<Menu> findAll() {
        return menuRepo.findAll();
    }

    private List<MenuVO> getAllSubMenus(String treeParentId, List<Menu> completeMenus) {
        List<Menu> subMenus = completeMenus.stream()
                .filter(menu -> menu.getTreeParentId().equals(treeParentId))
                .collect(Collectors.toList());
        subMenus.sort((a, b) -> a.getOrderIndex() > b.getOrderIndex() ? 1 : -1);

        List<MenuVO> subMenuVOs = new ArrayList<>();
        for(Menu menu : subMenus) {
            MenuVO menuVO = new MenuVO();
            menuVO.setName(menu.getName());
            menuVO.setUrl(menu.getUrl());
            menuVO.setTreeId(menu.getTreeId());
            menuVO.setIcon(menu.getIcon());
            subMenuVOs.add(menuVO);
        }
        return subMenuVOs;
    }

    private List<Menu> completeMenus(List<Menu> allMenu, List<Menu> hasLeafMenu) {
        List<Menu> completeMenus = new ArrayList<>(hasLeafMenu);

        for(int i = 0; i < hasLeafMenu.size(); i++) {
            Menu leafMenu = hasLeafMenu.get(i);
            String treeParentId = leafMenu.getTreeParentId();

            while (!"1".equals(treeParentId) && treeParentId.length() >= 3) {
                final String tPid = treeParentId;
                Optional<Menu> firstMenuOpt = allMenu.stream()
                        .filter(menu -> tPid.equals(menu.getTreeId()))
                        .findFirst();
                if(firstMenuOpt.isPresent()) {
                    Menu tParentMenu  = firstMenuOpt.get();
                    treeParentId = tParentMenu.getTreeParentId();
                    String tParentTreeId = tParentMenu.getTreeId();
                    // 如果父节点已经存在, 则不用添加.
                    boolean exist = completeMenus.stream()
                            .anyMatch(cms -> cms.getTreeId().equals(tParentTreeId));
                    if(!exist) {
                        completeMenus.add(firstMenuOpt.get());
                    }
                } else {
                    treeParentId = "1";
                }
            }
        }
        return completeMenus;
    }

    private void covertMenusToMenuVO(MenuVO parentMenuVO, String treeParentId, List<Menu> completeMenus) {
        List<MenuVO> subMenus = getAllSubMenus(treeParentId, completeMenus);
        parentMenuVO.setSubMenus(subMenus);

        for(int i = 0; subMenus != null && i < subMenus.size(); i++) {
            covertMenusToMenuVO(subMenus.get(i), subMenus.get(i).getTreeId(), completeMenus);
        }
    }

    @Cacheable(value = CACHE_MENU_KEY, key = "'userId:' + #userId")
    @Override
    public List<MenuVO> findAllByUserId(Long userId) {
        QUser qUser = QUser.user;
        QUserRole qUserRole = QUserRole.userRole;
        QRole qRole = QRole.role;
        QRolePrivilege qRolePrivilege = QRolePrivilege.rolePrivilege;
        QPrivilege qPrivilege = QPrivilege.privilege;
        QMenuPrivilege qMenuPrivilege = QMenuPrivilege.menuPrivilege;
        QMenu qMenu = QMenu.menu;

        List<Menu> leafMenus = factory.selectDistinct(qMenu)
                .from(qMenu)
                .innerJoin(qMenuPrivilege)
                .on(qMenu.id.eq(qMenuPrivilege.menuId))
                .innerJoin(qPrivilege)
                .on(qMenuPrivilege.privilegeName.eq(qPrivilege.name))
                .innerJoin(qRolePrivilege)
                .on(qPrivilege.name.eq(qRolePrivilege.privilegeName))
                .innerJoin(qRole)
                .on(qRolePrivilege.roleId.eq(qRole.id))
                .innerJoin(qUserRole)
                .on(qRole.id.eq(qUserRole.roleId))
                .innerJoin(qUser)
                .on(qUserRole.userId.eq(qUser.id))
                .where(
                        qUser.id.eq(userId)
                )
                .fetch();

        List<Menu> allMenus = findAll();
        MenuVO root = new MenuVO();
        root.setTreeId("1");
        covertMenusToMenuVO(root, root.getTreeId(), completeMenus(allMenus, leafMenus));
        return root.getSubMenus();
    }

    @Override
    public Optional<Menu> findById(Long id) {
        return menuRepo.findById(id);
    }

    private void subRecursionMenuVOS(MenuVO menuVO, Predicate<MenuVO> menuVOHandler) {
        boolean isTrue = menuVOHandler.test(menuVO);
        if(isTrue) {
            List<MenuVO> subMenus = menuVO.getSubMenus();
            if(!CollectionUtils.isEmpty(subMenus)) {
                subMenus.forEach(child -> {
                    subRecursionMenuVOS(child, menuVOHandler);
                });
            }
        }
    }

    @Override
    public void recursionMenuVOS(List<MenuVO> menuVOS, Predicate<MenuVO> menuVOHandler) {
        if(CollectionUtils.isEmpty(menuVOS)) {
            return;
        }
        menuVOS.forEach(menuVO -> subRecursionMenuVOS(menuVO, menuVOHandler));
    }
}
