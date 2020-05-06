package com.qiwen.base.controller.admin;

import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.entity.FileMap;
import com.qiwen.base.service.IFileMapService;
import com.qiwen.base.util.Result;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.shiro.authz.annotation.RequiresGuest;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 图片上传
 */
@RestController
@RequestMapping("/imgs/rest/")
public class AdminImgRestController {

    private final IFileMapService fileMapService;

    public AdminImgRestController(IFileMapService fileMapService) {
        this.fileMapService = fileMapService;
    }

    /**
     * 文件将会被存储在 temp，文件夹里面的文件会被定时清理任务清除掉
     * @param file
     * @return
     */
    @PostMapping("/upload-to-temp")
    public String upload(MultipartFile file) {
        if(!file.isEmpty()) {
            Path path = fileMapService.saveToTempDir(file);
            if(!Objects.isNull(path) && path.toFile().exists()) {
                return Result.ok().put("name", path.toFile().getName()).json();
            }
            return Result.error("文件上传失败").json();
        }
        return Result.error("未读取到上传数据").json();
    }

    /**
     * 文件将会被存储到服务器上传文件存储路径，并且将存储位置保存至数据库
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public String upload2(MultipartFile file) {
        if(!file.isEmpty()) {
            Path path = fileMapService.saveToTempDir(file);
            FileMap realFile = fileMapService.save("", path);
            if(!Objects.isNull(path) && path.toFile().exists()) {
                return Result.ok().put("name", realFile.getFileId()).json();
            }
            return Result.error("文件上传失败").json();
        }
        return Result.error("未读取到上传数据").json();
    }

    /**
     *  保存图片到服务器, 按照长宽对该图片进行缩放.
     * @param file: 客户端上传文件.
     * @param width: 图片宽, 默认: 300px
     * @param height: 图片高, 默认: 300px
     * @return
     */
    @RequiresUser
    @PostMapping("/upload/shrink")
    public String resize(MultipartFile file, @RequestParam(defaultValue = "300") int width, @RequestParam(defaultValue = "300") int height) throws IOException {
        if(!file.isEmpty()) {
            Path path = fileMapService.saveToTempDir(file);
            if(!Objects.isNull(path) && path.toFile().exists()) {
                // 裁剪图片
                Thumbnails.of(path.toFile())
                        .size(width, height)
                        .keepAspectRatio(false)
                        .toFile(path.toFile().getAbsoluteFile());
                return Result.ok().put("name", path.toFile().getName()).json();
            }
            return Result.error("文件上传失败").json();
        }
        return Result.error("未读取到上传数据").json();
    }
}
