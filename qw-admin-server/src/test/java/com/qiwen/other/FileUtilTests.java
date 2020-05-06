package com.qiwen.other;

import com.qiwen.base.util.FileUtil;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class FileUtilTests {

    @Test
    public void test01() {
        List<File> allSubFile = FileUtil.getAllSubFile(new File("D:\\tmp\\qw2"));
        allSubFile.forEach(f -> {
            System.out.println(f.getAbsoluteFile());
        });
    }

    @Test
    public void test02() {
        FileUtil.clearEmptyDir(new File("D:\\tmp\\qw2"));
    }
}
