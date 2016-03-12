package cn.com.aladdinet.controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.com.aladdinet.service.MobileService;
import cn.com.aladdinet.util.HttpsUtil;
/**
* 项目名称：PushServer   
* 类名称：MobileController   
* 创建人：huli   
* 创建时间：2016-2-2 下午12:28:02      
 */
@Controller
@RequestMapping("/mobile")
public class MobileController {
	protected Logger log=Logger.getLogger(MobileController.class);
	@Autowired
	private MobileService mobileService;
	/**
	 * 推送
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/push")
	public String push(HttpServletRequest request,HttpServletResponse response){
		String jsonFromRequest = HttpsUtil.getJsonFromRequest(request);
		log.info("accept json "+jsonFromRequest);
		String resJson=mobileService.push2Client(jsonFromRequest);
		HttpsUtil.sendAppMessage(resJson, response);
		return null;
	}
}
