package com.qiwen.base.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.patchca.background.BackgroundFactory;
import org.patchca.filter.predefined.CurvesRippleFilterFactory;
import org.patchca.service.ConfigurableCaptchaService;
import org.patchca.utils.encoder.EncoderHelper;
import org.patchca.word.RandomWordFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

/**
 * Created by yangxiuchu on 2017/8/17.
 */
@Slf4j
public class ValidateImageUtil {

    public static final String CHECK_CODE_KEY = "CHECK_NUMBER_KEY";

    public static boolean isWindow() {
        return System.getProperty("os.name").toLowerCase().startsWith("win");
    }

    private static class QWCustomBackgroundFactory implements BackgroundFactory {
        private Random random = new Random();

        @Override
        public void fillBackground(BufferedImage bufferedImage) {
            Graphics graphics = bufferedImage.getGraphics();
            // 验证码图片的宽高
            int imgWidth = bufferedImage.getWidth();
            int imgHeight = bufferedImage.getHeight();
            // 填充为白色背景
            graphics.setColor(Color.lightGray);
            graphics.fillRect(0, 0, imgWidth, imgHeight);
            // 画100个噪点(颜色及位置随机)
            for (int i = 0; i < 100; i++) {
                // 随机颜色
                int rInt = random.nextInt(255);
                int gInt = random.nextInt(255);
                int bInt = random.nextInt(255);
                graphics.setColor(new Color(rInt, gInt, bInt));
                // 随机位置
                int xInt = random.nextInt(imgWidth - 3);

                int yInt = random.nextInt(imgHeight - 2);
                // 随机旋转角度
                int sAngleInt = random.nextInt(360);
                int eAngleInt = random.nextInt(360);
                // 随机大小
                int wInt = random.nextInt(6);
                int hInt = random.nextInt(6);
                graphics.fillArc(xInt, yInt, wInt, hInt, sAngleInt, eAngleInt);
                // 画5条干扰线
                if (i % 20 == 0) {
                    int xInt2 = random.nextInt(imgWidth);
                    int yInt2 = random.nextInt(imgHeight);
                    graphics.drawLine(xInt, yInt, xInt2, yInt2);
                }
            }
        }
    }

    public static boolean validate(HttpServletRequest request, String checkCodeKeyParameter) {
        String checkCode = request.getParameter(checkCodeKeyParameter);
        return !StringUtils.isEmpty(checkCode) && checkCode.equalsIgnoreCase((String) request.getSession().getAttribute(CHECK_CODE_KEY));
    }

    /**
     * 依赖 patchca.jar
     */
    private static ConfigurableCaptchaService CS1 = new ConfigurableCaptchaService();
    private static ConfigurableCaptchaService CS2 = new ConfigurableCaptchaService();
    private static Random RANDOM = new Random();
    private static RandomWordFactory NUMBER_WORD_FACTORY;
    private static RandomWordFactory CASE_AND_NUMBER_FACTORY;

    static {
        CS1.setColorFactory((x) -> {
            int[] c = new int[3];
            int i = RANDOM.nextInt(c.length);
            for (int fi = 0; fi < c.length; fi++) {
                if (fi == i) {
                    c[fi] = RANDOM.nextInt(71);
                } else {
                    c[fi] = RANDOM.nextInt(256);
                }
            }
            return new Color(c[0], c[1], c[2]);
        });
        CS2.setColorFactory(CS1.getColorFactory());

        NUMBER_WORD_FACTORY = new RandomWordFactory();
        NUMBER_WORD_FACTORY.setCharacters("0123456789");
        NUMBER_WORD_FACTORY.setMaxLength(4);
        NUMBER_WORD_FACTORY.setMinLength(4);
        CS1.setWordFactory(NUMBER_WORD_FACTORY);

        CASE_AND_NUMBER_FACTORY = new RandomWordFactory();
        CASE_AND_NUMBER_FACTORY.setCharacters("23456789abcdefghigkmnpqrstuvwxyzABCDEFGHIGKLMNPQRSTUVWXYZ");
        CASE_AND_NUMBER_FACTORY.setMaxLength(4);
        CASE_AND_NUMBER_FACTORY.setMinLength(4);
        CS2.setWordFactory(CASE_AND_NUMBER_FACTORY);

        CS1.setFilterFactory(new CurvesRippleFilterFactory(CS1.getColorFactory()));
        CS1.setBackgroundFactory(new QWCustomBackgroundFactory());
        CS1.setFilterFactory(new CurvesRippleFilterFactory(CS2.getColorFactory()));
        CS1.setBackgroundFactory(new QWCustomBackgroundFactory());
    }

    public static void generateImage(HttpServletRequest request, HttpServletResponse response, boolean onlyNumbers) throws IOException {
//        switch (RANDOM.nextInt(5)) {
//            case 0:
//                CS.setFilterFactory(new CurvesRippleFilterFactory(CS.getColorFactory()));
//                break;
//            case 1:
//                CS.setFilterFactory(new MarbleRippleFilterFactory());
//                break;
//            case 2:
//                CS.setFilterFactory(new DoubleRippleFilterFactory());
//                break;
//            case 3:
//                CS.setFilterFactory(new WobbleRippleFilterFactory());
//                break;
//            case 4:
//                CS.setFilterFactory(new DiffuseRippleFilterFactory());
//                break;
//        }
        HttpSession session = request.getSession();
        setResponseHeaders(response);
        String token = EncoderHelper.getChallangeAndWriteImage(onlyNumbers ? CS1 : CS2, "png", response.getOutputStream());
        session.setAttribute(CHECK_CODE_KEY, token);
    }

    private static void setResponseHeaders(HttpServletResponse response) {
        response.setContentType("image/png");
        response.setHeader("Cache-Control", "no-cache, no-store");
        response.setHeader("Pragma", "no-cache");
        long time = System.currentTimeMillis();
        response.setDateHeader("Last-Modified", time);
        response.setDateHeader("Date", time);
        response.setDateHeader("Expires", time);
    }
}
