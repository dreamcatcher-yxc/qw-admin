package com.qiwen.base.config.web;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.config.thymeleaf.AuthenticationAttributeTagProcessor;
import com.qiwen.base.config.thymeleaf.NotAuthenticationAttributeTagProcessor;
import com.qiwen.base.config.thymeleaf.SystemDialect;
import com.qiwen.base.config.thymeleaf.UserAttributeTagProcessor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.thymeleaf.context.ILazyContextVariable;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@AutoConfigureBefore(WebConfig.class)
public class PreWebConfig implements ApplicationContextAware {

    @Bean
    public SystemDialect systemDialect(
            AuthenticationAttributeTagProcessor authenticationElementTagProcessor,
            NotAuthenticationAttributeTagProcessor notAuthenticationAttributeTagProcessor,
            UserAttributeTagProcessor userElementTagProcessor
    ) {
        return new SystemDialect(
                authenticationElementTagProcessor,
                notAuthenticationAttributeTagProcessor,
                userElementTagProcessor
        );
    }

    @Bean
    public Validator validator() {
        ValidatorFactory validatorFactory = Validation.byProvider( HibernateValidator.class )
                .configure()
                .addProperty( "hibernate.validator.fail_fast", "true" )
                .buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        return validator;
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor processor = new MethodValidationPostProcessor();
        processor.setValidator(validator());
        return processor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        QWAppConfig appConfig = applicationContext.getBean(QWAppConfig.class);
        ThymeleafViewResolver thymeleafViewResolver = applicationContext.getBean(ThymeleafViewResolver.class);

        if(thymeleafViewResolver != null && appConfig != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("GLOBAL_BASE_IMG_PATH", appConfig.getImgViaBasePath());
            // 该变量可能正在系统运行过程过被修改, 这里使用懒加载的方式, (LazyContextVariable 有缓存, 不满足使用要求)
            vars.put("GLOBAL_ICP_CODE", (ILazyContextVariable<String>) () -> "---");
            vars.put("APPLICATION_NAME", (ILazyContextVariable<String>) appConfig::getApplicationName);
            thymeleafViewResolver.setStaticVariables(vars);
            log.info("thymeleaf 自定义变量初始化完成!");
        } else {
            log.info("thymeleaf 自定义变量初始化失败!");
        }
    }

}
