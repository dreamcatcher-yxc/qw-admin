package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.dto.OfflineUserDTO;
import com.qiwen.base.service.*;
import com.qiwen.base.util.ControllerUtil;
import com.qiwen.base.util.ReflectUtil;
import com.qiwen.base.util.Result;
import com.qiwen.base.util.http.HttpUtil;
import com.qiwen.base.vo.LoginUserVO;
import com.qiwen.base.vo.OnlineUserVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.Util;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.qiwen.base.util.LayUtil.toLayPageJson;
import static com.qiwen.base.util.LayUtil.toPageable;

@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "在线用户管理", groupName = QWConstant.MODULE_GROUP_NAME + "online-user-admin")
@RestController
@RequestMapping("/admin/base/online-user/rest")
public class AdminOnlineUserRestController {

    private final IUserService userService;

    private final IRoleService roleService;

    private final ISessionManagerService sessionManagerService;

    private final IIp2RegionService ip2RegionService;

    private final IForceOfflineUserService forceOfflineUserService;

    private final QWAppConfig appConfig;


    public AdminOnlineUserRestController(IUserService userService,
                                         IRoleService roleService,
                                         ISessionManagerService sessionManagerService,
                                         IIp2RegionService ip2RegionService,
                                         IForceOfflineUserService forceOfflineUserService,
                                         QWAppConfig appConfig) {
        this.userService = userService;
        this.roleService = roleService;
        this.sessionManagerService = sessionManagerService;
        this.ip2RegionService = ip2RegionService;
        this.forceOfflineUserService = forceOfflineUserService;
        this.appConfig = appConfig;
    }

    @Desc(value = "r-在线用户列表", name = "online-user-list")
    @GetMapping("/list")
    public String index(String username, int page, int limit) {
        Pageable pageable = toPageable(page, limit);

        Page<Session> sessionPage = sessionManagerService.findSessions(
                session -> {
                    LoginUserVO loginUserVO = (LoginUserVO) session.getAttribute(appConfig.getLoginUserKey());
                    if(loginUserVO == null) {
                        return false;
                    }
                    if(StringUtils.isNotEmpty(username)) {
                        return loginUserVO.getUsername().contains(username);
                    }
                    return true;
                },
                pageable,
                null
        );

        final String currentSessionId = (String) SecurityUtils.getSubject().getSession().getId();
        List<OnlineUserVO> logUserVOS = sessionPage.stream()
                .map(session -> {
                    LoginUserVO loginUserVO = (LoginUserVO) session.getAttribute(appConfig.getLoginUserKey());
                    OnlineUserVO onlineUserVO = ReflectUtil.O2ObySameField(loginUserVO, OnlineUserVO.class);
                    onlineUserVO.setLastAccessDate(session.getLastAccessTime());
                    onlineUserVO.setSessionId((String) session.getId());
                    String ipAddress = HttpUtil.getIpAddress();
                    onlineUserVO.setIp(ipAddress);
                    onlineUserVO.setCurrentUser(currentSessionId.equals(onlineUserVO.getSessionId()));
                    if (Util.isIpAddress(ipAddress)) {
                        try {
                            DataBlock dataBlock = ip2RegionService.ip2Region(ipAddress);
                            onlineUserVO.setLoginRegion(dataBlock.toString());
                        } catch (IOException e) {
                        }
                    }
                    onlineUserVO.setPassword(null);
                    return onlineUserVO;
                }).collect(Collectors.toList());

        Page<OnlineUserVO> pageResult = new PageImpl<>(logUserVOS, pageable, sessionPage.getTotalElements());

        return toLayPageJson(pageResult);
    }

    @Desc(value = "r-踢出用户", name = "online-user-delete")
    @GetMapping("/delete/{ids}")
    public String delete(@PathVariable String ids) {
        List<String> idList = Arrays.asList(ControllerUtil.splitStr2Array(ids, str -> str));
        sessionManagerService.findSessions(
                session -> session.getAttribute(appConfig.getLoginUserKey()) != null && idList.contains(session.getId())
        ).forEach(session -> {
            session.stop();
            LoginUserVO loginUserVO = (LoginUserVO) session.getAttribute(appConfig.getLoginUserKey());
            OfflineUserDTO offlineUserDTO = new OfflineUserDTO();
            offlineUserDTO.setUsername(loginUserVO.getUsername());
            offlineUserDTO.setOfflineType(1);
            offlineUserDTO.setCount(1);
            forceOfflineUserService.save(offlineUserDTO);
        });
        this.sessionManagerService.validateSessions();
        return Result.ok().json();
    }

}
