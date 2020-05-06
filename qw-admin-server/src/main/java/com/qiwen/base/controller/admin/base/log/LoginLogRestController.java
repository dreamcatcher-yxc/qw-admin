package com.qiwen.base.controller.admin.base.log;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.dto.LoginLogQueryDTO;
import com.qiwen.base.service.ILoginLogService;
import com.qiwen.base.util.Result;
import com.qiwen.base.vo.LoginLogVO;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.qiwen.base.util.LayUtil.toLayPageJson;
import static com.qiwen.base.util.LayUtil.toPageable;

@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "登录日志管理", groupName = QWConstant.MODULE_GROUP_NAME + "login-log-admin")
@RestController()
@RequestMapping("/admin/base/login-log/rest")
public class LoginLogRestController {

    private final ILoginLogService loginLogService;

    public LoginLogRestController(ILoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Desc(value = "r-查看列表", name = "login-log-list")
    @GetMapping("/list")
    public String index(LoginLogQueryDTO queryDTO, int page, int limit) {
        Page<LoginLogVO> result = loginLogService.findByCondition(queryDTO, toPageable(page, limit));
        return toLayPageJson(result);
    }

    @Desc(value = "r-删除用户登录日志", name = "login-log-delete")
    @GetMapping("/delete/{ids}")
    public String delete(@PathVariable String ids) {
        List<String> tl = Arrays.asList(ids.split(","));
        if(CollectionUtils.isEmpty(tl)) {
            return Result.error("参数错误").json();
        }
        List<Long> idList = tl.stream().map(Long::valueOf).collect(Collectors.toList());
        loginLogService.deleteByIds(idList.toArray(new Long[idList.size()]));
        return Result.ok().json();
    }
}
