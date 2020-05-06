package com.qiwen.base.config.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.pam.AuthenticationStrategy;
import org.apache.shiro.realm.Realm;

import java.util.Collection;

/**
 * 自定义多个 Realm 鉴权规则处理
 */
public class QWAuthenticationStrategyImpl implements AuthenticationStrategy {

//    private List String AuthenticationStrategy;

    @Override
    public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
        return null;
    }

    @Override
    public AuthenticationInfo beforeAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        return aggregate;
    }

    @Override
    public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
        // 有异常, 直接抛出, 后续校验不再进行
        if(t != null) {
            if(t instanceof RuntimeException) {
                RuntimeException e = (RuntimeException) t;
                throw e;
            }
            throw new AuthenticationException(t.getMessage());
//            throw new SystemException(t.getMessage());
        }
        if(singleRealmInfo != null && aggregateInfo == null) {
            return singleRealmInfo;
        }
        if(singleRealmInfo != null && aggregateInfo instanceof SimpleAuthenticationInfo) {
            SimpleAuthenticationInfo mergeableAutenticationInfo = (SimpleAuthenticationInfo) aggregateInfo;
            mergeableAutenticationInfo.merge(singleRealmInfo);
            return mergeableAutenticationInfo;
        }
        return aggregateInfo;
    }

    @Override
    public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregate) throws AuthenticationException {
        return aggregate;
    }
}
