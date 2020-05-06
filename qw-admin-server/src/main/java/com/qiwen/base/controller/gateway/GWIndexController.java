package com.qiwen.base.controller.gateway;

import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Function;

@Controller
@RequestMapping
public class GWIndexController {

    private String docSavePath;

    private Function<String, String> render = route -> "/gateway/" + route;

    @SneakyThrows
    @GetMapping({"", "/index"})
    public String index(Model model, HttpServletRequest request) {
        return render.apply("index");
    }
}
