package com.qiwen.base.service;

import com.qiwen.base.vo.MenuVO;
import com.qiwen.base.entity.Menu;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface IMenuService {

    void save(Menu menu);

    void modifyNameById(Long id, String name);

    void modifyById(Menu menu);

    void deleteByIds(Long... ids);

    void deleteByIdsAndAdjust(List<Long> ids, List<Menu> adjustMenus);

    void adjustByMenus(List<Menu> menus, boolean removeRelation);

    List<Menu> findAll();

    List<MenuVO> findAllByUserId(Long userId);

    Optional<Menu> findById(Long id);

    /**
     * 递归遍历 MenuVO
     * @param menuVOS
     */
    void recursionMenuVOS(List<MenuVO> menuVOS, Predicate<MenuVO> menuVOHandler);
}
