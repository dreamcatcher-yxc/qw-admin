package com.qiwen.yjyx.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.OperateLog;
import com.qiwen.base.util.Result;
import com.qiwen.base.util.SystemUtil;
import com.qiwen.yjyx.config.YJYXConstant;
import com.qiwen.yjyx.entity.YJFoo;
import com.qiwen.yjyx.service.IYJFooService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yangxiuchu
 * @since 2020-03-13
 */
@RestController
@RequestMapping(YJYXConstant.APP_URL_PREFIX + "/foo")
public class YJFooController {

    private final IYJFooService fooService;

    public YJFooController(IYJFooService fooService) {
        this.fooService = fooService;
    }

    @GetMapping("/find-all")
    public String findAll(HttpServletRequest request) {
        OperateLog operateLog = (OperateLog) request.getAttribute(Desc.CUSTOM_ATTR_NAME);
        operateLog.setOperation(SystemUtil.currentLoginUser().getUsername());

        // 工号、姓名、部门、岗位、口令
        String[] queryFields = { "id", "foo1", "foo2" };
        QueryWrapper<YJFoo> queryWrapper = new QueryWrapper<>();

        queryWrapper.select(queryFields)
                .orderByAsc("id");

        List<YJFoo> fooList = this.fooService.list(queryWrapper);
        return Result.ok().putData("foos", fooList).json();
    }
}

