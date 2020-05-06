package com.qiwen.other;

import com.qiwen.base.util.NameImgUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

import static com.qiwen.base.util.PasswordUtil.encryptPassword;

@Slf4j
public class DescTests {

    @Test
    public void test01() {
//        Constant.PASSWORD_SALT = "qw-gateway";
        String p = encryptPassword("123456");
        log.info(p);
    }

    @Test
    public void test02() throws IOException {
        NameImgUtil.generateImg("杨秀初", "D:/", "name");
    }

}
