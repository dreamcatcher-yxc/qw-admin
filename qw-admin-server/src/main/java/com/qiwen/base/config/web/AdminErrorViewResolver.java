package com.qiwen.base.config.web;

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 自定义 SpringMVC 系统错误处理。
 */
@Configuration
public class AdminErrorViewResolver implements ErrorViewResolver {

	@Override
	public ModelAndView resolveErrorView(HttpServletRequest request,
										 HttpStatus status, Map<String, Object> model) {
		ModelAndView mv = new ModelAndView();

		if(status.equals(HttpStatus.NOT_FOUND)) {
			mv.setViewName("/error/404");
		} else if(status.equals(HttpStatus.BAD_REQUEST)) {
			mv.setViewName("/error/404");
		} else {
			mv.setViewName("/error/5XX");
		}
		return mv;
	}

}