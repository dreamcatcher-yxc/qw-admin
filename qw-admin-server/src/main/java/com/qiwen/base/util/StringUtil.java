package com.qiwen.base.util;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

	public static enum DateFormatPattern {
		DATE {
			public String getPattern() {
				return "yyyy-MM-dd";
			}
		},

		TIME{
			public String getPattern() {
				return "HH:mm:ss";
			}
		},

		DATETIME {
			public String getPattern() {
				return "yyyy-MM-dd HH:mm:ss";
			}
		};

		public abstract String getPattern();
	}

	private static int[] secretKeyArr = { 
										  0, 64, 0, 32, 1, 35, 39, 0, 50, 0
										};
	
	/**
	 * @Name: stringConvertDate
	 * @Description: 将字符串形式的类型转换成日期类型
	 * @Author: 刘洋（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2011-12-22 （创建日期）
	 * @Parameters: String date 字符串类型的日期形式
	 * @Return: Date 日期类型
	 */
	public static Date stringConvertDate(String date, DateFormatPattern pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern.getPattern());
		Date d = null;
		try {
			d = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d;
	}

	/**
	 * @Name: dateConvertString
	 * @Description: 将字符串形式的类型转换成日期类型
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-9-14 （创建日期）
	 * @Parameters: Date date
	 * @Return: 转换之后的字符串。
	 */
	public synchronized static String dateConvertString(Date date, DateFormatPattern pattern) {
		SimpleDateFormat format = new SimpleDateFormat(pattern.getPattern());
		String dateStr = null;

		try {
			dateStr = format.format(date);
			return dateStr;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * @Name: join
	 * @Description: 将一个字符串数组装换为一个字符串，连接符为指定的separator.
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-1 （创建日期）
	 * @Parameters: String[] strArr, String separator
	 * @Return: 转换之后的字符串。
	 */
	public static String join(String[] strArr, String separator) {
		if (ArrayUtils.isEmpty(strArr)) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < strArr.length - 1; i++) {
			sb.append(strArr[i]).append(separator);
		}

		sb.append(strArr[strArr.length - 1]);

		return sb.toString();
	}

	/**
	 * @Name: convertToMD5Str
	 * @Description:将一个字符串进行MD5加密。
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-2 （创建日期）
	 * @Parameters: String originalStr(需要转换的字符串)
	 * @Return: MD5加密之后的字符串。
	 */
	public static String convertToMD5Str(String originalStr) {
		MD5key m = new MD5key();
		return m.getkeyBeanofStr(originalStr);
	}

	/**
	 * @Name: encipher
	 * @Description:加密一个字符串
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-04 （创建日期）
	 * @Parameters: String str(需要加密的原始字符串)
	 * @Return: 加密结果。
	 */
	public static String encipher(String str) {
		int size = secretKeyArr.length;
		String result = str;

		for (int i = 0; i < size; i++) {
			if(secretKeyArr[i] == 0) {
				result = base64Encode(result);
			}
			else {
				result = reverse(result, secretKeyArr[i]);
			}
		}

		return result;
	}

	/**
	 * @Name: decipher
	 * @Description:解密密一个字符串.
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-04 （创建日期）
	 * @Parameters: String str(需要解密的字符串)
	 * @Return: 解密结果。
	 */
	public static String decipher(String str) {
		int size = secretKeyArr.length;
		String result = str;

		for (int i = 0; i < size; i++) {
			if(secretKeyArr[size - 1 - i] == 0) {
				result = base64Decode(result);
			}
			else {
				result = reverse(result, secretKeyArr[size - 1 - i]);
			}
		}

		return result;
	}

	/**
	 * @Name: base64Encode
	 * @Description:使用base64编码一个字符串。
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-04 （创建日期）
	 * @Parameters: String str(需要编码的字符串)
	 * @Return: 编码结果。
	 */
	public static String base64Encode(String str) {
		if (StringUtils.isEmpty(str)) {
			return null;
		}
		return Base64.encodeBase64String(str.getBytes());
	}

	/**
	 * @Name: base64Decode
	 * @Description:解码一个base64编码的字符串。
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-04 （创建日期）
	 * @Parameters: String str(需要解码的字符串)
	 * @Return: 编码结果。
	 */
	public static String base64Decode(String str) {

		try {
			return new String(Base64.decodeBase64(str));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("解密失败!");
		}
	}

	/**
	 * @Name: reverse
	 * @Description:反转一个字符串.
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-04 （创建日期）
	 * @Parameters: String str
	 * @Return: 计算结果。
	 */
	public static String reverse(String str) {
		byte[] buff = str.getBytes();
		byte temp;
		int size = str.length();

		for (int i = 0; i < size / 2; i++) {
			temp = buff[i];
			buff[i] = buff[size - 1 - i];
			buff[size - 1 - i] = temp;
		}

		return new String(buff, 0, size);
	}
	
	/**
	 * @Name: reverse
	 * @Description:反转一个数组，反转的长度为指定的长度。
	 * 				当长度大于该数组的长度的时候，则整个数组反转，超出的部分将会被忽略。
	 * 				反转是从开始或者是末尾为位置，这里根据一个长度而定，如果长度模2为0
	 * 				则是从开始计算，否则为从末尾开始计算。
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-05 （创建日期）
	 * @Parameters: String str
	 * @Return: 计算结果。
	 */
	private static String reverse(String str, int len) {
		byte[] buff = str.getBytes();
		byte temp;
		boolean positionAtFirst = len % 2 == 0 ? true : false; 
		int size = len >  str.length()? str.length() : len;
		
		if(size == str.length()) {
			return reverse(str);
		}
		
		if(positionAtFirst) {
			for (int i = 0; i < size / 2; i++) {
				temp = buff[i];
				buff[i] = buff[size - 1 - i];
				buff[size - 1 - i] = temp;
			}
			return new String(buff, 0, buff.length);
		}
		else {
			String tempStr = str.substring(str.length() - len);
			return str.substring(0, str.length() - len) + reverse(tempStr);
		}
	}
	
	/**
	 * @Name: hasChinese
	 * @Description:判断一个字符串中是否含有汉字，如果有返回true,否则放回false
	 * @Author: YXC（作者）
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2016-10-04 （创建日期）
	 * @Parameters: String str
	 * @Return: 判断结果。
	 */
	public static boolean hasChinese(String str) {
		return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find();
	}

	public static String removeEmptyStr(String str) {
		return str.replaceAll("\\s+", "");
	}
	
	/**
	 * @Name: uuid
	 * @Description:产生一个uuid字符串。
	 * @Author: YXC
	 * @Version: V1.00
	 * @Create Date: 2017-05-18
	 * @Parameters: 
	 * @Return: 产生的UUID。
	 */
	public static String uuid() {
		return UUID.randomUUID().toString();
	}

	private static final String[] hex = {
			"00","01","02","03","04","05","06","07","08","09","0A","0B","0C","0D","0E","0F",
			"10","11","12","13","14","15","16","17","18","19","1A","1B","1C","1D","1E","1F",
			"20","21","22","23","24","25","26","27","28","29","2A","2B","2C","2D","2E","2F",
			"30","31","32","33","34","35","36","37","38","39","3A","3B","3C","3D","3E","3F",
			"40","41","42","43","44","45","46","47","48","49","4A","4B","4C","4D","4E","4F",
			"50","51","52","53","54","55","56","57","58","59","5A","5B","5C","5D","5E","5F",
			"60","61","62","63","64","65","66","67","68","69","6A","6B","6C","6D","6E","6F",
			"70","71","72","73","74","75","76","77","78","79","7A","7B","7C","7D","7E","7F",
			"80","81","82","83","84","85","86","87","88","89","8A","8B","8C","8D","8E","8F",
			"90","91","92","93","94","95","96","97","98","99","9A","9B","9C","9D","9E","9F",
			"A0","A1","A2","A3","A4","A5","A6","A7","A8","A9","AA","AB","AC","AD","AE","AF",
			"B0","B1","B2","B3","B4","B5","B6","B7","B8","B9","BA","BB","BC","BD","BE","BF",
			"C0","C1","C2","C3","C4","C5","C6","C7","C8","C9","CA","CB","CC","CD","CE","CF",
			"D0","D1","D2","D3","D4","D5","D6","D7","D8","D9","DA","DB","DC","DD","DE","DF",
			"E0","E1","E2","E3","E4","E5","E6","E7","E8","E9","EA","EB","EC","ED","EE","EF",
			"F0","F1","F2","F3","F4","F5","F6","F7","F8","F9","FA","FB","FC","FD","FE","FF"
	};

	private static final byte[] val = {
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,
			0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F,0x3F
	};

	public static String escape(String s) {
		StringBuffer sbuf = new StringBuffer();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int ch = s.charAt(i);
			if (ch == ' ') {                        // space : map to '+'
				sbuf.append('+');
			} else if ('A' <= ch && ch <= 'Z') {    // 'A'..'Z' : as it was
				sbuf.append((char)ch);
			} else if ('a' <= ch && ch <= 'z') {    // 'a'..'z' : as it was
				sbuf.append((char)ch);
			} else if ('0' <= ch && ch <= '9') {    // '0'..'9' : as it was
				sbuf.append((char)ch);
			} else if (ch == '-' || ch == '_'       // unreserved : as it was
					|| ch == '.' || ch == '!'
					|| ch == '~' || ch == '*'
					|| ch == '/' || ch == '('
					|| ch == ')') {
				sbuf.append((char)ch);
			} else if (ch <= 0x007F) {              // other ASCII : map to %XX
				sbuf.append('%');
				sbuf.append(hex[ch]);
			} else {                                // unicode : map to %uXXXX
				sbuf.append('%');
				sbuf.append('u');
				sbuf.append(hex[(ch >>> 8)]);
				sbuf.append(hex[(0x00FF & ch)]);
			}
		}
		return sbuf.toString();
	}

	public static String unescape(String s) {
		StringBuffer sbuf = new StringBuffer();
		int i = 0;
		int len = s.length();
		while (i < len) {
			int ch = s.charAt(i);
			if (ch == '+') {                        // + : map to ' '
				sbuf.append(' ');
			} else if ('A' <= ch && ch <= 'Z') {    // 'A'..'Z' : as it was
				sbuf.append((char)ch);
			} else if ('a' <= ch && ch <= 'z') {    // 'a'..'z' : as it was
				sbuf.append((char)ch);
			} else if ('0' <= ch && ch <= '9') {    // '0'..'9' : as it was
				sbuf.append((char)ch);
			} else if (ch == '-' || ch == '_'       // unreserved : as it was
					|| ch == '.' || ch == '!'
					|| ch == '~' || ch == '*'
					|| ch == '/' || ch == '('
					|| ch == ')') {
				sbuf.append((char)ch);
			} else if (ch == '%') {
				int cint = 0;
				if ('u' != s.charAt(i+1)) {         // %XX : map to ascii(XX)
					cint = (cint << 4) | val[s.charAt(i+1)];
					cint = (cint << 4) | val[s.charAt(i+2)];
					i+=2;
				} else {                            // %uXXXX : map to unicode(XXXX)
					cint = (cint << 4) | val[s.charAt(i+2)];
					cint = (cint << 4) | val[s.charAt(i+3)];
					cint = (cint << 4) | val[s.charAt(i+4)];
					cint = (cint << 4) | val[s.charAt(i+5)];
					i+=5;
				}
				sbuf.append((char)cint);
			}
			i++;
		}
		return sbuf.toString();
	}

	/**
	 * 只对 url 的中文字符进行编码, 如果失败返回原 url.
	 * @param url
	 * @param enc
	 */
	public static String escapeUrlOnlyChineseCharacter(String url, String enc) {
		try {
			String tStr = url;
			String chineseRegex = "[\u4e00-\u9fa5]+";
			Pattern pattern = Pattern.compile(chineseRegex);
			Matcher matcher = pattern.matcher(tStr);
			while(matcher.find()) {
				String result = matcher.group();
				tStr = tStr.replaceAll(result, URLEncoder.encode(result, enc));
			}
			return tStr;
		} catch (Exception e) {
			return url;
		}
	}

	/**
	 * 获取第一个非空字符串
	 * @param strs
	 */
	public static String getFirstNotEmptyStr(String... strs) {
		Optional<String> firstOp = Arrays.stream(strs)
				.filter(str -> org.apache.commons.lang3.StringUtils.isNotEmpty(str))
				.findFirst();
		if(firstOp.isPresent()) {
			return firstOp.get();
		}
		return null;
	}
}