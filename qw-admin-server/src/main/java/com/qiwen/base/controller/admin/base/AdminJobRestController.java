package com.qiwen.base.controller.admin.base;

import com.qiwen.base.config.QWConstant;
import com.qiwen.base.config.annotaion.Desc;
import com.qiwen.base.service.IJobAndTriggerService;
import com.qiwen.base.service.IQuartzService;
import com.qiwen.base.util.LayUtil;
import com.qiwen.base.util.Result;
import com.qiwen.base.vo.JobAndTriggerVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Desc(value = QWConstant.MODULE_GROUP_NAME_DESC + "作业管理", groupName = QWConstant.MODULE_GROUP_NAME + "job-admin")
@RestController
@RequestMapping(value="/admin/base/job/rest")
public class AdminJobRestController
{
	private final IJobAndTriggerService jtService;
	
	private final IQuartzService quartzService;

    public AdminJobRestController(IJobAndTriggerService jtService, IQuartzService quartzService) {
        this.jtService = jtService;
        this.quartzService = quartzService;
    }

    @Desc(value = "r-新建工作任务", name = "job-add")
    @PostMapping(value="/add")
	public String add(@RequestParam(value="jobClassName")String jobClassName,
			@RequestParam(value="jobGroupName")String jobGroupName, 
			@RequestParam(value="cronExpression")String cronExpression) {
        quartzService.addJob(jobClassName, jobGroupName, cronExpression);
        return Result.ok().json();
	}

    @Desc(value = "r-暂停指定任务", name = "job-pause")
	@PostMapping(value="/pause")
	public String pause(@RequestParam(value="jobClassName")String jobClassName, @RequestParam(value="jobGroupName")String jobGroupName) {
        quartzService.jobPause(jobClassName, jobGroupName);
        return Result.ok().json();
	}

    @Desc(value = "r-恢复被暂停的任务", name = "job-resume")
	@PostMapping(value="/resume")
	public String resume(@RequestParam(value="jobClassName")String jobClassName, @RequestParam(value="jobGroupName")String jobGroupName) {
		quartzService.jobResume(jobClassName, jobGroupName);
        return Result.ok().json();
	}

    /**
     * 重新设置任务的执行规则
     * @param jobClassName
     * @param jobGroupName
     * @param cronExpression
     * @return
     */
    @Desc(value = "r-重新设置任务的执行规则", name = "job-reschedule")
	@PostMapping(value="/reschedule")
	public String reschedule(@RequestParam(value="jobClassName")String jobClassName,
			@RequestParam(value="jobGroupName")String jobGroupName,
			@RequestParam(value="cronExpression")String cronExpression) {
		quartzService.jobReschedule(jobClassName, jobGroupName, cronExpression);
        return Result.ok().json();
	}

	@Desc(value = "r-删除指定的任务", name = "job-delete")
	@PostMapping(value="/delete")
	public String delete(@RequestParam(value="jobClassName")String jobClassName, @RequestParam(value="jobGroupName")String jobGroupName) {
		quartzService.jobDelete(jobClassName, jobGroupName);
        return Result.ok().json();
	}

	@Desc(value = "r-分页查询所有的任务", name = "job-list")
	@GetMapping(value="/list")
	public String query(@RequestParam(value="page", defaultValue = "1")Integer page, @RequestParam(value="limit", defaultValue = "10")Integer limit) {
		Page<JobAndTriggerVO> result = jtService.findAll(LayUtil.toPageable(page, limit));
		return LayUtil.toLayPageJson(result);
	}

	@Desc(value = "r-直接触发有效的工作任务", name = "job-trigger")
	@PostMapping(value = "/trigger")
	public String trigger(@RequestParam(value="jobClassName")String jobClassName, @RequestParam(value="jobGroupName")String jobGroupName) {
		quartzService.jobTrigger(jobClassName, jobGroupName);
		return Result.ok().json();
	}

	@Desc(value = "r-查询当前可选任务列表", name = "job-available")
	@GetMapping(value = "/jobs/available")
	public String queryAvailableJobs(@RequestParam(defaultValue = "true") Boolean available) {
		Page<JobAndTriggerVO> all = jtService.findAll(PageRequest.of(0, 100000));
		List<Map<String, Object>> jobs = null;
		List<JobAndTriggerVO> content = all.getContent();

		if (content == null) {
			jobs = QWConstant.JOB_CLASS_NAMES;
		} else if(available) {
			jobs = QWConstant.JOB_CLASS_NAMES
					.stream()
					.filter(meta -> !content.stream().anyMatch(vo -> meta.get("className").equals(vo.getJOB_CLASS_NAME())))
					.collect(Collectors.toList());
		} else {
			jobs = QWConstant.JOB_CLASS_NAMES;
		}

		return Result.ok().put("jobs", jobs).toJsonString();
	}
}
