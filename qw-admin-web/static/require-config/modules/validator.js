/**
 * 包含所有日常数据校验方法.
 */
define(function() {
	
	var validator = {
		// 校验用户名
		validUsername : function(username) {
			//用户名正则，4到16位（字母，数字，下划线，减号）
			var uPattern = /^[a-zA-Z0-9_-]{4,16}$/;
			return uPattern.test(username);
		},
		// 强密码规则校验
		validStrongPwd : function(pwd) {
			//密码强度正则，最少6位，包括至少1个大写字母，1个小写字母，1个数字，1个特殊字符
			var pPattern = /^.*(?=.{6,})(?=.*\d)(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*? ]).*$/;
			return pPattern.test(pwd);
		},
		// 校验整数
		validInteger : function(integer, flag) {
			//正整数正则
			var posPattern = /^\d+$/;
			//负整数正则
			var negPattern = /^-\d+$/;
			//整数正则
			var intPattern = /^-?\d+$/;
			
			var tVal = integer;
			
			if((typeof integer) !== 'string') {
				tVal = integer + '';
			}
			
			if(flag === '+') {
				return posPattern.test(tVal);
			} else if(flag === '-') {
				return negPattern.test(tVal);
			} else {
				return intPattern.test(tVal);
			}
		},
		// 校验数字
		validNum : function(number, flag){
			//正数正则
			var posPattern = /^\d*\.?\d+$/;
			//负数正则
			var negPattern = /^-\d*\.?\d+$/;
			//数字正则
			var numPattern = /^-?\d*\.?\d+$/;
			
			var tVal = number;
			
			if((typeof number) !== 'string') {
				tVal = number + '';
			}
			
			if(flag === '+') {
				return posPattern.test(tVal);
			} else if(flag === '-') {
				return negPattern.test(tVal);
			} else {
				return numPattern.test(tVal);
			}
		},
		// 校验邮箱号
		validEmail : function(email) {
			var ePattern = /^([A-Za-z0-9_\-\.])+\@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
			return ePattern.test(email);
		},
		// 校验手机号码
		validPhone : function(phone) {
			//手机号正则
			var mPattern = /^1[34578]\d{9}$/;
			return mPattern.test(phone);
		},
		// 验证身份证号码
		validID : function(id) {
			// 身份证号正则
			var cPattern = /^[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;
			return cPattern.test(id);
		},
		// 验证 URL 正则
		validURL : function(url) {
			//URL正则
			var urlPattern = /^((https?|ftp|file):\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/;
			return urlPattern.test(url);
		},
		// 验证ipv4 地址
		validIPV4 : function(ipv4Address) {
			//ipv4地址正则
			var ipPattern = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
			//输出 true
			return ipPattern.test(ipv4Address);
		},
		// 验证16进制颜色
		validHexColor : function(hexColor) {
			var hcPattern = /^#?([a-fA-F0-9]{6}|[a-fA-F0-9]{3})$/;
			return hcPattern.test(hexColor);
		},
		// 校验日期
		validHexDate : function(date) {
			var datePattern = /^(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)$/;
			return dPattern.test(date);
		},
		//校验QQ号
		validQQ : function(qq) {
			var qqPattern = /^[1-9][0-9]{4,10}$/;
			return qqPattern.test(qq);
		},
		//校验微信号
		validWeiChat : function(weixin) {
			var wxPattern = /^[a-zA-Z]([-_a-zA-Z0-9]{5,19})+$/;
			return wxPattern.test(weixin);
		},
		//校验车牌号
		validPlateNum : function(plateNum) {
			var pnPattern = /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-Z0-9]{4}[A-Z0-9挂学警港澳]{1}$/;
			return pnPattern.test(plateNum);
		},
		//校验包含中文正则
		validChinese : function(chinese) {
			//包含中文正则
			var cnPattern = /[\u4E00-\u9FA5]/;
			return cnPattern.test(chinese);
		},
		//校验码头货箱号码
		validContainerNum : function(containerNum) {
			var strCode = containerNum;
			var Charcode = "0123456789A?BCDEFGHIJK?LMNOPQRSTU?VWXYZ";
			if (strCode.length != 11) return false;
			var result = true;
			var num = 0;
			for (var i = 0; i < 10; i++) {
				var idx = Charcode.indexOf(strCode[i]);
				if (idx == -1 || Charcode[idx] == '?') {
					result = false;
					break;
				}
				idx = idx * Math.pow(2, i);
				num += idx;
			}
			num = (num % 11) % 10;
			return parseInt(strCode[10]) == num;
		},
		// 验证是否为空字符串
		validEmpty : function(val) {
			var tVal = val.trim();
			return tVal === '';
		},
		// 校验金额
		validMoney : function(val, allowedZero) {
			if(typeof val !== 'string') {
				val = val + '';				
			}

			allowedZero = allowedZero === undefined ? true : !!allowedZero;

			if(allowedZero && parseInt(val) === 0) {
				return true;
			}

			var tVal = val.trim();
			var reg =/^(([1-9][0-9]*)|(([0]\.\d{1,2}|[1-9][0-9]*\.\d{1,2})))$/;
			return reg.test(tVal);
		},
		// 校验域名
		validDomain : function(val) {
			if(typeof val !== 'string') {
				val = val + '';				
			}
			var tVal = val.trim();
			var reg =/^(?=^.{3,255}$)[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$/;
			return reg.test(tVal);
		}
	};
	
	return validator;
});