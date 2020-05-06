package com.qiwen.other;

import com.qiwen.base.util.ZipUtil;
import net.coobird.thumbnailator.Thumbnails;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class ZipTests {

    @Test
    public void zipTest() {
        String srcFilePath = "D:\\tmp";
        String zipFileName = "D:\\tmp\\test3.zip";
        ZipUtil.zip(srcFilePath, zipFileName);
    }

    @Test
    public void unzipTest() {
        String zipFileName = "D:\\tmp\\test3.zip";
        String distPath = "D:\\tmp\\unzip";
        ZipUtil.unzip(zipFileName, distPath);
    }

    @Test
    public void imageCompressTest() throws IOException {
        for (int i = 1; i <= 9; i++) {
            Thumbnails.of("D:\\tmp\\t.jpg")
                    .scale(0.1 * i)
                    .toFile(new File(String.format("D:\\tmp\\t_%d.jpg", i)));
        }
    }
}
