package com.qiwen.base.config.web;

import com.qiwen.base.util.http.HttpUtil;
import org.springframework.web.servlet.View;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class TextPlainView implements View {

    private String content;

    public TextPlainView(String content) {
        this.content = content;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpUtil.responseJsonData(response, content);
    }
}