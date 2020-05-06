package com.qiwen.base.util;

import java.math.BigDecimal;

/**
 * 浮点数精准算法
 */
public class BigDecimalArithUtil {

    private static final int DIV_SCALE = 4;//除法精度（除不尽时保留10为小数)

    public static String getFormat() {
        StringBuffer sb = new StringBuffer("%." + DIV_SCALE + "f");
        return sb.toString();
    }

    /**
     * 小数精确加法
     */
    public static float add(float d1, float d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        return bd1.add(bd2).floatValue();
    }

    /**
     * 小数精确加法
     */
    public static double add(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        return bd1.add(bd2).doubleValue();
    }

    /**
     * 小数精确减法
     */
    public static float sub(float d1, float d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        return bd1.subtract(bd2).floatValue();
    }

    /**
     * 小数精确减法
     */
    public static double sub(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        return bd1.subtract(bd2).doubleValue();
    }

    /**
     * 小数精确乘法
     */
    public static float mul(float d1, float d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        return bd1.multiply(bd2).floatValue();
    }

    /**
     * 小数精确乘法
     */
    public static double mul(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        return bd1.multiply(bd2).doubleValue();
    }

    /**
     * 小数精确除法
     */
    public static float div(float d1, float d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        /*
         * 当除不尽时，以四舍五入的方式（关于除不尽后的值的处理方式有很多种）保留小数点后10位小数
         */
        return bd1.divide(bd2, DIV_SCALE, BigDecimal.ROUND_HALF_UP).floatValue();
    }

    /**
     * 小数精确除法
     */
    public static double div(double d1, double d2) {
        BigDecimal bd1 = new BigDecimal(d1);
        BigDecimal bd2 = new BigDecimal(d2);
        /*
         * 当除不尽时，以四舍五入的方式（关于除不尽后的值的处理方式有很多种）保留小数点后10位小数
         */
        return bd1.divide(bd2, DIV_SCALE, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 小数精确乘法
     */
    public static String exchange(float d) {
        BigDecimal bd1 = new BigDecimal(d);
        return bd1.setScale(1, BigDecimal.ROUND_HALF_DOWN).toPlainString();
    }

    /**
     * 小数精确乘法
     */
    public static String exchange(double d) {
        BigDecimal bd1 = new BigDecimal(d);
        return bd1.setScale(1, BigDecimal.ROUND_HALF_DOWN).toPlainString();
    }
}
