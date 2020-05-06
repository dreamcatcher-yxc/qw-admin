/**
 * 任务管理相关 API
 */
define([
    'request',
], function(Request) {
    var $http = Request.$http;
    return {
        /**
         * 查找所有任务
         * @param page
         * @param limit
         */
        queryTasks: function(page, limit) {
            page = page||1;
            limit = limit||10;
            return $http.get('/base/job/rest/list', { params: { page: page, limit: limit } });
        },
        /**
         * 根据 taskId 查询任务信息
         * @param {*} taskId 
         */
        queryTask: function(taskId) {
        },
        /**
         * 新增任务
         * @param {*} form.jobClassName 名称
         * @param {*} form.jobGroupName 分组
         * @param {*} form.cronExpression 表达式 
         */
        addTask: function(form) {
            return $http.post('/base/job/rest/add', form);
        },
        /**
         * 判断任务名是否已经存在
         * @param {*} taskName 任务名
         */
        isExisted: function(username) {
            return $http.post('/base/job/rest/exist/name', { username: username })
                        .then(function(data) {
                            return !!data.sdata.exist;
                        });
        },
        /**
         * 删除指定任务信息
         * @param {*} form.jobClassName 名称
         * @param {*} form.jobGroupName 分组
         */
        deleteTask: function(form) {
            return $http.post('/base/job/rest/delete', form);
        },
        /**
         * 修改任务信息
         * @param {*} form.jobClassName 名称
         * @param {*} form.jobGroupName 分组
         * @param {*} form.cronExpression 表达式
         */
        editTask: function(form) {
            return $http.post('/base/job/rest/reschedule', form);
        },
        /**
         * 停止当前正在执行的任务
         * @param {*} form.jobClassName 名称
         * @param {*} form.jobGroupName 分组
         */
        pauseTask: function(form) {
            return $http.post('/base/job/rest/pause', form);
        },
        /**
         * 恢复当前已经暂停的任务
         * @param {*} form.jobClassName 名称
         * @param {*} form.jobGroupName 分组
         */
        resumeTask: function(form) {
            return $http.post('/base/job/rest/resume', form);
        },
        /**
         * 触发当前任务执行
         * @param {*} form.jobClassName 名称
         * @param {*} form.jobGroupName 分组
         */
        triggerTask: function(form) {
            return $http.post('/base/job/rest/trigger', form);
        },
        /**
         * 查询当前可新建的任务名称
         * @param { Boolean } available 是否需要过滤当前已经创建的任务列表
         */
        queryJobs: function(available) {
            available = !!available;
            return $http.get('/base/job/rest/jobs/available', { params: { available: available } }).then(data => {
                var jobs = data.sdata.jobs;
                if(Array.isArray(jobs)) {
                    data.sdata.jobs = jobs.map(function(job) {
                        return { 
                            key: job.className,
                            value: job.name
                        };
                    });
                }
                return data;
            });
        },
        /**
         * 查询当前可选择的任务分组
         */
        queryTaskGroups: function() {
            var taskGroups = [
                { key: 'system', value: '系统任务组' }
            ];
            return Promise.resolve({
                sdata: {
                    taskGroups: taskGroups
                }
            });
        },
        /**
         * 查询执行日志列表
         * @param {*} searchForm.startDate 开始日期（创建） 
         * @param {*} searchForm.endDate 结束日志（创建） 
         * @param {*} searchForm.jobClass 执行类 
         */
        queryTaskLogs: function(searchForm, page, limit) {
            var params = {
                startDate: searchForm.startDate,
                endDate: searchForm.endDate,
                jobClass: searchForm.jobClass,
                page: page||1,
                limit: limit||10
            };
            return $http.get('/base/worklog/rest/list', { params: params });
        },
        /**
         * 删除1 ~ n条任务执行日志信息
         * @param {*} workLogIds 任务执行日志 ID 集合。
         */
        deleteTaskLogs: function(workLogIds) {
            return $http.get('/base/worklog/rest/delete/' + workLogIds.join(','));
        },
    }
});