package com.qiwen.base.config.thymeleaf;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.HashSet;
import java.util.Set;

public class SystemDialect extends AbstractProcessorDialect {

    public static final String AUTH_ATTRIBUTE_TAG_NAME = "has";

    public static final String NOT_AUTH_ATTRIBUTE_TAG_NAME = "not";

    public static final String USER_ATTRIBUTE_TAG_NAME = "user";

    public static final String USER_CUSTOMER_TAG_PREFIX= "uc";

    public static final String USER_ATTRIBUTE_TAG_PREFIX = "sys";

    private final AuthenticationAttributeTagProcessor authenticationElementTagProcessor;

    private final NotAuthenticationAttributeTagProcessor notAuthenticationAttributeTagProcessor;

    private final UserAttributeTagProcessor userElementTagProcessor;

    public SystemDialect(AuthenticationAttributeTagProcessor authenticationElementTagProcessor,
                         NotAuthenticationAttributeTagProcessor notAuthenticationAttributeTagProcessor,
                         UserAttributeTagProcessor userElementTagProcessor) {
        super("QW-ADMIN", "QW", StandardDialect.PROCESSOR_PRECEDENCE);
        this.authenticationElementTagProcessor = authenticationElementTagProcessor;
        this.notAuthenticationAttributeTagProcessor = notAuthenticationAttributeTagProcessor;
        this.userElementTagProcessor = userElementTagProcessor;
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<>();
        processors.add(authenticationElementTagProcessor);
        processors.add(notAuthenticationAttributeTagProcessor);
        processors.add(userElementTagProcessor);
        return processors;
    }
}