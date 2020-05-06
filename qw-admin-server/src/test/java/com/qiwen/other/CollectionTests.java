package com.qiwen.other;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

public class CollectionTests {

    private List<Date> list = new ArrayList<>();

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    @Before
    public void before() {
        long base = System.currentTimeMillis();
        long size = 86400 * 1000;

        for(int i = 0; i < 100; i++) {
            list.add(new Date(RandomUtils.nextLong(base, base + size)));
        }
    }

    @Test
    public void sortTest() {
        // 升序
        Collections.sort(list, (v1, v2) -> v1.getTime() < v2.getTime() ? -1 : 1);
        list.forEach(date -> System.out.println(sdf.format(date)));
    }

    @Test
    public void minAndMaxTest() {
        Optional<Date> max = list.stream().max((v1, v2) -> v1.getTime() < v2.getTime() ? -1 : 1);
        Optional<Date> min = list.stream().min((v1, v2) -> v1.getTime() < v2.getTime() ? -1 : 1);
        max.ifPresent(date -> System.out.println(sdf.format(date)));
        min.ifPresent(date -> System.out.println(sdf.format(date)));
    }

    @Test
    public void reduceTest() {
        List<Integer> list = new ArrayList<>(10);
        for(int i = 0; i < 10; i++) {
            list.add(RandomUtils.nextInt(10, 100));
        }
        System.out.println(Arrays.toString(list.toArray()));

        Optional<Integer> reduce1 = list.stream().reduce((prev, curr) -> {
            System.out.println(String.format("%d, %d", prev, curr));
            return curr;
        });
    }
}
