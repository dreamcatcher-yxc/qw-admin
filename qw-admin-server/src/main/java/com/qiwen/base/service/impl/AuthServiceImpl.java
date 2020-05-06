package com.qiwen.base.service.impl;

import com.qiwen.base.entity.Privilege;
import com.qiwen.base.exception.SystemException;
import com.qiwen.base.service.IAuthService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class AuthServiceImpl implements IAuthService {

//    private final ScriptEngine scriptEngine;
    private final AntPathMatcher antPathMatcher;

    private final ExpressionParser expressionParser;

    public AuthServiceImpl() {
//        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
//        scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
        expressionParser = new SpelExpressionParser();
        antPathMatcher = new AntPathMatcher();
    }

    public boolean calculatePermissionExpression(String expression, boolean isUrlExpression, List<Privilege> privilegeList) {
        List<String> canAccessUrlPatternList = convertPrivileges(privilegeList, isUrlExpression);
        if(isUrlExpression) {
            expression = convertExpression(expression, privilegeList);
        }
        if(CollectionUtils.isEmpty(canAccessUrlPatternList) || StringUtils.isEmpty(expression)) {
            return false;
        }
        String cleanExpression = expression.replaceAll("\\s+", "");
        String reg = isUrlExpression ? "[\\w-/\\*\\.\\,]+" : "[a-zA-Z0-9]+(\\-[a-zA-Z0-9]+)*";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(cleanExpression);
        while (matcher.find()) {
            cleanExpression = StringUtils.replaceOnce(cleanExpression, matcher.group(), hasPermission(matcher.group(), canAccessUrlPatternList) ? "true" : "false");
        }

        // 去除表达式中的空格.
        try {
            String newEvalExp = cleanExpression
                    .replaceAll("\\&", "&&")
                    .replaceAll("\\|", "||");
            Expression elExpression = expressionParser.parseExpression(newEvalExp);
            boolean evalResult = (Boolean)elExpression.getValue();
            return evalResult;
        } catch (Exception e) {
            log.error("转换失败, cause: {}, message: {}", e.getCause(), e.getMessage());
            if(e instanceof ClassCastException) {
                throw new SystemException(String.format("转换结果不能强行转换为 Boolean 类型"));
            }
            if(e instanceof ParseException) {
                throw new SystemException(String.format("SpringEL 表达式转换失败"));
            }
            throw new SystemException("系统权限服务权限校验失败");
        }
    }

    private boolean hasPermission(String uri, List<String> canAccessUrlPatternList) {
        for (String urlPattern : canAccessUrlPatternList) {
            if(this.antPathMatcher.match(urlPattern, uri)) {
                return true;
            }
        }
        return false;
    }

    // 转换资源到集合, 去重
    private List<String> convertPrivileges(List<Privilege> privilegeList, boolean isGetUrl) {
        if(CollectionUtils.isEmpty(privilegeList)) {
            return null;
        }
        Set<String> privilegeResSet = new HashSet<>(privilegeList.size());
        privilegeList.forEach(pri-> {
            if(isGetUrl) {
                String urls = pri.getUrls();
                if(StringUtils.isNotEmpty(urls)) {
                    privilegeResSet.addAll(Arrays.asList(urls.split("\\s*\\,\\s*")));
                }
            } else {
                privilegeResSet.add(pri.getName());
            }
        });
        List<String> privilegeResList = new ArrayList<>(privilegeResSet.size());
        privilegeResSet.forEach((res) -> privilegeResList.add(res));
        return privilegeResList;
    }

    private String convertExpression(String expression, List<Privilege> hasPrivilegeList) {
        expression = expression.replaceAll("\\s*", "");
        String reg = "[\\w-/\\*\\.]+";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(expression);
        StringBuffer evalExp = new StringBuffer();

        while (matcher.find()) {
            String alias = matcher.group();
            Optional<String> opt = hasPrivilegeList.stream()
                    .filter(pri -> pri.getName().equals(alias))
                    .map(pri -> {
                        String urls = pri.getUrls();
                        return "(" + urls.replaceAll("\\s*\\,\\s*", "|") + ")";
                    })
                    .findFirst();
            if(opt.isPresent()) {
                matcher.appendReplacement(evalExp, opt.get());
            }
        }
        return evalExp.toString();
    }

}
