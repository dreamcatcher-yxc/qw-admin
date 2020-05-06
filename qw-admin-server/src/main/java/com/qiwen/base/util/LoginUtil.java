package com.qiwen.base.util;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;

public class LoginUtil {

    private static final String REMEMBER_ME_COOKIE_NAME = "imageInformation";

    @Data
    public static class RememberInfo {
    	private String username;

    	private String password;

    	private long deadLine;

    	private boolean expired;
	}

	/**
	 * hex转byte数组
	 * @param hex
	 * @return
	 */
	public static byte[] hexToByte(String hex){
		int m = 0, n = 0;
		int byteLen = hex.length() / 2; // 每两个字符描述一个字节
		byte[] ret = new byte[byteLen];
		for (int i = 0; i < byteLen; i++) {
			m = i * 2 + 1;
			n = m + 1;
			int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
			ret[i] = Byte.valueOf((byte)intVal);
		}
		return ret;
	}

	/**
	 * byte数组转hex
	 * @param bytes
	 * @return
	 */
	public static String byteToHex(byte[] bytes){
		String strHex = "";
		StringBuilder sb = new StringBuilder("");
		for (int n = 0; n < bytes.length; n++) {
			strHex = Integer.toHexString(bytes[n] & 0xFF);
			sb.append((strHex.length() == 1) ? "0" + strHex : strHex); // 每个字节由两个字符表示，位数不够，高位补0
		}
		return sb.toString().trim();
	}

	/**
	* @Name: checkValidateNum
	* @Description: 检测用户输入的验证码和实际的验证码是否一致。
	* @Author: YXC
	* @Version: V1.00
	* @Create Date: 2016-10-04
	* @Parameters: 无。
	* @Return: boolean
	*/
	public static boolean checkValidateNum(HttpServletRequest request, String checkNumberKey, boolean ignoreCase) {
		HttpSession session = request.getSession(false);

		if(session == null) {
			return false;
		}

		String realCheckNumber = (String) session.getAttribute(ValidateImageUtil.CHECK_CODE_KEY);
		if(StringUtils.isEmpty(realCheckNumber)) {
			return false;
		}
		// 验证码使用一次之后即失效
		session.removeAttribute(ValidateImageUtil.CHECK_CODE_KEY);

		String checkNum = request.getParameter(checkNumberKey);
		if(StringUtils.isEmpty(checkNum)) {
			return false;
		}

		if(ignoreCase) {
			return checkNum.equalsIgnoreCase(realCheckNumber);
		}
		return checkNum.equals(realCheckNumber);
	}
	
	/**  
	* @Name: rememberElecUser
	* @Description: 记住当前登录用户一周，在一周之内访问此网站的时候将会自动登录。
	* @Author: YXC
	* @Version: V1.00
	* @Create Date: 2016-10-04
	* @Parameters: ...
	* @Return:无
	*/
	public static void rememberElecUser(HttpServletResponse response,
										String path,
										String username,
										String password,
										String... names) {
		String pwd = StringUtil.encipher(password);
		String encripUsername = StringUtil.encipher(username);
		int maxAge = 7 * 24 * 60 * 60 * 1000;
		String deadTime = (System.currentTimeMillis() + maxAge) + "";
		String value = StringUtil.encipher(encripUsername + "|" + pwd + "|" + StringUtil.encipher(deadTime));
		value = value.replaceAll("\\s*", "");
		String name;
		if(names == null || names.length == 0) {
			name = REMEMBER_ME_COOKIE_NAME;
		} else {
			name = names[0];
		}
		Cookie cookie = new Cookie(name, byteToHex(value.getBytes()));
		cookie.setMaxAge(maxAge / 1000);
		cookie.setPath(path);
		// 不允许通过客户端脚本读取 cookie 信息.
		cookie.setHttpOnly(true);
		response.addCookie(cookie);
	}

	/**
	 * @Name: forgetElecUser
	 * @Description: 使当前登陆用户不能再自动登陆。
	 * @Author: YXC
	 * @Version: V1.00
	 * @Create Date: 2018-04-03
	 * @Parameters: ...
	 * @Return:无
	 */
	public static void forgetElecUser(HttpServletResponse response, String path, String... names) {
		String name;
		if(names == null || names.length == 0) {
			name = REMEMBER_ME_COOKIE_NAME;
		} else {
			name = names[0];
		}
        Cookie cookie = new Cookie(name, "");
        cookie.setMaxAge(0);
        cookie.setPath(path);
        // 不允许通过客户端脚本读取 cookie 信息.
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
	}

	/**  
	* @Name: decipherElecUserCookie
	* @Description: 解密cookie的信息，如果解密成功，则存入一个字符串数组中!
	* 				*第一个代表logonName, 第二个代表logonPwd。
	* 				*当解密失败的时候返回null.
	* @Author: YXC
	* @Version: V1.00
	* @Create Date: 2016-10-04
	* @Parameters: HttpServletRequest request
	* @Return:解密结果。
	*/
	public static RememberInfo decipherElecUserLogonCookie(HttpServletRequest request, String... names) {
		Cookie[] cookies = request.getCookies();

		if(cookies == null) {
			return null;
		}

		String name;
		if(names == null || names.length == 0) {
			name = REMEMBER_ME_COOKIE_NAME;
		} else {
			name = names[0];
		}
		Cookie cookie = null;
		
		//寻找cookie.
		for(int i = 0; i < cookies.length; i++) {
			if(name.equals(cookies[i].getName())) {
				cookie = cookies[i];
				break;
			}
		}
		
		if(cookie == null) {
			return null;
		}
		
		//给出cookie中的数值。
		String value = cookie.getValue().replaceAll("\\s*", "");
		//System.out.println(value);
		
		if(StringUtils.isEmpty(value)) {
			return null;
		}

		String[] infoArr;
		try {
			infoArr = StringUtil.decipher(new String(hexToByte(value), "UTF-8")).split("\\|");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		//判断长度是否合法。
		if(infoArr.length != 3) {
			return null;
		}
		
		//解密三个字符串。
		for(int i = 0; i < 3; i++) {
			String str = StringUtil.decipher(infoArr[i]);
			infoArr[i] = str;
		}

		RememberInfo result = new RememberInfo();
		result.setUsername(infoArr[0]);
		result.setPassword(infoArr[1]);

		try {
			long deadTime = Long.parseLong(infoArr[2]);
			result.setDeadLine(deadTime);
			//当前时间大于有效期限，返回null.
			if(System.currentTimeMillis() > deadTime) {
				result.setExpired(true);
				return result;
			}
		} catch(Exception e) {
			return null;
		}
		
		//删除条件均满足，则认为此cookie中存储的用户自动登录信息有效，组装logonName, logonPwd数组，返回。
		result.setExpired(false);
		return result;
	}

	public static boolean isRememberMe(HttpServletRequest request, String... names) {
		RememberInfo rememberInfo = decipherElecUserLogonCookie(request, names);
		return  rememberInfo != null && !rememberInfo.isExpired();
	}
}