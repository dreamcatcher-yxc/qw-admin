package com.qiwen.base.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.function.Function;

/**
 * 错误页面映射
 */
@Controller
@RequestMapping("/admin/error")
public class AdminErrorViewController {

    private final Function<String, String> render = route -> "/error/" + route;

    @RequestMapping("/400")
    public String to400() {
        return render.apply("400");
    }

    @RequestMapping("/403")
    public String to403() {
        return render.apply("403");
    }

    @RequestMapping("/404")
    public String to404() {
        return render.apply("404");
    }

    @RequestMapping("/5XX")
    public String to5XX() {
        return render.apply("5XX");
    }

}
