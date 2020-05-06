package com.qiwen.base.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class FileUtil {

    private FileUtil() {
    }

    /**
     * 组合多个路径。
     * @param paths 路径数组。
     * @return 路径组合结果。
     */
    public static String combination(String... paths) {
        StringBuffer sb = new StringBuffer();
        for (String path : paths) {
            if (path == null) {
                break;
            }
            if (path.indexOf(":") < 0) {
                path = path.startsWith(File.separator) ? path : (File.separator + path);
            }
            path = path.endsWith(File.separator) ? path.substring(0, path.length() - 1) : path;
            sb.append(path);
        }
        return sb.toString();
    }

    /**
     * 获取一个文件的摘要信息
     * @param path
     * @return
     */
    public static String getMd5(Path path) {
        BigInteger bi = null;
        try {
            byte[] buffer = new byte[8192];
            int len = 0;
            MessageDigest md = MessageDigest.getInstance("MD5");
            File f = path.toFile();
            FileInputStream fis = new FileInputStream(f);
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            fis.close();
            byte[] b = md.digest();
            bi = new BigInteger(1, b);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bi.toString(16);
    }

    /**
     * 删除文件或者文件夹。
     * @param path: 目标文件或者文件夹路径。
     * @return
     * {
     *      file : 10,
     *      dir : 20
     * }
     */
    public static Map<String, Integer> removeDirOrFile(Path path) {
        File file = path.toFile();
        Map<String, Integer> result = new HashMap<>();
        int fileCount = 0, dirCount = 0;

        if(file.isDirectory()) {
            File[] subs = file.listFiles();

            if(ArrayUtils.isEmpty(subs)) {
                result.put("file", 0);
                result.put("dir", 0);
                return result;
            }

            for(int i = 0; i < subs.length; i++) {
                File sub = subs[i];

                if(sub.isDirectory()) {
                    Map<String, Integer> resultMap = removeDirOrFile(sub.toPath());
                    fileCount += resultMap.get("file");
                    dirCount += resultMap.get("dir");
                } else {
                    removeDirOrFile(sub.toPath());
                }
            }

            deleteSingleFile(file.toPath());
            dirCount += 1;
        } else {
            deleteSingleFile(file.toPath());
            fileCount += 1;
        }

        result.put("file", fileCount);
        result.put("dir", dirCount);

        return result;
    }

    private static void deleteSingleFile(Path path) {
        File file = path.toFile();
        file.delete();
    }

    /**
     * 遍历一个文件夹下所有的文件，以集合的方式返回。
     * @param file: 遍历的文件夹，如果该文件夹不能存在或者部位文件夹类型，将会返回一个空集合。
     * @return 包含所有子文件夹的集合。
     */
    public static List<File> getAllSubFile(File file) {
        List<File> container = new ArrayList<>();
        if(file.exists() && file.isDirectory()) {
            ergodicDir(file, container);
        }
        return container;
    }

    private static void ergodicDir(File file, List<File> container) {
        if(file.isFile()) {
            container.add(file);
        } else if(file.isDirectory()) {
            for(File tf : file.listFiles()) {
                ergodicDir(tf, container);
            }
        }
    }

    /**
     * 删除某个文件夹下的所有空文件夹，本身为空文件夹也会被删除。
     * @param dir: 目标文件夹。
     * @return
     */
    public static void clearEmptyDir(File dir) {
        if(dir.isDirectory()) {
            File[] subs = dir.listFiles();
            if(!ArrayUtils.isEmpty(subs)) {
                for(File sub : subs) {
                    clearEmptyDir(sub);
                }
            }
            if(ArrayUtils.isEmpty(dir.listFiles())) {
                dir.delete();
            }
        }
    }

    public static byte[] fileToBytes(String filePath) throws IOException {
        byte[] buffer = null;
        File file = new File(filePath);

        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];

            int n;

            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }

            buffer = bos.toByteArray();
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
            } catch (IOException ex) {
                throw ex;
            } finally{
                try {
                    if(null!=fis){
                        fis.close();
                    }
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        return buffer;
    }

    public static void bytesToFile(byte[] buffer, final String filePath){
        File file = new File(filePath);

        OutputStream output = null;
        BufferedOutputStream bufferedOutput = null;

        try {
            output = new FileOutputStream(file);
            bufferedOutput = new BufferedOutputStream(output);
            bufferedOutput.write(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(null!=bufferedOutput){
                try {
                    bufferedOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(null != output){
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
