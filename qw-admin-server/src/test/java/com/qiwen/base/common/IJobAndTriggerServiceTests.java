//package com.qiwen.base.common;
//
//import com.qiwen.base.config.web.WebConfig;
//import com.qiwen.base.vo.JobAndTriggerVO;
//import com.qiwen.base.service.IJobAndTriggerService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@DataJpaTest
//// 使用真实数据库测试
//@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
//@ComponentScan(
//        basePackages = {"com.qiwen.base.config.db", "com.alibaba.druid.spring.boot.autoconfigure"},
//        excludeFilters = {
//                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebConfig.class})})
//public class IJobAndTriggerServiceTests {
//
//    @Autowired
//    private IJobAndTriggerService jobAndTriggerService;
//
//    @Test
//    public void testFindAll() throws Exception {
//        Page<JobAndTriggerVO> page = jobAndTriggerService.findAll(PageRequest.of(0, 10));
//        Assert.assertEquals(page.getTotalElements(), 0);
//    }
//
//}
