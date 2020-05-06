package com.qiwen.querydsl;

import com.qiwen.base.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RestController;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(
        basePackages = "com.qiwen.base.common",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = {Controller.class, RestController.class})})
public class Test01 {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JPAQueryFactory factory;

    /**
     * 查询所有的用户信息
     */
    @Test
    public void testFindAll() {

    }

}
