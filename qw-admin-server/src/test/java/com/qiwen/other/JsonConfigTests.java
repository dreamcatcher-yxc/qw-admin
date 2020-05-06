package com.qiwen.other;

import com.qiwen.base.util.FileUtil;
import com.qiwen.base.util.GsonUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonConfigTests {

    private Object object;

    @Before
    public void before() {
        try {
            byte[] bytes = FileUtil.fileToBytes("D:\\tmp\\config.json");
            object = GsonUtil.fromJsonToClass(new String(bytes), Object.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test01() {
        System.out.println(object);
    }

    @Test
    public void test02() {
        Pattern compile = Pattern.compile("(\\w*)(\\[([^\\]]+)\\])+?");
        Matcher matcher = compile.matcher("aaa");

        while (matcher.find()) {
            System.out.println(matcher.start());
            System.out.println(matcher.end());
            int len = matcher.groupCount();

            for(int i = 0; i < len; i++) {
                System.out.println(matcher.group(i + 1));
            }
        }
    }

    @Test
    public void test03() {
        String str = "${age}aaa${name}ccc${gender }";
        Pattern compile = Pattern.compile("(\\$\\{([\\w]+)\\})+?");
        Matcher matcher = compile.matcher(str);

        Map<String, Object> params = new HashMap<>();
        params.put("age", 10);
        params.put("name", "小明");
        params.put("gender", "男");
        for (String key : params.keySet()) {
            str = str.replaceAll("\\$\\{\\s*" + key + "\\s*\\}", params.get(key).toString());
        }
        System.out.println(str);
    }
}
