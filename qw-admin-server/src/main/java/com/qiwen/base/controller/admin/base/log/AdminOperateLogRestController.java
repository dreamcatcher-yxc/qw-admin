package com.qiwen.base.controller.admin.base.log;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.OperateLog;
import com.qiwen.base.service.IOperateLogService;
import com.qiwen.base.util.LayUtil;
import com.qiwen.base.util.Result;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 操作日志管理
 */
@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "系统操作日志", groupName = QWConstant.MODULE_GROUP_NAME + "operator-log-admin")
@RestController
@RequestMapping("/admin/base/operate/log/rest")
public class AdminOperateLogRestController {

    private final IOperateLogService olService;

    public AdminOperateLogRestController(IOperateLogService olService) {
        this.olService = olService;
    }

    @Desc(value = "r-分页按条件查询操作日志信息", name = "operate-log-list")
    @GetMapping("/list")
    public String index(OperateLog operateLog, Date start, Date end, int page, int limit) {
        Page<OperateLog> res = olService.findAll(operateLog, start, end, LayUtil.toPageable(page, limit));
        return LayUtil.toLayPageJson(res);
    }

    @Desc(value = "r-删除 1~n 条操作日志信息", name = "operate-log-delete")
    @GetMapping("/delete/{ids}")
    public String delete(@PathVariable String ids) {
        if(StringUtils.isEmpty(ids)) {
            return Result.error().json();
        }
        String[] arr = ids.split("\\s*\\,\\s*");
        List<Long> idList = Arrays.stream(arr)
                .map(id -> Long.parseLong(id))
                .collect(Collectors.toList());
        olService.delete(idList.toArray(new Long[idList.size()]));
        return Result.ok().json();
    }
}
