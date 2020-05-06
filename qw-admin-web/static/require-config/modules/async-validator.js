/**
 * 包含所有日常数据校验方法.
 */
define(['validator'], function(Util) {

	function __getMsg(rule, defaultVal) {
		return 'message' in rule ? 	rule.message : defaultVal;
	}

	var asyncValidator = {
		// 校验用户名
		validUsername : function(rule, value, callback) {
			var ok = Util.validUsername(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule,'名称格式不正确'));
			}
		},
		// 强密码规则校验
		validStrongPwd : function(rule, value, callback) {
			var ok = Util.validStrongPwd(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '密码强度不够'));
			}
		},
		// 校验整数
		validInteger : function(rule, value, callback) {
			var ok = Util.validInteger(value, rule.flag);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '整数格式不正确'));
			}
		},
		// 校验数字
		validNum : function(rule, value, callback){
			var ok = Util.validNum(value, rule.flag);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '数字格式不正确'));
			}
		},
		// 校验邮箱号
		validEmail : function(rule, value, callback) {
			var ok = Util.validEmail(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '邮箱格式不正确'));
			}
		},
		// 校验手机号码
		validPhone : function(rule, value, callback) {
			var ok = Util.validPhone(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule,'手机号格式不正确'));
			}
		},
		// 验证身份证号码
		validID : function(rule, value, callback) {
			var ok = Util.validID(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '身份证号码格式不正确'));
			}
		},
		// 验证 URL 正则
		validURL : function(rule, value, callback) {
			var ok = Util.validURL(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, 'URL地址格式不正确'));
			}
		},
		// 验证ipv4 地址
		validIPV4 : function(rule, value, callback) {
			var ok = Util.validIPV4(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, 'IPV4地址格式不正确'));
			}
		},
		// 验证16进制颜色
		validHexColor : function(rule, value, callback) {
			var ok = Util.validHexColor(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '16进制颜色格式不正确'));
			}
		},
		// 校验日期
		validHexDate : function(rule, value, callback) {
			var ok = Util.validHexDate(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '日期格式不正确'));
			}
		},
		//校验QQ号
		validQQ : function(rule, value, callback) {
			var ok = Util.validQQ(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, 'QQ号码格式不正确'));
			}
		},
		//校验微信号
		validWeiChat : function(rule, value, callback) {
			var ok = Util.validWeiChat(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '微信号格式不正确'));
			}
		},
		//校验车牌号
		validPlateNum : function(rule, value, callback) {
			var ok = Util.validPlateNum(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '车牌号格式不正确'));
			}
		},
		//校验包含中文正则
		validChinese : function(rule, value, callback) {
			var ok = Util.validChinese(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '不能包含非中文字符'));
			}
		},
		//校验码头货箱号码
		validContainerNum : function(rule, value, callback) {
			var ok = Util.validContainerNum(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '箱号格式不正确'));
			}
		},
		// 验证是否为空字符串
		validEmpty : function(rule, value, callback) {
			var ok = Util.validEmpty(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '不能为空'));
			}
		},
		// 校验金额
		validMoney : function(rule, value, callback) {
			var ok = Util.validMoney(value, rule.allowedZero);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '金额格式不正确'));
			}
		},
		// 校验域名
		validDomain : function(rule, value, callback) {
			var ok = Util.validDomain(value);

			if(ok) {
				callback();
			} else {
				callback(__getMsg(rule, '域名格式不正确'));
			}
		}
	};
	
	return asyncValidator;
});