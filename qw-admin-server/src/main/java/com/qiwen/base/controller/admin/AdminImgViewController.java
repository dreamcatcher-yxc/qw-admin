package com.qiwen.base.controller.admin;

import com.qiwen.base.service.IFileMapService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/imgs/view/")
public class AdminImgViewController {

    private final IFileMapService fileMapService;

    public AdminImgViewController(IFileMapService fileMapService) {
        this.fileMapService = fileMapService;
    }

    @GetMapping(value = "/find/{via}", produces = {MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody
    public ResponseEntity<byte[]> findImage(HttpServletResponse response, @PathVariable String via) throws IOException {
        Path path;
        String filenameWithoutName = com.google.common.io.Files.getNameWithoutExtension(via);

        if(filenameWithoutName.endsWith("temp")) {
            path = fileMapService.findTempFile(via);
        } else {
            path = fileMapService.findById(via);
        }
        byte[] bytes = path == null ? new byte[]{} : Files.readAllBytes(path);
        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(365, TimeUnit.DAYS))
                .body(bytes);
    }

}
