package com.qiwen.base.util;

public class BusinessUtil {

    public static boolean validBoxNum(String boxNum) {
        if(boxNum.length() != 11)  {
            return false;
        }
        return Character.getNumericValue(boxNum.charAt(10)) == getBoxNumCheckCode(boxNum);
    }

    public static Integer getBoxNumCheckCode(String boxNum) {
        if(boxNum.length() < 10) {
            return null;
        }
        String charCode = "0123456789A?BCDEFGHIJK?LMNOPQRSTU?VWXYZ";
        int num = 0;
        for(int i = 0; i < boxNum.length(); i++) {
            int idx = charCode.indexOf(boxNum.charAt(i));
            if(idx == -1 || charCode.charAt(i) == '?') {
                return num;
            }
            idx = idx * (int)Math.pow(2, i);
            num += idx;
        }
        num = (num % 11) % 11;
        return num;
    }
}
