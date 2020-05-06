package com.qiwen.base.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QWConstant {
    // 所有的任务类名
    public static final List<Map<String, Object>> JOB_CLASS_NAMES = new ArrayList<>();

    // ehcache 缓存（start）
    // 权限缓存
    public static final String CACHE_PRIVILEGE_KEY = "cache_privilege";
    // 菜单缓存
    public static final String CACHE_MENU_KEY = "cache_menu";
    // 文件查询缓存
    public static final String CACHE_FILE_MAP_KEY = "cache_file_map";
    // 角色缓存
    public static final String CACHE_ROLE_KEY = "cache_role";
    // 用户缓存
    public static final String CACHE_USER_KEY = "cache_user";
    // 用户缓存
    public static final String CACHE_OFFLINE_USER_KEY = "force_offline_users";
    // ehcache 缓存（end）

    // 启动监听器
    public static final int STARTUP_LISTENER_ORDER = 100;

    // 基础分组名称
    public static final String MODULE_GROUP_NAME = "qw-admin > ";

    // 基础分组名称描述信息
    public static final String MODULE_GROUP_NAME_DESC = "基础权限 > ";

    // 配置数据表名前缀
    public static final String DB_TABLE_PREFIX = "sys";

}
