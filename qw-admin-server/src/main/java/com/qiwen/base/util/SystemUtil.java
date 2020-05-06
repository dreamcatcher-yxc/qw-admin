package com.qiwen.base.util;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.service.ISessionManagerService;
import com.qiwen.base.vo.LoginUserVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.session.Session;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yangxiuchu on 2017/8/17.
 */
public class SystemUtil {

    private SystemUtil() {}

    public static boolean isWindow() {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    public static String generateId() {
        int random = (int) (Math.random() * (10000000 - 1000000)) + 1000000;
        return (System.currentTimeMillis() + "" + random).substring(0, 20);
    }

    public static void highlightFiledValueByPattern(Object target, String field, String keyWord, String color) {
        Object filedValue = ReflectUtil.getFieldValue(target, field);
        String targetStr = null;
        if (!(filedValue instanceof String)) {
            return;
        }
        targetStr = (String) filedValue;
        targetStr = targetStr.replaceAll(keyWord, "<span style='color:" + color + "'>" + keyWord + "</span>");
        ReflectUtil.setFieldValue(target, field, targetStr);
    }

    public static void highlightListElementFiledValueByPattern(List beanList, String field, String keyWord) {
        if (CollectionUtils.isEmpty(beanList)) {
            return;
        }
        for (Object bean : beanList) {
            highlightFiledValueByPattern(bean, field, keyWord, "red");
        }
    }

    public static void highlightListElementFiledValueByPatternMap(List beanList, Map<String, String> patternMap, String color) {
        if (CollectionUtils.isEmpty(beanList)) {
            return;
        }
        if (CollectionUtils.isEmpty(beanList) || patternMap.isEmpty()) {
            return;
        }
        for (Object bean : beanList) {
            Set<Map.Entry<String, String>> entrySet = patternMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                highlightFiledValueByPattern(bean, entry.getKey(), entry.getValue(), color);
            }
        }
    }

    /**
     * 当前线程是否存在 SecurityManager
     * @return
     */
    private static boolean hasSecurityManager() {
        try {
            return SecurityUtils.getSecurityManager() != null;
        } catch (UnavailableSecurityManagerException e) {
            return false;
        }
    }

    public static LoginUserVO currentLoginUser() {
        if(hasSecurityManager()) {
            QWAppConfig appConfig = SpringHelper.getRealBean(QWAppConfig.class);
            Object tObj = SecurityUtils.getSubject().getSession().getAttribute(appConfig.getLoginUserKey());
            if(tObj instanceof LoginUserVO) {
                return (LoginUserVO) tObj;
            }
        }
        return null;
    }

    /**
     * 获得登录用户ID 2017-9-12 xxb
     *
     * @return
     */
    public static Long getUserID() {
        LoginUserVO user = currentLoginUser();
        if (user != null && user.getId() != null) {
            return user.getId();
        }
        return -1L;
    }

    /**
     * 获取用户名称
     * @return
     */
    public static String getUserName() {
        LoginUserVO user = currentLoginUser();
        if (user != null && user.getUsername() != null) {
            return user.getUsername();
        }
        return "";
    }

    /**
     * 获取用户的昵称
     * @return
     */
    public static String getLoginUserNickname() {
        LoginUserVO user = currentLoginUser();
        if (user != null && user.getNickname() != null) {
            return user.getNickname();
        }
        return "";
    }

    /**
     * 当前是否处于登录状态
     * @return
     */
    public static final boolean isLogin() {
        return currentLoginUser() != null;
    }

    /**
     * 当前是否处于登录状态
     * @return
     */
    public static final boolean isLogin(boolean autoLogin) {
        LoginUserVO loginUserVO = currentLoginUser();
        if(loginUserVO == null) {
            return false;
        }
        return autoLogin ? loginUserVO.isAutoLogin() : !loginUserVO.isAutoLogin();
    }

    /**
     * 根据用户 ID 判断当前账号是否已经登录.
     * @param userId
     * @return
     */
    public static Session checkUserIsLoginByUserId(Long userId) {
        ISessionManagerService sessionManagerService = SpringHelper.getRealBean(ISessionManagerService.class);
        QWAppConfig appConfig = SpringHelper.getRealBean(QWAppConfig.class);
        final String loginUserKey = appConfig.getLoginUserKey();
        List<Session> sessions = sessionManagerService.findSessions(session -> {
            Object tObj = session.getAttribute(loginUserKey);
            if(tObj instanceof LoginUserVO) {
                return userId.equals(((LoginUserVO)tObj).getId());
            }
            return false;
        });
        int size = sessions.size();
        return size > 0 ? sessions.get(0) : null;
    }

}
