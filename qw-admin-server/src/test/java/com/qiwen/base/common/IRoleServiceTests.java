//package com.qiwen.base.common;
//
//import com.qiwen.base.config.web.WebConfig;
//import com.qiwen.base.entity.Role;
//import com.qiwen.base.service.IRoleService;
//import com.qiwen.base.util.LayUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.data.domain.Page;
//import org.springframework.test.context.junit4.SpringRunner;
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@DataJpaTest
//// 使用真实数据库测试
//@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
//@ComponentScan(
//        basePackages = {"com.qiwen.base.common", "com.alibaba.druid.spring.boot.autoconfigure"},
//        excludeFilters = {
//                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebConfig.class})})
//public class IRoleServiceTests {
//
//    @Autowired
//    private IRoleService roleService;
//
//    @Test
//    public void saveTest() {
//        Role role = new Role();
//        role.setName("admin");
//        role.setDescription("系统基本事务管理");
//        role.setStatus(1);
//        roleService.save(role);
//    }
//
//    @Test
//    public void deleteByIdsTest() {
//        roleService.deleteByIds(1L);
//    }
//
//    @Test
//    public void findAllTest() {
//        Page<Role> page = roleService.findAll(LayUtil.toPageable(1, 5));
//        Assert.assertNull(page.getContent());
//    }
//}
