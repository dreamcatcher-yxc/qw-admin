package com.qiwen.base.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlUtil {

    /**
     * 丑化 html 脚本.
     * @param html
     * @return
     */
    public static final String uglyHTML(String html) {
        StringBuffer sb = new StringBuffer();
        Pattern compile = Pattern.compile("</?[^>]+>");
        Matcher matcher = compile.matcher(html);
        int preEnd = 0, nowStart = 0, nowEnd = 0;

        while (matcher.find()) {
            String match = matcher.group();
            System.out.println(match);
            nowStart = matcher.start();
            nowEnd = matcher.end();

            if(nowStart - preEnd > 0) {
                sb.append(StringUtil.removeEmptyStr(html.substring(preEnd, nowStart)));
            }
            sb.append(match);
            preEnd = nowEnd;
        }
        return sb.toString();
    }

}
