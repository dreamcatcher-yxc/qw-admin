package com.qiwen.base.config.thymeleaf;

import com.qiwen.base.util.SystemUtil;
import com.qiwen.base.vo.LoginUserVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.AbstractAttributeTagProcessor;
import org.thymeleaf.processor.element.IElementTagStructureHandler;
import org.thymeleaf.standard.processor.StandardWithTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;

import java.util.Objects;

import static com.qiwen.base.config.thymeleaf.SystemDialect.USER_ATTRIBUTE_TAG_NAME;
import static com.qiwen.base.config.thymeleaf.SystemDialect.USER_ATTRIBUTE_TAG_PREFIX;

@Component
public class UserAttributeTagProcessor extends AbstractAttributeTagProcessor {

        public UserAttributeTagProcessor() {
            super(
                    TemplateMode.HTML, // 此处理器将仅应用于HTML模式
                    USER_ATTRIBUTE_TAG_PREFIX,     // 要应用于名称的匹配前缀
                    null,              // 无标签名称：匹配任何标签名称
                    false,             // 没有要应用于标签名称的前缀
                    USER_ATTRIBUTE_TAG_NAME,         // 将匹配的属性的名称
                    true,              // 将方言前缀应用于属性名称
                    StandardWithTagProcessor.PRECEDENCE,        // 优先(内部方言自己的优先)
                    true);             // 之后删除匹配的属性
        }

        /**
         *  查看 attributeValue 的出现的形式, 可以分为两种形式,
         *  <ol>
         *      <li> replace-attribute-name(替换标签中的什么属性):need-user-info-name(需要的用户信息)</li>
         *      <li> need-user-name(需要的用户信息)</li>
         *  </ol>
         *  <ol>
         *      <li>
         *          第一种情况, 获取到的用户的信息会被替换为该标签相应的属性值, 如 &lt;span sys:user="text:name"&gt;&lt;/span&gt;会被转换为 &lt;span&gt;zhangsan&lt;/span&gt;
         *      </li>
         *      <li>
         *          第二种情况, 只出现了 need-user-info-name, 则默认替换的属性为 value.
         *      </li>
         *  </ol>
         */
        @Override
        protected void doProcess(ITemplateContext context, IProcessableElementTag tag, AttributeName attributeName, String attributeValue, IElementTagStructureHandler structureHandler) {
            if(StringUtils.isEmpty(attributeValue)) {
                return;
            }

            LoginUserVO user = SystemUtil.currentLoginUser();

            if(Objects.isNull(user)) {
                // 当前没有登录用户, 不做任何处理
                return;
            }

            String expressionParseResult;
            // 如果以 ! 开头, 则后面的部分会按照标准的 thymeleaf 支持的表达式方式解析.
//            if(attributeValue.startsWith("!")) {
//                ApplicationContext appCtx = SpringContextUtils.getApplicationContext(context);
//                final IEngineConfiguration configuration = context.getConfiguration();
//                //获得Thymeleaf标准表达式解析器
//                final IStandardExpressionParser parser =
//                        StandardExpressions.getExpressionParser(configuration);
//                //将属性值解析为Thymeleaf标准表达式
//                final IStandardExpression expression = parser.parseExpression(context, attributeValue.substring(1));
//                //执行刚刚解析的表达式
//                expressionParseResult = (String) expression.execute(context);
//
//            } else {
                expressionParseResult = attributeValue;
//            }

            String[] tArr = expressionParseResult.trim().split("\\s*:\\s*");
            String replaceAttrName, needUserInfoName, needUserInfoVal;

            if(tArr.length == 1) {
                replaceAttrName = "value";
                needUserInfoName = tArr[0];
            } else {
                replaceAttrName = tArr[0];
                needUserInfoName = tArr[1];
            }

            switch (needUserInfoName) {
                case "name":
                    if(!StringUtils.isEmpty(user.getNickname())) {
                        needUserInfoVal = user.getNickname();
                    } else{
                        needUserInfoVal = user.getUsername();
                    }
                    break;

                case "username":
                    needUserInfoVal = user.getUsername();
                    break;

                case "id":
                    needUserInfoVal = String.valueOf(user.getId());
                    break;

                case "email":
                    needUserInfoVal = user.getEmail();
                    break;

                case "phone":
                    needUserInfoVal = user.getPhone();
                    break;

                case "header":
                    needUserInfoVal = user.getHeader();
                    break;

                default:
                    needUserInfoVal = user.getUsername();
            }

            if("text".equals(replaceAttrName)) {
                final IModelFactory modelFactory = context.getModelFactory();
                final IModel model = modelFactory.createModel();
                model.add(modelFactory.createText(needUserInfoVal));
                structureHandler.setBody(model, false);
            } else if("object".equals(replaceAttrName)) {
                //  设置标签内局部变量
                structureHandler.setLocalVariable(needUserInfoName, needUserInfoVal);
            } else {
                structureHandler.setAttribute(replaceAttrName, needUserInfoVal);
            }
        }
    }