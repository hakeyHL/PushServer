package cn.com.aladdinet.util;

import net.sf.json.JSONObject;

/**
 * 用于对每个提交请求的客户端的版本检测控制
*  规则:只支持当前版本,以往版本不兼容。
* 项目名称：AuthorizationServer   
* 类名称：VersionUtil   
* 创建人：huli   
* 创建时间：2015-12-26 上午9:38:55      
 */
public class VersionUtil {
	public static String currentVersion="1.1.0";
	public static boolean versionCheck(String plateform,String version){
		return true;
	}
	/**
	 * @param build 内部版本
	 * @param version 使用版本
	 * @return 是否可用
	 */
	public static JSONObject standVersionCheck(String platform,String build,String version){
		JSONObject v_check_result=new JSONObject();
		if(platform.equals("")||platform==null){
			//如果平台没有传,直接规避
			v_check_result.put("errorcode", "00000");
			v_check_result.put("msg", "通过");
			return v_check_result;
		}
		//1.取出四个版本属性参数值
		String adroidVersionSupport = PropertyFactory.getProperty("a_version_v");
		String adroidBuildSupport = PropertyFactory.getProperty("a_version_b");
		String iPhoneVersionSupport = PropertyFactory.getProperty("i_version_v");
		String iPhoneBuildSupport = PropertyFactory.getProperty("a_version_b");
		
		//2.将所有版本转为整数进行比较
		int t_adroidVersionSupport=getVersionInt(adroidVersionSupport);
		int t_adroidBuildSupport=Integer.valueOf(adroidBuildSupport);
		int t_iPhoneVersionSupport=getVersionInt(iPhoneVersionSupport);
		int t_iPhoneBuildSupport=Integer.valueOf(iPhoneBuildSupport);
		//服务器版本
		int t_serverVersion=getVersionInt(currentVersion);
		
		int t_build=Integer.valueOf(build);
		int t_version=getVersionInt(version);
		//3.根据客户端传来参数匹配比较
		if(platform.equals("android")){
			//如果是安卓
			if(t_version<t_adroidVersionSupport){
				//如果客户端版本低于server最小版本,提示升级
				v_check_result.put("errorcode", "20008");
				v_check_result.put("msg", "版本过低,请升级");
				return v_check_result;
			}
			if(t_version==t_adroidVersionSupport&&t_build<t_adroidBuildSupport){
				//如果客户端version满足,但build版本小于服务端版本也提示升级
				v_check_result.put("errorcode", "20008");
				v_check_result.put("msg", "版本过低,请升级");
				return v_check_result;
			}
			if(t_version>t_serverVersion){
				//如果移动端版本大于了server版本,提示请联系管理员对服务系统升级
				v_check_result.put("errorcode", "20010");
				v_check_result.put("msg", "当前server版本过低,请联系管理员对server升级");
				return v_check_result;
			}
		}else{
			//否则为苹果
			if(t_version<t_iPhoneVersionSupport){
				//如果客户端版本低于server最小版本,提示升级
				v_check_result.put("errorcode", "20008");
				v_check_result.put("msg", "版本过低,请升级");
				return v_check_result;
			}
			if(t_version==t_iPhoneVersionSupport&&t_build<t_iPhoneBuildSupport){
				//如果客户端version满足,但build版本小于服务端版本也提示升级
				v_check_result.put("errorcode", "20008");
				v_check_result.put("msg", "版本过低,请升级");
				return v_check_result;
			}
			if(t_version>t_serverVersion){
				//如果移动端版本大于了server版本,提示请联系管理员对服务系统升级
				v_check_result.put("errorcode", "20010");
				v_check_result.put("msg", "当前server版本过低,请联系管理员对server升级");
				return v_check_result;
			}
		}
		//4.将结果返回
		//其他情况,全部通过
		v_check_result.put("errorcode", "00000");
		v_check_result.put("msg", "通过");
		return v_check_result;
	}
	/**
	 * 将version/build转为整数
	 * @param version 只取前两位
	 * @return
	 */
	public static Integer getVersionInt(String version){
		String []temp=version.split("\\.");
		int result=0;
		/*for(int i=0;i<temp.length;i++){
			result=result*10+Integer.valueOf(temp[i]);
		}*/
		for(int i=0;i<2;i++){
			result=result*10+Integer.valueOf(temp[i]);
		}
		return result;
	}
}
