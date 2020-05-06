package com.qiwen.base.util;

import org.apache.commons.lang.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

public class GatewayUtil {

    private GatewayUtil() {}

    /**
     * 将 page 中保存的分页信息存入 model 中, 具体映射关系如下:
     * <ol>
     *     <li>pages: 分页查询得到的数据信息</li>
     *     <li>prev: 上一页分页</li>
     *     <li>next: 下一页分页</li>
     *     <li>curr: 当前分页</li>
     *     <li>totalElements: 总共的记录数目</li>
     *     <li>totalPages: 总的分页数量</li>
     *     <li>size: 分页大小</li>
     *     <li>hasPrev: 是否有前一页</li>
     *     <li>hasNext: 是否有后一页</li>
     *     <li>bars: 分页条数组, 最大为 10</li>
     * </ol>
     * @param model: springMVC Model
     * @param page: Spring-data-JPA Page
     * @param wrap: 当 wrap 参数传递的时候, 则会将上面的属性添加到以为 map 中, 然后以 wrap[0] 的值为键存入 model 中.
     */
    public static void toPage(Model model, Page page, String... wrap) {
        int start =  page.getNumber() > 5 ? page.getNumber() - 5 : 0;
        int end = start + 9 > page.getTotalPages() ? page.getTotalPages() : start + 9;
        int span = end - start + (start != 0 ? 1 : 0);
        int[] bars = new int[span];
        int t = start;

        for(int i = 0; i < span; i++) {
            bars[i] = t++;
        }
        
        if(ArrayUtils.isEmpty(wrap)) {
            model.addAttribute("pages", page.getContent());
            model.addAttribute("prev", page.getNumber() > 0 ? page.getNumber() - 1 : page.getNumber());
            model.addAttribute("next", page.getNumber() >= page.getTotalPages() ? page.getNumber() : page.getNumber() + 1);
            model.addAttribute("curr", page.getNumber());
            model.addAttribute("totalElements", page.getTotalElements());
            model.addAttribute("totalPages", page.getTotalPages());
            model.addAttribute("size", page.getSize());
            model.addAttribute("hasPrev", page.hasPrevious());
            model.addAttribute("hasNext",  page.hasNext());
            model.addAttribute("bars", bars);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("pages", page.getContent());
            map.put("prev", page.getNumber() > 0 ? page.getNumber() - 1 : page.getNumber());
            map.put("next", page.getNumber() >= page.getTotalPages() ? page.getNumber() : page.getNumber() + 1);
            map.put("curr", page.getNumber());
            map.put("totalElements", page.getTotalElements());
            map.put("totalPages", page.getTotalPages());
            map.put("size", page.getSize());
            map.put("hasPrev", page.hasPrevious());
            map.put("hasNext",  page.hasNext());
            map.put("bars", bars);
            model.addAttribute(wrap[0], map);
        }
    }

}
