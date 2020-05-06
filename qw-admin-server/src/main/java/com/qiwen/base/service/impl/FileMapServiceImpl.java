package com.qiwen.base.service.impl;

import com.google.common.io.Files;
import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.entity.FileMap;
import com.qiwen.base.entity.QFileMap;
import com.qiwen.base.exception.NotFoundException;
import com.qiwen.base.exception.SystemException;
import com.qiwen.base.repository.FileMapRepository;
import com.qiwen.base.service.IFileMapService;
import com.qiwen.base.util.FileUtil;
import com.qiwen.base.util.QueryDSLUtil;
import com.qiwen.base.util.StringUtil;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.qiwen.base.config.QWConstant.CACHE_FILE_MAP_KEY;

@Slf4j
@Service
public class FileMapServiceImpl implements IFileMapService {

    private final FileMapRepository fileMapRepo;

    private final JPAQueryFactory factory;

    private final String fileTempDir;

    private final String fileSaveDir;

    private final String tempSuffix = "temp";

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH");

    public FileMapServiceImpl(FileMapRepository fileMapRepo, JPAQueryFactory factory, QWAppConfig appConfig) {
        this.fileMapRepo = fileMapRepo;
        this.factory = factory;
        this.fileTempDir = appConfig.getFileTempDir();
        this.fileSaveDir = appConfig.getFileSaveDir();
    }

    private String generateSaveFileName() {
        return sdf.format(new Date()) + "_" + RandomUtils.nextLong(1000 * 1000, 1000 * 10000);
    }

    /**
     * 根据存储文件的根路径、文件无后缀名称、文件后缀，文件扩展名生成文件保存的路径。
     *
     * @param root:               文件存储的根路径
     * @param fileNameWithoutExt: 文件无后缀名称
     * @param suffix:             文件后缀, 为 null 表示不用添加后缀
     * @param ext:                保存的文件的扩展名.
     * @return
     */
    private String generateSavePathByName(String root, String fileNameWithoutExt, String suffix, String ext) {
        String[] paths = new String[5];
        paths[0] = root;
        String[] tArr = fileNameWithoutExt.split("_");
        int t = 1;

        for (int i = 0; i < 2; i++) {
            paths[t++] = tArr[i];
        }

        paths[t++] = StringUtils.leftPad((Integer.valueOf(tArr[2]) % 2000) + "", 4, "0");
        File tf = new File(FileUtil.combination(paths));

        if (!tf.exists()) {
            tf.mkdirs();
        }

        paths[t] = fileNameWithoutExt + ((suffix == null) ? "" : ("_" + suffix));
        return FileUtil.combination(paths) + "." + ext;
    }

    /**
     * 随机生成一个临时文件存放目录, 该目录的第一层标识
     */
    private Path generateSaveDir(String root) {
        String[] paths = new String[3];
        paths[0] = root;
        paths[1] = StringUtil.dateConvertString(new Date(), StringUtil.DateFormatPattern.DATE);
        paths[2] = StringUtil.uuid();
        String dir = FileUtil.combination(paths);
        File fDir = new File(dir);
        if (fDir.isFile()) {
            fDir.delete();
        }
        if (!fDir.exists()) {
            fDir.mkdirs();
        }
        return fDir.toPath();
    }

