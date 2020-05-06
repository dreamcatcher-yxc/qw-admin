package com.qiwen.base.config.thymeleaf;

import com.qiwen.base.config.shiro.QWExpressionPermission;
import com.qiwen.base.entity.User;
import com.qiwen.base.util.SystemUtil;
import com.qiwen.base.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.StandardWithTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import static com.qiwen.base.config.thymeleaf.SystemDialect.NOT_AUTH_ATTRIBUTE_TAG_NAME;
import static com.qiwen.base.config.thymeleaf.SystemDialect.USER_ATTRIBUTE_TAG_PREFIX;

@Slf4j
@Component
public class NotAuthenticationAttributeTagProcessor extends AbstractAttributeTagProcessor {

        public NotAuthenticationAttributeTagProcessor() {
            super(
                    TemplateMode.HTML, // 此处理器将仅应用于HTML模式
                    USER_ATTRIBUTE_TAG_PREFIX,     // 要应用于名称的匹配前缀
                    null,              // 无标签名称：匹配任何标签名称
                    false,             // 没有要应用于标签名称的前缀
                    NOT_AUTH_ATTRIBUTE_TAG_NAME,         // 将匹配的属性的名称
                    true,              // 将方言前缀应用于属性名称
                    StandardWithTagProcessor.PRECEDENCE,        // 优先(内部方言自己的优先)
                    true);             // 之后删除匹配的属性
        }

        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
            if(StringUtils.isEmpty(attributeValue)) {
                structureHandler.removeElement();
                return;
            }

            LoginUserVO user = SystemUtil.currentLoginUser();
            // 用户没有登录, 该标签内的所有内容将会不显示
            if(user == null) {
                structureHandler.removeElement();
                return;
            }

            // 有权限访问, 该标签内的所有内容将会不显示
            Subject subject = SecurityUtils.getSubject();
            boolean hasPermission = subject.isPermitted(new QWExpressionPermission(attributeValue, true));
            if(hasPermission) {
                structureHandler.removeElement();
            } else {
                structureHandler.removeTags();
            }
        }
    }