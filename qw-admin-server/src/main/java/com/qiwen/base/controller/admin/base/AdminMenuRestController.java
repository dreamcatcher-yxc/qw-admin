package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.Menu;
import com.qiwen.base.entity.MenuPrivilege;
import com.qiwen.base.service.IMenuPrivilegeService;
import com.qiwen.base.service.IMenuService;
import com.qiwen.base.service.IPrivilegeService;
import com.qiwen.base.util.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "菜单管理", groupName = QWConstant.MODULE_GROUP_NAME + "menu-admin")
@RestController
@RequestMapping("/admin/base/menu/rest")
public class AdminMenuRestController {

    private final IMenuService menuService;

    private final IPrivilegeService privilegeService;

    private final IMenuPrivilegeService menuPrivilegeService;

    /**
     * 将 <b>1(id),1-0-0(treeId),1-0(treeParentId),1(isLeaf),0(orderIndex);...</b>
     * 类型的数据转换为 Menu 集合.
     */
    private final Function<String, List<Menu>> simpleMenuData2MenuList = (adjusts) -> {
        List<Menu> adjustMenus;

        if(StringUtils.isEmpty(adjusts)) {
            adjustMenus = new ArrayList<>();
        } else {
            List<String> tl2 = Arrays.asList(adjusts.split(";"));
            adjustMenus = tl2.stream()
                    .map(str -> {
                        String[] tArr = str.split(",");
                        Menu menu = new Menu();
                        menu.setId(Long.valueOf(tArr[0]));
                        menu.setTreeId(tArr[1]);
                        menu.setTreeParentId(tArr[2]);
                        menu.setIsLeaf(Integer.valueOf(tArr[3]));
                        menu.setOrderIndex(Integer.valueOf(tArr[4]));
                        return menu;
                    })
                    .collect(Collectors.toList());
        }
        return adjustMenus;
    };

    public AdminMenuRestController(IMenuService menuService, IPrivilegeService privilegeService, IMenuPrivilegeService menuPrivilegeService) {
        this.menuService = menuService;
        this.privilegeService = privilegeService;
        this.menuPrivilegeService = menuPrivilegeService;
    }

    @Desc(value = "r-查询所有菜单节点", name = "menu-list")
    @GetMapping("/list")
    public String list() {
        List<Menu> allMenus = menuService.findAll();
        return Result.ok().put("menus", allMenus).json();
    }

    @Desc(value = "r-添加一个菜单节点", name = "menu-add")
    @PostMapping("/add")
    public String add(Menu menu) {
        menuService.save(menu);
        return Result.ok().put("bId", menu.getId()).json();
    }

    @Desc(value = "r-修改菜单节点", name = "menu-edit")
    @PostMapping("/modify")
    public String modify(Menu menu) {
        menuService.modifyById(menu);
        return Result.ok().json();
    }

    @Desc(value = "r-修改菜单节点名称", name = "menu-edit")
    @PostMapping("/rename")
    public String rename(Menu menu) {
        menuService.modifyNameById(menu.getId(), menu.getName());
        return Result.ok().json();
    }

    /**
     * 删除菜单节点的时候, 需要节点之间的 treeId 和 treeParentId 之间的关系
     * @param ids: 需要删除的 id 集合.
     * @param adjusts: 需要调整的节点集合, 格式如下:
     *               <p>
     *                  1(id),1-0-0(treeId),1-0(treeParentId),1(isLeaf),0(orderIndex);
     *                  2(id),1-1-0(treeId),1-1(treeParentId),0(isLeaf),1(orderIndex);...
     *               </p>
     * @return
     */
    @Desc(value = "r-删除 1~n 个菜单节点", name = "menu-delete")
    @PostMapping("/delete")
    public String delete(String ids, String adjusts) {
        List<String> tl = Arrays.asList(ids.split(","));
        if(CollectionUtils.isEmpty(tl)) {
            return Result.error("参数错误").json();
        }
        List<Long> idList = tl.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Menu> adjustMenus = simpleMenuData2MenuList.apply(adjusts);
        menuService.deleteByIdsAndAdjust(idList, adjustMenus);
        return Result.ok().json();
    }

    /**
     * @param moves: 需要调整的节点集合, 格式如下:
     *               <p>
     *                  1(id),1-0-0(treeId),1-0(treeParentId),1(isLeaf),0(orderIndex);
     *                  2(id),1-1-0(treeId),1-1(treeParentId),0(isLeaf),1(orderIndex);
     *                  ...
     *               </p>
     * @return
     */
    @Desc(value = "r-移动 1~n 个菜单节点位置", name = "menu-move")
    @PostMapping("/move")
    public String move(String moves) {
        List<Menu> adjustMenus = simpleMenuData2MenuList.apply(moves);
        menuService.adjustByMenus(adjustMenus, false);
        return Result.ok().json();
    }

    @Desc(value = "r-根据 menuId 查询相应的权限集合", name = "menu-find-privilege")
    @GetMapping("/pnames/{menuId}")
    public String findHasPrivilegeIds(@PathVariable Long menuId) {
        List<String> pnames = privilegeService.findPrivilegeNamesByMenuId(menuId);
        return Result.ok()
                    .put("pnames", pnames)
                    .json();
    }

    @Desc(value = "r-添加菜单权限映射信息", name = "menu-add-menu-privilege")
    @PostMapping("/add-map")
    public String addMenuPriMap(Long menuId, String privilegeNames) {
        List<MenuPrivilege> mps  = Arrays.stream(privilegeNames.split("\\s*\\,\\s*"))
                .map(pname -> {
                    MenuPrivilege menuPrivilege = new MenuPrivilege();
                    menuPrivilege.setMenuId(menuId);
                    menuPrivilege.setPrivilegeName(pname);
                    return menuPrivilege;
                }).collect(Collectors.toList());
        menuPrivilegeService.save(mps);
        return Result.ok().json();
    }

    @Desc(value = "r-根据 menuId 查询菜单信息", name = "menu-edit")
    @GetMapping("/find/{id}")
    public String find(@PathVariable Long id) {
        Optional<Menu> menuOpt = menuService.findById(id);
        if(menuOpt.isPresent()) {
            return Result.ok().put("menu", menuOpt.get()).json();
        }
        return Result.error("不存在").json();
    }
}
