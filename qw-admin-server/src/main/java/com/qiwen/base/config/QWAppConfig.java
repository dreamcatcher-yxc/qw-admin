package com.qiwen.base.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties("qw.constant.web")
public class QWAppConfig {

    @Data
    public static class ExternalWebResource {

        private String[] resourceHandlers;

        private String[] resourceLocations;

        // CacheControlBean 名称
        private String cacheControlBeanName;
    }

    @Data
    public static class Ip2Region {
        private String dbPath;

        // B-tree | memory | Binary
        private String algorithm;
    }

    private String loginUserKey;

    private String passwordSalt;

    private String defaultErrorPage;

    // 服务器上传文件临时存储路径
    private String fileTempDir;

    // 服务器文件存储路径
    private String fileSaveDir;

    // 公共文档存储路径，该路径下的文件数据库不做映射保存，文件名称由程序控制.
    private String publicDocDir;

    // 外部静态资源配置列表
    private List<ExternalWebResource> externalWebResources;

    // 服务器定位图片的基础路径
    private String imgViaBasePath;

    // 默认配置根路径
    private String configBaseDir;

    // 是否生成超级用户信息, 默认不生成.
    private boolean generateSuperUserInfo;

    // 超级管理员用户名
    private String superUsername;

    // 超级管理员默认密码
    private String superUserPwd;

    // 配置权限配置扫描的包
    private List<String> privilegeScanPackages;

    // quartz job 扫描包集合
    private List<String> quartzJobScanPackages;

    // 应用名称
    private String applicationName;

    // ip2Region 相关配置
    private Ip2Region ip2Region;

    // 日志文件存储路径
    private String logPath;

    // 同一用户同时在线数量, -1 表示不限制
    private int maxLoginQuantityOfSameUser = 1;
}
