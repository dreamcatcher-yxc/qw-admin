package com.qiwen.base.service;

import com.qiwen.base.entity.FileMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface IFileMapService {

    /**
     * 将文件保存到服务器存储文件路径, 并且将存储的相关信息, 并且返回存储信息 FileMap.
     * @param realName: 文件真实名称, 不需要保存传递为 null 即可.
     * @param path: 真实的文件, 不能为文件夹.
     * @return
     */
    FileMap save(String realName, Path path);

    /**
     * 将当前文件保存到服务器的存储文件的临时目录下面, 该目录下的文件会定期清理.
     * @param path
     * @return
     */
    Path saveToTempDir(Path path);

    /**
     * 将当前文件保存到服务器的存储文件的临时目录下面, 该目录下的文件会定期清理.
     * @param file
     * @return
     */
    Path saveToTempDir(MultipartFile file);

    /**
     * 根据 md5s 删除服务器上的图片.
     * @param md5s
     */
    void deleteByMd5s(String... md5s);

    /**
     * 根据 tempFileName 查找该文件.
     * @param tempFileName
     * @return 如果没有查找到, 则会返回 null.
     */
    Path findTempFile(String tempFileName);

    /**
     * 根据 fileIds 删除服务器上的图片.
     * @param fileIds
     */
    void deleteByFileIds(String... fileIds);

    /**
     * 根据 fileId 查询用户
     * @param fileId
     * @return
     */
    Path findById(String fileId);

    /**
     * 根据文件名判断当前文件是否是暂存文件
     * @param name
     * @return
     */
    boolean isTempFile(String name);

    /**
     * 根据真实文件名模糊分页查询所有满足条件的文件
     * @param fileName 真实文件名
     * @param pageable 分页设置
     * @return 查询结果
     */
    Page<FileMap> findByCondition(String fileName, Pageable pageable);

    /**
     * 随机生成一个暂存文件的保存路径, 该文件是一个空文件
     * @param ext
     * @return
     */
    @Deprecated
    Path generateNewTempPath(String ext) throws IOException;

    /**
     * 随机生成一个暂存文件的保存路径, 该文件是一个空文件
     * @param ext
     * @return
     * @throws IOException
     */
    Path generateNewTempFile(String ext) throws IOException;

    /**
     * 随机生成一个暂存文件的保存路径, 该文件是一个空文件夹
     * @return
     * @throws IOException
     */
    Path generateNewTempDir() throws IOException;
}
