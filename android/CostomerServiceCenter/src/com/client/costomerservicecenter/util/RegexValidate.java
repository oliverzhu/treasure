/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2012-3-2
 */
package com.client.costomerservicecenter.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式验证工具类
 * 
 * @author MichaelHuang
 * 
 */
public class RegexValidate {

	// 合法字符正则表达式
	private final static String LAWFUL_EXPREG_STR = ".*(<script|select|update|delete|insert|').*";
	// 用户名正则格式验证(length > 5)
	private final static String USER_PATTERN_STR = "^[_0-9a-zA-Z]{6,12}$";
	// 文件名正则格式验证(length > 0)
	private final static String FILENAME_PATTERN_STR = "^[_0-9a-zA-Z\u4e00-\u9fa5]+$";
	// 手机号码正则表达式
	private final static String MOBILETELL_EXPREG_STR = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
	// 电话号码,传真正则表达式(兼容格式: 国家代码(4到5位)-区号(3到4位)-电话号码(7到8位)
	private final static String TELL_EXPREG_STR = "^(([0]\\d{3,4}-)?(0\\d{2,3})-)?(\\d{7,8})$";
	// QQ
	private final static String QQ_EXPREG_STR = "^[1-9]\\d{4,8}$";
	// 汉字
	private final static String CHINESE_EXPREG_STR = "^[\u4e00-\u9fa5_]+$";
	// 邮箱
	private final static String EMAIL_EXPREG_STR = "^(([0-9a-zA-Z]+)|([0-9a-zA-Z]+[_.0-9a-zA-Z-]*[0-9a-zA-Z]+))@([a-zA-Z0-9-]+[.])+([a-zA-Z]{2}|net|NET|com|COM|gov|GOV|mil|MIL|org|ORG|edu|EDU|int|INT)$";
	// 邮编
	private final static String POSTCODE_EXPREG_STR = "^[1-9]\\d{5}(?!\\d)$";
	// 日期 yyyy-mm-dd
	private final static String DATE_EXPREG_STR = "^((((1[6-9]|[2-9]\\d)\\d{2})-(0?[13578]|1[02])-(0?[1-9]|[12]\\d|3[01]))|(((1[6-9]|[2-9]\\d)\\d{2})-(0?[13456789]|1[012])-(0?[1-9]|[12]\\d|30))|(((1[6-9]|[2-9]\\d)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|(((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))-0?2-29-))$";
	// 自然数
	private final static String NATURE_NUMBER = "^[1-9]\\d*|0$";
	// 非负整数
	private final static String SIGNED_INTEGER_NUMBER = "^\\d+$";
	
	private final static String ID_NUMBER = "//d{15}|//d{18}";
	// URL
	private final static String URL_EXPREG_STR = "^((https|http|ftp|rtsp|mms)?://)(([0-9a-zA-Z_!~*'().&=+$%-]+: )?[0-9a-zA-Z_!~*'().&=+$%-]+@)?(([0-9]{1,3}.){3}[0-9]{1,3}|([0-9a-zA-Z_!~*'()-]+.)*([0-9a-zA-Z][0-9a-zA-Z-]{0,61})?[0-9a-zA-Z].[a-zA-Z]{2,6})(:[0-9]{1,4})?((/?)|(/[0-9a-zA-Z_!~*'().;?:@&=+$,%#-]+)+/?)$";
	//
	private static String SENSITIVITY_WORD = "sensitivityWord";
	private static String SENSITIVITY_WORD_ALL_TEXT = "sensitivityWord_allText";

	
	public static boolean isIDNumber(String text)
	{
		if(text == null)
		{
			return false;
		}
		if(text.length() != 15 && text.length() != 18)
		{
			return false;
		}
		return true;
	}
	/**
	 * 验证文件名格式是否正确,正确返回true
	 * 
	 * @author wangdawei
	 * @date 2010-2-24 上午09:58:19
	 * @param text
	 * @return boolean
	 */
	public static boolean isSpecialFileName(String text) {
		return matchingText(FILENAME_PATTERN_STR, text) || isChinese(text);
	}

	/**
	 * 验证用户名格式是否正确,正确返回true
	 * 
	 * @author wangdawei
	 * @date 2010-2-24 上午09:58:19
	 * @param text
	 * @return boolean
	 */
	public static boolean isSpecialUserName(String text) {
		return matchingText(USER_PATTERN_STR, text);
	}

	/**
	 * 验证是否是正整数，包含0
	 * 
	 * @author 李英夫(2009-12-31 下午02:25:16)
	 * @param inVal
	 * @return boolean
	 */
	public static boolean isSignedIntegerNumber(String text) {
		return matchingText(SIGNED_INTEGER_NUMBER, text);
	}

