package com.qiwen.base.config.job;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.entity.FileMap;
import com.qiwen.base.entity.QFileMap;
import com.qiwen.base.entity.WorkLog;
import com.qiwen.base.service.IFileMapService;
import com.qiwen.base.service.IWorkLogService;
import com.qiwen.base.util.FileUtil;
import com.qiwen.base.util.QueryDSLUtil;
import com.qiwen.base.util.SpringHelper;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class FileMapClearJob implements Job {

    public static final String NAME = "无效文件清理任务";

    private final IWorkLogService workLogService;

    private final IFileMapService fileMapService;

    private final JPAQueryFactory factory;

    private final QWAppConfig appConfig;

    public FileMapClearJob() {
        this.workLogService = SpringHelper.getRealBean(IWorkLogService.class);
        this.fileMapService = SpringHelper.getRealBean(IFileMapService.class);
        this.factory = SpringHelper.getRealBean(JPAQueryFactory.class);
        this.appConfig = SpringHelper.getRealBean(QWAppConfig.class);
    }

    private long clear() {
        String activeProfile = SpringHelper.getProperties("spring.profiles.active", String.class, "dev");
        // 测试阶段, 防止误删除电脑上的重要文件.
        if("dev".equalsIgnoreCase(activeProfile) && !"D:\\tmp\\yjyx\\upload".equals(appConfig.getFileTempDir())) {
            return 0;
        }

        QFileMap qfm = QFileMap.fileMap;
        JPAQuery<FileMap> query = factory.selectFrom(qfm);
        List<FileMap> fms = null;
        long invalidCount = 0L;
        int base = 0;

        while (fms == null || !CollectionUtils.isEmpty(fms)) {
            // 一次查询 1000 条记录.
            Page<FileMap> result = QueryDSLUtil.page(query, PageRequest.of(base * 1000, ++base * 1000));
            fms = result.getContent();

            // 过滤掉引用文件
            List<FileMap> notLinkFms = fms.stream()
                    .filter(fm -> fm.getIsLink() != 1)
                    .collect(Collectors.toList());

            // 去除无效文件，既数据库中存在该文件，但是该文件在磁盘上不存在。
            List<String> needRemoveFmRecordMd5 = new ArrayList<>();

            if(!CollectionUtils.isEmpty(notLinkFms)) {
                for(FileMap fm : notLinkFms) {
                    File tf = new File(fm.getPath());
                    if(!tf.exists()) {
                        needRemoveFmRecordMd5.add(fm.getMd5());
                        invalidCount++;
                        log.info("检测 {} 在数据库中存在，物理上不存在, 删除...", fm.getPath());
                    }
                }
            }

            if(!needRemoveFmRecordMd5.isEmpty()) {
                fileMapService.deleteByMd5s(needRemoveFmRecordMd5.toArray(new String[needRemoveFmRecordMd5.size()]));
            }
        }

        // 去除硬盘上存在，但是在数据库中不存在的文件.
        File fileSaveDir = new File(appConfig.getFileSaveDir());
        File[] files = fileSaveDir.listFiles();

        for (File file : files) {
            List<File> allSubFile = FileUtil.getAllSubFile(file);
            // 一次 100 个文件，计算摘要，并在数据库中进行查询。
            int count = allSubFile.size() % 100 == 0 ? allSubFile.size() / 100: allSubFile.size() / 100 + 1;

            for(int i = 0; i < count; i ++) {
                int start = i * 100;
                int end = (i + 1) * 100;
                end = end > allSubFile.size() ? allSubFile.size() : end;
                List<File> tList = allSubFile.subList(start, end);
                Map<String, File> tMap = new HashMap<>();

                for(File tf : tList) {
                    tMap.put(FileUtil.getMd5(tf.toPath()), tf);
                }

                List<String> existFmMd5s = factory.selectDistinct(qfm.md5)
                        .from(qfm)
                        .where(qfm.md5.in(tMap.keySet()))
                        .fetch();

                List<String> needRemoveFileMd5s = tMap.keySet()
                        .stream()
                        .filter(key -> !existFmMd5s.contains(key))
                        .collect(Collectors.toList());

                for(Map.Entry<String, File> tEn : tMap.entrySet()) {
                    if(needRemoveFileMd5s.contains(tEn.getKey())) {
                        String absPath = tEn.getValue().getAbsolutePath();
                        FileUtil.removeDirOrFile(tEn.getValue().toPath());
                        invalidCount++;
                        log.info("检测 {} 在数据库中不存在，物理上存在, 删除...", absPath);
                    }
                }
            }
        }

        // 清除空文件夹。
        FileUtil.clearEmptyDir(new File(appConfig.getFileSaveDir()));
        return invalidCount;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Date startDate = new Date(), endDate;
        log.info("持久化文件清理任务执行开始执行...");
        long invalidCount = clear();
        endDate = new Date();
        log.info("持久化清理任务执行开始完成...");

        WorkLog workLog = new WorkLog();
        workLog.setStartDate(startDate);
        workLog.setEndDate(endDate);
        workLog.setDescription(String.format("清理目录[%s]完成, 共清理[%s]个文件", appConfig.getFileSaveDir(), invalidCount));

        JobDetail jobDetail = context.getJobDetail();
        String jobName = jobDetail.getKey().getName();
        String jobGroupName = jobDetail.getKey().getGroup();
        workLog.setGroupName(jobGroupName);
        workLog.setName(jobName);
        workLog.setJobClass(jobDetail.getJobClass().getName());
        workLog.setStatus(1);
        workLogService.save(workLog);
    }
}
