package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.WorkLog;
import com.qiwen.base.service.IWorkLogService;
import com.qiwen.base.util.Result;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.qiwen.base.util.LayUtil.toLayPageJson;
import static com.qiwen.base.util.LayUtil.toPageable;

@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "QRTZ工作日志管理", groupName = QWConstant.MODULE_GROUP_NAME + "qrtz-work-log-admin")
@RestController
@RequestMapping("/admin/base/worklog/rest")
public class AdminQRTZWorkLogRestController {

    private final IWorkLogService workLogService;

    public AdminQRTZWorkLogRestController(IWorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @Desc(value = "r-工作日志列表", name = "work-log-list")
    @GetMapping("/list")
    public String index(Date startDate, Date endDate, String jobClass, int page, int limit) {
        Page<WorkLog> result = workLogService.findAll(startDate, endDate, jobClass, toPageable(page, limit));
        return toLayPageJson(result);
    }

    @Desc(value = "r-删除工作日志", name = "work-log-delete")
    @GetMapping("/delete/{ids}")
    public String delete(@PathVariable String ids) {
        List<String> tl = Arrays.asList(ids.split(","));
        if(CollectionUtils.isEmpty(tl)) {
            return Result.error("参数错误").json();
        }
        workLogService.deleteByWorkLogIds(tl.toArray(new String[tl.size()]));
        return Result.ok().json();
    }
}
