//package com.qiwen.base.common;
//
//import com.qiwen.base.config.web.WebConfig;
//import com.qiwen.base.entity.User;
//import com.qiwen.base.service.IUserService;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import static com.qiwen.base.util.PasswordUtil.encryptPassword;
//import static org.junit.Assert.assertNotNull;
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
//public class IUserServiceTests {
//
//    @Autowired
//    private IUserService userService;
//
//    @Test
//    public void saveUserTest() {
//        User user = new User();
//        user.setUsername("yangxiuchu");
//        user.setPassword("123456");
//        user.setEmail("yangxiuchu@sina.com");
//        user.setGender("M");
//        userService.save(user);
//    }
//
//    @Test
//    public void findByUsernameAndPasswordTest() {
//        assertNotNull(userService.findByUsernameAndPassword("yangxiuchu", encryptPassword("123456")));
//    }
//
//    @Test
//    public void findAllTest() {
//        Page<User> page = userService.findAll(PageRequest.of(0, 5));
//        assertNotNull(page);
//    }
//
//}
