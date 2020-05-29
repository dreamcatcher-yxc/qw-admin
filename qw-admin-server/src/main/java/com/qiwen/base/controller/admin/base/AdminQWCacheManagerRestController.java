package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.pojo.QWCacheMeta;
import com.qiwen.base.service.IQWCacheManagerService;
import com.qiwen.base.util.ControllerUtil;
import com.qiwen.base.util.Result;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "缓存管理", groupName = QWConstant.MODULE_GROUP_NAME + "qw-cache-admin")
@RestController
@RequestMapping("/admin/base/cache/rest")
public class AdminQWCacheManagerRestController {

    private final IQWCacheManagerService cacheManagerService;

    public AdminQWCacheManagerRestController(IQWCacheManagerService cacheManagerService) {
        this.cacheManagerService = cacheManagerService;
    }

    @Desc(value = "r-查询所有的缓存信息", name = "qw-cache-list")
    @GetMapping("/list")
    public String index() {
        List<QWCacheMeta> allCacheMetas = this.cacheManagerService.findAllCacheMetas();
        return Result.ok().put("data", allCacheMetas).json();
    }

    @Desc(value = "r-清除指定缓存数据", name = "qw-cache-clear")
    @GetMapping("/clear/{cacheNames}")
    public String delete(@PathVariable String cacheNames) {
        List<String> cacheNameList = ControllerUtil.splitStr2List(cacheNames, name -> name);
        if(CollectionUtils.isEmpty(cacheNameList)) {
            return Result.error("参数错误").json();
        }
        this.cacheManagerService.clearCaches(cacheNameList);
        return Result.ok().json();
    }
}
