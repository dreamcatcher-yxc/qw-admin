package com.qiwen.base.config.annotaion;

import java.lang.annotation.*;

/**
 * @author yangxiuchu
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Desc {

    String CUSTOM_ATTR_NAME = "qwCustomOperateLogAttributeKey";

    /**
     * 描述信息
     * @return
     */
    String value() default "";

    /**
     * 类级别描述信息, 建议使用 [a-z0-9]+-[a-z0-9]+ 格式。
     * @return
     */
    String groupName() default "";

    /**
     * 类级别描述信息, 如果没有, 默认为 Desc.value()
     * @return
     */
    String groupDesc() default "";

    /**
     * 类级别日志描述信息
     * @return
     */
    String logGroupName() default "";

    /**
     * 方法级别权限描述信息，如果没有, 默认为 Desc.value()
     * @return
     */
    String desc() default "";

    /**
     * 方法级别权限名称，建议使用 [a-z0-9]+-[a-z0-9]+ 格式。
     * @return
     */
    String name() default "";


    /**
     * 标志是权限是否需要拦截, 如果类上的 Desc 设置为 false, 则类型的所有方法都不拦截.
     * @return
     */
    boolean requiredPermission() default true;

    /**
     * 标志
     * @return
     */
    String log() default "";

    /**
     * 是否自定义, 开启之后将会把生成的 OperateLog 作为参数注入 Controller method,
     * 注入名称为
     */
    boolean customLog() default false;

    /**
     * 标志该 类 或者 方法 是否需要向数据库中记录日志。
     * @return
     */
    boolean requiredLog() default true;

    /**
     * 该日志信息是否需要保存至数据库
     * @return
     */
    boolean requiredSaveToDB() default true;

    /**
     * 保存至日志包含的参数信息
     * @return
     */
    String[] logIncludeParams() default {};

    /**
     * 保存至日志不包含的参数信息(当且仅当 {@code  Desc.logIncludeParams } 为 null 或者空数组)
     * @return
     */
    String[] logExcludeParams() default {};
}