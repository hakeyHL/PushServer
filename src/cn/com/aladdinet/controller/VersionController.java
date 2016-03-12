package cn.com.aladdinet.controller;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.com.aladdinet.util.GetVersion;
import cn.com.aladdinet.util.HttpsUtil;
@Controller
public class VersionController {
	/**
	 * 获取版本信息
	 * @param request
	 * @param response
	 * @return 当前版本
	 */
	@RequestMapping("/version")
	public String getVersion(HttpServletResponse response){
		String yqtversion = GetVersion.version;
		HttpsUtil.sendAppMessage(yqtversion, response);
		return null;
	}
}