    /**
     * 此操作在一个事物中运行.
     *
     * @param realName: 文件真实名称, 不需要保存传递为 null 即可.
     * @param path:     真实的文件, 不能为文件夹.
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public FileMap save(String realName, Path path) {
        QFileMap qfm = QFileMap.fileMap;
        String srcFileMd5 = FileUtil.getMd5(path);
        FileMap fileMap = new FileMap();
        fileMap.setFileId(StringUtil.uuid());
        fileMap.setMd5(srcFileMd5);
        fileMap.setIsLink(0);

        // 保存该文件
        File srcFile = path.toFile();
        String ext = Files.getFileExtension(srcFile.getName());
        String name = generateSaveFileName();
        String logicSaveDir = generateSavePathByName("", name, null, ext);

        fileMap.setName(name + "." + ext);
        fileMap.setRealName(!Objects.isNull(realName) ? realName : "");
        fileMap.setPath(logicSaveDir);

        try {
            fileMap.setType(java.nio.file.Files.probeContentType(path));
        } catch (IOException e) {
            log.error("获取文件类型失败, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new NotFoundException(String.format("获取文件类型失败"));
        }

        fileMapRepo.save(fileMap);

        try {
            String realSavePath = generateSavePathByName(fileSaveDir, name, null, ext);
            Files.copy(srcFile, new File(realSavePath));
        } catch (IOException e) {
            log.error("文件保存失败, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new SystemException("文件保存失败");
        }
        return fileMap;
    }

    @Override
    public Path saveToTempDir(Path path) {
        File srcFile = path.toFile();
        String ext = Files.getFileExtension(srcFile.getName());
        String nameWithoutExt = generateSaveFileName();
        String realTempSavePath = generateSavePathByName(fileTempDir, nameWithoutExt, tempSuffix, ext);
        File destFile = new File(realTempSavePath);

        try {
            Files.copy(srcFile, destFile);
        } catch (IOException e) {
            log.error("暂存文件保存失败, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new SystemException("暂存文件保存失败");
        }
        return destFile.toPath();
    }

    @Override
    public Path saveToTempDir(MultipartFile file) {
        String ext = Files.getFileExtension(file.getOriginalFilename());
        String nameWithoutExt = generateSaveFileName();
        String realTempSavePath = generateSavePathByName(fileTempDir, nameWithoutExt, tempSuffix, ext);
        File destFile = new File(realTempSavePath);

        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            log.error("暂存文件保存失败, cause: {}, message: {}", e.getCause(), e.getMessage());
            throw new SystemException("暂存文件保存失败");
        }
        return destFile.toPath();
    }

    @Transactional
    @Override
    public void deleteByMd5s(String... md5s) {
        QFileMap qfm = QFileMap.fileMap;
        List<FileMap> paths = factory.selectFrom(qfm)
                .where(qfm.md5.in(md5s))
                .fetch();

        if (CollectionUtils.isEmpty(paths)) {
            return;
        }

        factory.delete(qfm)
                .where(qfm.md5.in(md5s))
                .execute();

        paths.stream()
                .filter(fm -> fm.getIsLink() == 0)
                .forEach(fm -> {
                    File realFile = new File(fm.getPath());
                    if (realFile.exists() && realFile.isFile()) {
                        realFile.delete();
                    }
                });
    }

    @Override
    public Path findTempFile(String tempFileName) {
        String extension = Files.getFileExtension(tempFileName);
        String nameWithoutExtension = Files.getNameWithoutExtension(tempFileName);
        String pathSeed = nameWithoutExtension.substring(0, nameWithoutExtension.lastIndexOf("_"));
        String realPath = generateSavePathByName(fileTempDir, pathSeed, tempSuffix, extension);
        File realTempFile = new File(realPath);

        if (realTempFile.exists() && !realTempFile.isDirectory()) {
            return realTempFile.toPath();
        }

        return null;
    }

    /**
     * 此操作在一个新的事务中运行
     *
     * @param fileIds
     */
    @CacheEvict(value = CACHE_FILE_MAP_KEY, allEntries = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void deleteByFileIds(String... fileIds) {
        QFileMap qfm = QFileMap.fileMap;
        List<FileMap> fms = factory.selectFrom(qfm)
                .where(qfm.fileId.in(fileIds))
                .fetch();

        if (CollectionUtils.isEmpty(fms)) {
            return;
        }

        factory.delete(qfm)
                .where(qfm.fileId.in(fileIds))
                .execute();

        fms.stream()
                .forEach(fm -> {
                    File realFile = new File(fm.getPath());
                    if (realFile.exists() && realFile.isFile()) {
                        realFile.delete();
                    }
                });
    }

    @Cacheable(value = CACHE_FILE_MAP_KEY, key = "'fileId:' + #fileId")
    @Override
    public Path findById(String fileId) {
        QFileMap qfm = QFileMap.fileMap;
        FileMap fm = factory.selectFrom(qfm)
                .where(qfm.fileId.eq(fileId))
                .fetchFirst();

        if (Objects.isNull(fm)) {
            return null;
        }

        // 处理图片路径问题
        this.convertFileMapPath(fm);
        File realFile = new File(fm.getPath());

        if (!realFile.exists()) {
            return null;
        }

        return realFile.toPath();
    }

    /**
     * 将 FileMap.path 转换为实际实际存储路径
     * @param fileMap
     */
    private void convertFileMapPath(FileMap fileMap) {
        String path = fileMap.getPath();
        fileMap.setPath(FileUtil.combination(fileSaveDir, path));
    }

    @Override
    public Page<FileMap> findByCondition(String fileName, Pageable pageable) {
        QFileMap qfm = QFileMap.fileMap;
        JPAQuery<FileMap> query = factory.selectFrom(qfm)
                .where(qfm.realName.like("%" + fileName + "%"));
        Page<FileMap> result = QueryDSLUtil.page(query, pageable);
        if(result.getTotalElements() <= 0) {
            return result;
        }
        result.getContent()
                .forEach(this::convertFileMapPath);
        return result;
    }

    @Override
    public boolean isTempFile(String name) {
        return name != null && Files.getNameWithoutExtension(name).endsWith(tempSuffix);
    }

    @Override
    public Path generateNewTempPath(String ext) throws IOException {
        return generateNewTempFile(ext);
    }

    @Override
    public Path generateNewTempFile(String ext) throws IOException {
        String nameWithoutExt = generateSaveFileName();
        String realTempSavePath = generateSavePathByName(fileTempDir, nameWithoutExt, tempSuffix, ext);
        File file = new File(realTempSavePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        return Paths.get(realTempSavePath);
    }

    @Override
    public Path generateNewTempDir() throws IOException {
        Path path = generateSaveDir(fileTempDir);
        return path;
    }
}
