package com.qiwen.other;

import com.sargeraswang.util.ExcelUtil.ExcelCell;
import com.sargeraswang.util.ExcelUtil.ExcelLogs;
import com.sargeraswang.util.ExcelUtil.ExcelSheet;
import com.sargeraswang.util.ExcelUtil.ExcelUtil;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class ExcelTests {

    @Data
    @AllArgsConstructor
    private class Model {
        @ExcelCell(index = 0)
        private String a;

        @ExcelCell(index = 1)
        private String b;

        @ExcelCell(index = 2)
        private String c;

        @ExcelCell(index = 3)
        private Date d;
    }

    @Test
    @SneakyThrows
    public void exportWithBeanTest() {
        Map<String,String> map = new LinkedHashMap<>();
        map.put("a","姓名");
        map.put("b","年龄");
        map.put("c","性别");
        map.put("d","出生日期");
        Collection<Object> dataset=new ArrayList<Object>();
        dataset.add(this.new Model("", "", "",null));
        dataset.add(new Model(null, null, null,null));
        dataset.add(new Model("王五", "34", "男",new Date()));
        File f=new File("D:\\tmp\\test.xls");
        @Cleanup OutputStream out = new FileOutputStream(f);
        ExcelUtil.exportExcel(map, dataset, out);
    }

    @Test
    @SneakyThrows
    public void testExportWithMapTest() {
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> map =new LinkedHashMap<>();
        map.put("name", "");
        map.put("age", "");
        map.put("birthday","");
        map.put("sex","");

        Map<String,Object> map2 =new LinkedHashMap<String, Object>();
        map2.put("name", "测试是否是中文长度不能自动宽度.测试是否是中文长度不能自动宽度.");
        map2.put("age", null);
        map2.put("sex", null);
        map.put("birthday",null);

        Map<String,Object> map3 =new LinkedHashMap<String, Object>();
        map3.put("name", "张三");
        map3.put("age", 12);
        map3.put("sex", "男");
        map3.put("birthday",new Date());
        list.add(map);
        list.add(map2);
        list.add(map3);

        Map<String,String> map1 = new LinkedHashMap<>();
        map1.put("name","姓名");
        map1.put("age","年龄");
        map1.put("birthday","出生日期");
        map1.put("sex","性别");

        File f= new File("D:\\tmp\\test2.xls");
        @Cleanup OutputStream out = new FileOutputStream(f);
        ExcelUtil.exportExcel(map1,list, out);
    }

    @Test
    @SneakyThrows
    public void testExportWithSheetTest() {
        // 1 班
        ExcelSheet<Model> sheet1 = new ExcelSheet<>();
        Map<String, String> map1 = new LinkedHashMap<>();
        map1.put("a","姓名");
        map1.put("b","年龄");
        map1.put("c","性别");
        map1.put("d","出生日期");
        sheet1.setHeaders(map1);
        sheet1.setSheetName("1班");
        Collection<Model> coll1 = new ArrayList<>();
        for(int i = 0; i < 10000; i++) {
            String name = "1班-" + (i + 1) + "号同学";
            String age = RandomUtils.nextLong(18, 20) + "";
            String gender = RandomUtils.nextLong(0, 100) >= 50 ? "男" : "女";
            coll1.add(this.new Model(name, age, gender, new Date()));
        }
        sheet1.setDataset(coll1);

        // 2 班
        ExcelSheet<Model> sheet2 = new ExcelSheet<>();
        Map<String, String> map2 = new LinkedHashMap<>();
        map2.put("a","姓名");
        map2.put("b","年龄");
        map2.put("c","性别");
        map2.put("d","出生日期");
        sheet2.setHeaders(map1);
        sheet2.setSheetName("2班");
        Collection<Model> coll2 = new ArrayList<>();
        for(int i = 0; i < 10000; i++) {
            String name = "2班-" + (i + 1) + "号同学";
            String age = RandomUtils.nextLong(18, 20) + "";
            String gender = RandomUtils.nextLong(0, 100) >= 50 ? "男" : "女";
            coll2.add(this.new Model(name, age, gender, new Date()));
        }
        sheet2.setDataset(coll2);

        File f= new File("D:\\tmp\\test3.xls");
        @Cleanup OutputStream out = new FileOutputStream(f);
        ExcelUtil.exportExcel(Arrays.asList(sheet1, sheet2), out);
    }

    @Test
    @SneakyThrows
    public void importExcelTest() {
        File f=new File("D:\\tmp\\test2.xls");
        @Cleanup InputStream inputStream= new FileInputStream(f);

        ExcelLogs logs =new ExcelLogs();
        Collection<Map> importExcel = ExcelUtil.importExcel(Map.class, inputStream, "yyyy/MM/dd HH:mm:ss", logs , 0);

        for(Map m : importExcel){
            System.out.println(m);
        }
    }

}
