package com.qiwen.base.config.job;

import com.qiwen.base.config.QWAppConfig;
import com.qiwen.base.entity.WorkLog;
import com.qiwen.base.service.IWorkLogService;
import com.qiwen.base.util.FileUtil;
import com.qiwen.base.util.SpringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TempFileClearJob implements Job {

    public static final String NAME = "缓存文件清理任务";

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private final IWorkLogService workLogService;

    private final QWAppConfig appConfig;

    public TempFileClearJob() {
        this.workLogService = SpringHelper.getRealBean(IWorkLogService.class);
        this.appConfig = SpringHelper.getRealBean(QWAppConfig.class);
    }

    /**
     * 清理今天之前的所有缓存文件
     */
    private Map<String, Integer> clearTemp() {
        String activeProfile = SpringHelper.getProperties("spring.profiles.active", String.class, "dev");
        // 测试阶段, 防止误删除电脑上的重要文件.
        if("dev".equalsIgnoreCase(activeProfile) && !appConfig.getFileTempDir().equals("D:\\tmp\\yjyx\\temp")) {
            return null;
        }

        Path path = Paths.get(appConfig.getFileTempDir());
        File file = path.toFile();

        if(!file.exists() || file.isFile()) {
            return null;
        }

        File[] childrens = file.listFiles();
//        String lowerLimit = sdf.format(DateUtils.addDays(new Date(), -1));
        Long lowerLimit = DateUtils.addDays(new Date(), -1).getTime();

        List<File> needRemoveDirs = Arrays.stream(childrens)
                .filter(children -> {
                    try {
                        return children.exists()
                                && children.getName().matches("[\\d]{4}\\-[\\d]{2}\\-[\\d]{2}")
                                && sdf.parse(children.getName()).getTime() < lowerLimit;
                    } catch (ParseException e) {
                    }
                    return false;
                })
                .collect(Collectors.toList());

        Map<String, Integer> result = new HashMap<>();
        int fileCount = 0;
        int dirCount = 0;

        for (File removeDir : needRemoveDirs) {
            Map<String, Integer> res = FileUtil.removeDirOrFile(removeDir.toPath());
            if(res != null) {
                fileCount += res.get("file");
                dirCount += res.get("dir");
            }
        }

        result.put("file", fileCount);
        result.put("dir", dirCount);

        return result;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Date startDate = new Date(), endDate;
        Map<String, Integer> result = clearTemp();
        endDate = new Date();

        WorkLog workLog = new WorkLog();
        workLog.setStartDate(startDate);
        workLog.setEndDate(endDate);
        workLog.setDescription(
                result == null ?
                        String.format("清理缓存目录[%s]完成", appConfig.getFileTempDir()) :
                        String.format("清理缓存目录[%s]完成, 共清理 [%s] 个文件，[%s] 个文件夹", appConfig.getFileTempDir(), result.get("file"), result.get("dir"))
        );

        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        String jobName = jobDetail.getKey().getName();
        String jobGroupName = jobDetail.getKey().getGroup();
        workLog.setGroupName(jobGroupName);
        workLog.setName(jobName);
        workLog.setJobClass(jobDetail.getJobClass().getName());
        workLog.setStatus(1);
        workLogService.save(workLog);
    }
}
