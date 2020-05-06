package com.qiwen.base.util;

import com.qiwen.base.config.QWAppConfig;

public class PasswordUtil {

    /**
     * 获取加密的私钥
     * @param realPassword
     * @return
     */
    public static String encryptPassword(String realPassword) {
        QWAppConfig appConfig = SpringHelper.getRealBean(QWAppConfig.class);
        return StringUtil.convertToMD5Str(realPassword + appConfig.getPasswordSalt());
    }

}