	/**
	 * validate url
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isURL(String url) {
		return isChinese(url) || matchingText(URL_EXPREG_STR, url);
	}

	/**
	 * 验证是否是数字，包含浮点数
	 * 
	 * @author 李英夫(2009-12-31 下午02:22:16)
	 * @param inVal
	 * @return boolean
	 */
	public static boolean isDigit(String inVal) {
		if (inVal == null || inVal.length() < 1)
			return false;
		boolean isRight = true;
		try {
			Float.parseFloat(inVal);
		} catch (NumberFormatException nfe) {
			isRight = false;
		}
		return isRight;
	}

	/**
	 * 是否存在非法字符，存在则返回true
	 * 
	 * @author 李英夫(2009-12-31 下午02:26:16)
	 * @param text
	 * @return boolean
	 */
	public static boolean isSpecialword(String text) {
		return matchingText(LAWFUL_EXPREG_STR, text);
	}

	/**
	 * 电话，正确则返回true
	 * 
	 * @author 李英夫(2009-12-31 下午02:26:16)
	 * @param text
	 * @return boolean
	 */
	public static boolean telPhone(String text) {
		return matchingText(TELL_EXPREG_STR, text);
	}

	/**
	 * 是否是手机号码
	 * 
	 * @author 李英夫(2009-12-31 下午02:27:44)
	 * @param text
	 * @return boolean
	 */
	public static boolean isMobilPhone(String text) {
		return matchingText(MOBILETELL_EXPREG_STR, text);
	}

	/**
	 * 是否是QQ
	 * 
	 * @author 李英夫(2009-12-31 下午02:29:50)
	 * @param text
	 * @return boolean
	 */
	public static boolean isQQ(String text) {
		return matchingText(QQ_EXPREG_STR, text);
	}

	/**
	 * 是否是汉字
	 * 
	 * @author 李英夫(2009-12-31 下午02:31:53)
	 * @param text
	 * @return boolean
	 */
	public static boolean isChinese(String text) {
		return matchingText(CHINESE_EXPREG_STR, text);
	}

	/**
	 * 是否是合法邮编
	 * 
	 * @author 李英夫(2009-12-31 下午02:32:57)
	 * @param text
	 * @return boolean
	 */
	public static boolean isPostCode(String text) {
		return matchingText(POSTCODE_EXPREG_STR, text);
	}

	/**
	 * 是否是电子邮件
	 * 
	 * @author 李英夫(2009-12-31 下午02:33:49)
	 * @param text
	 * @return boolean
	 */
	public static boolean isEMail(String text) {
		return matchingText(EMAIL_EXPREG_STR, text);
	}

	/**
	 * 是否是yyyy-MM-dd格式的日期
	 * 
	 * @author 李英夫(2009-12-31 下午02:34:33)
	 * @param text
	 * @return boolean
	 */
	public static boolean isDate(String text) {
		return matchingText(DATE_EXPREG_STR, text);
	}

	/**
	 * 是否是自然数
	 * 
	 * @author 李英夫(2009-12-31 下午02:35:24)
	 * @param text
	 * @return boolean
	 */
	public static boolean isNatureNumber(String text) {
		return matchingText(NATURE_NUMBER, text);
	}

	/**
	 * 有敏感词返回true
	 * 
	 * @author 李英夫(2010-1-21 下午02:13:01)
	 * @param text
	 * @return boolean
	 */

	public static boolean isSensitivityWord(String text) {
		// try {
		// String sw = PropertiesLoader.getText(SENSITIVITY_WORD_ALL_TEXT);
		// return matchingText(sw, text);
		// } catch (Exception e) {
		// e.printStackTrace();
		// return true;
		// }
		return false;
	}

	/**
	 * 替换敏感词
	 * 
	 * @author 李英夫(2010-1-21 下午04:48:04)
	 * @param text
	 * @param newWord
	 * @return String
	 */
	public static String replaceSensitivityWord(String text, String newWord) {
		// try {
		// Pattern p = Pattern.compile(PropertiesLoader
		// .getText(SENSITIVITY_WORD)); // 正则表达式
		// Matcher m = p.matcher(text); // 操作的字符串
		// return m.replaceAll(newWord);
		// } catch (Exception e) {
		// e.printStackTrace();
		// return "";
		// }
		return "";
	}

	/**
	 * 正则验证
	 * 
	 * @author 李英夫(2009-12-31 下午02:20:16)
	 * @param expression
	 * @param text
	 * @return boolean
	 */
	public static boolean matchingText(String expression, String text) {
		boolean bool = false;
		if (expression != null && !"".equals(expression) && text != null
				&& !"".equals(text)) {
			Pattern p = Pattern.compile(expression); // 正则表达式
			Matcher m = p.matcher(text); // 操作的字符串
			bool = m.matches();
		}
		return bool;
	}

	/**
	 * 替换Html中的form标签、on..事件属性、href='javascript...'引用
	 * 
	 * @author 李英夫(2010-5-6 上午09:07:52)
	 * @param src
	 * @return String
	 */
	public static String replaceFOH(String src) {
		return src
				.replaceAll(
						"<form.*?</form>|on[a-zA-z]+=([\"']?)[^\"']*\\1|href=([\"']?)javascript:[^\"']*\\2",
						"");
	}
}
