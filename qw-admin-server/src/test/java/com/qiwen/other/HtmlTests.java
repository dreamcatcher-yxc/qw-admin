package com.qiwen.other;

import com.qiwen.base.util.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlTests {

    @Test
    public void test01() throws Exception {
        // 直接从字符串中输入 HTML 文档
        String html = "<html><head>aaa<title> 开源中国社区 </title> bbb </head>"
                + "<body><p> 这里是 jsoup 项目的相关文章 </p> www </body></html>"
                + "<img src='/foo.png'>";
        Document doc = Jsoup.parse(html);
        html = doc.html();

        System.out.println(html);
        System.out.println("**************************************************************");

        StringBuffer sb = new StringBuffer();
        Pattern compile = Pattern.compile("</?[^>]+>");
        Matcher matcher = compile.matcher(html);
        String preMatch = "";
        int preStart = 0, preEnd = 0;
        int nowStart = 0, nowEnd = 0;

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
        System.out.println(sb.toString());
    }

    @Test
    public void test02() throws Exception {
        String html = "<html><head>aaa<title> 开源中国社区 </title> bbb </head>"
                + "<body><p> 这里是 jsoup 项目的相关文章 </p> www </body></html>"
                + "<img src='/foo.png'>";
        Document doc = Jsoup.parse(html);
        String text = doc.body().text();
        System.out.println(text);
    }

}
