package cn.com.aladdinet.util;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.apache.log4j.Logger;

public class AppSecurity {
	protected static Logger log = Logger.getLogger(AppSecurity.class);
	public static String success="1";
	public static String code="code";
	public static String msg="msg";
	
	public static Map<String,String> valid(String timestamp,String authenticator ){
		log.info("timestamp = "+timestamp);
		log.info("authenticator = "+authenticator);
		Map<String,String> map = new HashMap<String,String>();
		if(timestamp.equals("")){
			map.put(AppSecurity.code, "2");
			map.put(AppSecurity.msg, "时间戳不能为空");
			log.info("时间戳不能为空");
			return map;
		}
		
		if(authenticator.equals("")){
			map.put(AppSecurity.code, "2");
			map.put(AppSecurity.msg, "认证码不能为空");
			log.info("认证码不能为空");
			return map;
		}
		
		String local_authenticator = CipherUtil.encryptWithSHA256(StringUtil.defaultKey + timestamp);
		if (!local_authenticator.equals(authenticator)) {
			map.put(AppSecurity.code, "2");
			map.put(AppSecurity.msg, "认证错误");
			return map;
		}
		
		Date d1 = null;
		try {
			d1 = DateHelper.format(timestamp);
		} catch (ParseException e) {
			map.put(AppSecurity.code, "2");
			map.put(AppSecurity.msg, "时间戳格式不正确");
			log.info("时间戳格式不正确"+timestamp);
			return map;
		}
		//去掉超时验证
//		Date d2 = new Date();
//		long diff = (d2.getTime() - d1.getTime()) / 1000;
//		if (diff > 300) {
//			map.put(AppSecurity.code, "2");
//			map.put(AppSecurity.msg,PropertyFactory.getErrorProperty(ErrorCode.REQUESTTIMEOUT));
//			log.info(PropertyFactory.getErrorProperty(ErrorCode.REQUESTTIMEOUT));
//			return map;
//		}
		map.put(AppSecurity.code, "1");
		return  map;
	}
	
	public static void main(String [] args){
		TimeZone tz = TimeZone.getDefault();
		for(String a :tz.getAvailableIDs()){
			if(a.contains("/") && !a.contains("GMT")){
				System.out.println((TimeZone.getTimeZone(a).getRawOffset()/1000/60/60));
			}
		}
	}
}
