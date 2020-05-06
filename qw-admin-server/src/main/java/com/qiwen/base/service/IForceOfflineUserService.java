package com.qiwen.base.service;

import com.qiwen.base.dto.OfflineUserDTO;

import java.util.List;

/**
 * 强制下线用户管理服务
 */
public interface IForceOfflineUserService {

    /**
     * 添加一直强制下线用户信息
     * @param offlineUserDTO
     */
    void save(OfflineUserDTO offlineUserDTO);

    /**
     * 根据 Usernames 删除
     * @param usernames
     */
    void deleteByUsernames(String... usernames);

    /**
     * 处理强制下线用户信息, 如果返回 true, 表示成功, 后续操作可以继续, 否则后续操作跳过。<br>
     * <strong>注意: 此方法应当在登录认证信息校验完成之后，保存用户信息至 session 之前调用</strong>
     * @param username
     * @param needAutoCountDownAndDelete OfflineUserDTO.count 是否需要自动减1,减小到 0 的时候自动删除当前 OfflineUserDTO
     */
    boolean handleOfflineUser(String username, boolean needAutoCountDownAndDelete);

    /**
     * 如果不传递 username, 否则返回所有.
     * @param usernames
     */
    List<OfflineUserDTO> findByUsernames(String... usernames);
}
