package cn.com.aladdinet.service;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javapns.devices.Device;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.Payload;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cn.com.aladdinet.util.PropertyFactory;
import cn.com.aladdinet.util.StringUtil;
import cn.com.aladdinet.vo.Client;

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.base.payload.APNPayload;
import com.gexin.rp.sdk.exceptions.RequestException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.LinkTemplate;
import com.gexin.rp.sdk.template.NotificationTemplate;
import com.gexin.rp.sdk.template.NotyPopLoadTemplate;
import com.gexin.rp.sdk.template.PopupTransmissionTemplate;
import com.gexin.rp.sdk.template.TransmissionTemplate;
import com.mysql.jdbc.StringUtils;
/**
 * 项目名称：PushServer 
 * 类名称：MobileService 
 * 创建人：huli 
 * 创建时间：2016-2-3 下午4:04:25
 * 
 */
@Service
public class MobileService {
	protected Logger log = Logger.getLogger(MobileService.class);
	/**
	 * 标准推送方法
	 * 
	 * @param jsonFromRequest
	 * @return
	 */
	@SuppressWarnings("all")
	public String push2Client(String jsonFromRequest) {
		log.info("enter push method ....");
		JSONObject acceptJson = JSONObject.fromObject(jsonFromRequest);
		String appId = null;
		String appkey = null;
		String master = null;
		// String host = "http://sdk.open.api.igexin.com/apiex.htm";
		String platform=acceptJson.optString("platform");
		if(platform.equals("android")){
			//安卓手机
			appId = PropertyFactory.getProperty("ANAPPID");
		    appkey = PropertyFactory.getProperty("ANAPPKEY");
			master = PropertyFactory.getProperty("ANMASTERSECRET");
		}else{
			//安卓tv
			appId = PropertyFactory.getProperty("TVAPPID");
		    appkey = PropertyFactory.getProperty("TVAPPKEY");
			master = PropertyFactory.getProperty("TVMASTERSECRET");
		}
		IGtPush push = new IGtPush(appkey, master, true);
		log.info("accept json " + jsonFromRequest);
		// 1.接收、转化数据
		String clients = acceptJson.optString("clients");
		log.info("clients " + clients);
		String content = acceptJson.optString("content");
		log.info("content is " + content);
		JSONArray clientJsonArray = JSONArray.fromObject(clients);
		List<Client> clientArray = (ArrayList<Client>) JSONArray.toCollection(
				clientJsonArray, Client.class);
		String results = null;
		// 遍历集合,依据不同平台调用不同的方法
		for (Client client : clientArray) {
			String plateform = client.getPlatform();
			log.info("platform are " + plateform);
			String clientid = client.getClientid();
			log.info("clientid are " + clientid);
			if (plateform.equals("ios")) {
				// 如果是ios平台
				String pushToIosResult = this.PushSingle2iPhone(appId, appkey,
						content, push, clientid);
				log.info("向ios端推送消息" + content + " 的结果 " + pushToIosResult);
				return pushToIosResult;
			} else {
				// 安卓/安卓tv
				try {
					results = this.PushSingle2Android(appId, appkey, content,
							push, clientid);
					log.info("推送结果..." + results);
					return results;
				} catch (Exception e) {
					log.error("向安卓端 " + clientid + " 推送消息" + content + "异常...");
					return results;
				}
			}
		}
		return results;
	}

	/**
	 * @param appId
	 * @param appkey
	 * @param content
	 * @param push
	 * @param clientid
	 * @return
	 * @throws Exception
	 */
	public String PushSingle2Android(String appId, String appkey,
			String content, IGtPush push, String clientid) throws Exception {
		String errorcode = null;
		String msg = null;
		JSONObject res = new JSONObject();
		SingleMessage message = new SingleMessage();
		TransmissionTemplate template = new TransmissionTemplate();
		template.setAppId(appId);
		template.setAppkey(appkey);
		template.setTransmissionContent(content);
		message.setData(template);
		// 过期时间为十分钟
		message.setOfflineExpireTime(10 * 60 * 1000);
		message.setPushNetWorkType(0);
		Target target1 = new Target();
		target1.setAppId(appId);
		target1.setClientId(clientid);
		try {
			IPushResult ret = push.pushMessageToSingle(message, target1);
			Map<String, Object> response = ret.getResponse();
			Set<String> keySet = response.keySet();
			Iterator<String> iterator = keySet.iterator();
			int i = 0;
			while (iterator.hasNext()) {
				String key = iterator.next();
				System.out.println(response.get(key));
				if (key.equals("result")) {
					// 说明推送成功
					if (response.get(key).equals("ok")) {
							i++;
							msg = (String) response.get(key);
					}else {
						msg = (String) response.get(key);
					}
				}
				if(key.equals("status")){
					if(response.get(key).equals("successed_online")){
						i++;
					} 
				}
			}
			// i>0则成功,其他都是离线
			if (i > 1) {
				errorcode = "00000";
			} else {
				errorcode = "B000A";
				// A代表android
			}
		} catch (RequestException e) {
			log.error("请求推送异常..." + e.getMessage(), e);
			String requstId = e.getRequestId();
			IPushResult ret = push.pushMessageToSingle(message, target1,
					requstId);
			log.error("异常结果...   " + ret.getResponse().toString());
			errorcode = "B000A";
			msg = "推送异常";
			res.put("errorcode", errorcode);
			res.put("msg", msg);
			return res.toString();
		}
		res.put("errorcode", errorcode);
		res.put("msg", msg);
		return res.toString();
	}

	/*@Test
	public String PushSingle2iPhone(String appId, String appkey,
			String content, IGtPush push, String clientid) {
		String errorcode = null;
		String msg = null;
		JSONObject res = new JSONObject();
		try {
			String deviceToken = clientid;
			// 被推送的iphone应用程序标示符
			PayLoad payLoad = new PayLoad();
			JSONObject JsonContent = JSONObject.fromObject(content);
			Iterator contentIterator = JsonContent.keys();
			while (contentIterator.hasNext()) {
				String key = (String) contentIterator.next();
				if ("nitifytype".equals(key)) {
					// 如果是通知类型则以整型获取和填入
					payLoad.addCustomDictionary(key, JsonContent.optInt(key));
				} else {
					payLoad.addCustomDictionary(key, JsonContent.optString(key));
				}
			}
			payLoad.addSound(PropertyFactory.getProperty("AppleDefaultSound"));
			JSONObject fromObject = JSONObject.fromObject(content);
			payLoad.addAlert(fromObject.optString("nickname")+"  "+fromObject.optString("phone")+"  通过恋家向您发起了一个呼叫   ");
			payLoad.addBadge(1);
			PushNotificationManager pushManager = PushNotificationManager
					.getInstance();
			pushManager.addDevice("iphone", deviceToken);

			// Device c = pushManager.getDevice("iphone");
			String host = PropertyFactory.getProperty("APPLEADDR"); // 测试用的苹果推送服务器
			int port = 2195;
			String certificatePath = PropertyFactory
					.getProperty("CERTIFICATEADDR"); // 刚才在mac系统下导出的证书

			String certificatePassword = PropertyFactory
					.getProperty("CERTIFICATEPASS");

			pushManager.initializeConnection(host, port, certificatePath,
					certificatePassword,
					SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
			// Send Push
			Device client = pushManager.getDevice("iphone");
			pushManager.setRetryAttempts(10);
			pushManager.sendNotification(client, payLoad); // 推送消息
			pushManager.stopConnection();
			pushManager.removeDevice("iphone");
			errorcode = "00000";
			msg = "成功";
		} catch (Exception e) {
			log.error("向苹果请求推送异常 " + e.getMessage(), e);
			errorcode = "B000P";
			msg = "向苹果apns请求推送异常";
			// P代表apple
		}
		res.put("errorcode", errorcode);
		res.put("msg", msg);
		return res.toString();
	}*/

	public static void sf(long time) {
		Date date = new Date(time);
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sf.format(date));

	}
	public String PushSingle2iPhone(String appId, String appkey,
			String content, IGtPush push, String clientid) {
		String errorcode = null;
		String msg = null;
		JSONObject res = new JSONObject();
		try {
			String deviceToken = clientid;
			deviceToken=deviceToken.replace(" ", "");
			System.out.println(deviceToken);
			// 被推送的iphone应用程序标示符
			PushNotificationPayload payLoad = new PushNotificationPayload();
			JSONObject JsonContent = JSONObject.fromObject(content);
			Iterator contentIterator = JsonContent.keys();
			while (contentIterator.hasNext()) {
				String key = (String) contentIterator.next();
				if ("nitifytype".equals(key)) {
					// 如果是通知类型则以整型获取和填入
					payLoad.addCustomDictionary(key, JsonContent.optInt(key));
				} else {
					payLoad.addCustomDictionary(key, JsonContent.optString(key));
				}
			}
			BasicDevice device=new BasicDevice ();
			device.setToken(deviceToken);
			BasicDevice.validateTokenFormat(deviceToken);
			//设置过期时间.单位为秒
			payLoad.setExpiry(10*60);
			//添加铃声
			payLoad.addSound(PropertyFactory.getProperty("AppleDefaultSound"));
			JSONObject fromObject = JSONObject.fromObject(content);
			payLoad.addAlert(fromObject.optString("nickname")+"  "+fromObject.optString("phone")+"  通过恋家向您发起了一个呼叫   ");
			//通知图标显示消息数量
			payLoad.addBadge(1);
			PushNotificationManager pushManager = new PushNotificationManager();
			//苹果证书的位置
			String certificatePath = PropertyFactory
					.getProperty("CERTIFICATEADDR"); // 刚才在mac系统下导出的证书
			//证书密码
			String certificatePassword = PropertyFactory
					.getProperty("CERTIFICATEPASS");
			//初始化
			AppleNotificationServer appleNotifyCation=new AppleNotificationServerBasicImpl(certificatePath, certificatePassword, true);
			pushManager.initializeConnection(appleNotifyCation);
			//尝试多少次
			pushManager.setRetryAttempts(10);
			log.info("发送信息 ..."+payLoad.toString());
			PushedNotification sendNotification = pushManager.sendNotification(device, payLoad,true); // 推送消息
			
			List<Device> arg1=new ArrayList<Device>();
			pushManager.sendNotifications(payLoad, arg1);
			//TODO 需要联调测试确认
			int identifier = sendNotification.getIdentifier();
			System.out.println("已经推送的数量.. "+identifier);
			
			List<PushedNotification> arg0=new ArrayList<PushedNotification>();
			arg0.add(sendNotification);
			List<PushedNotification> findFailedNotifications = PushedNotification.findFailedNotifications(arg0);
			if(findFailedNotifications==null||findFailedNotifications.size()<1){
				System.out.println("失败...");
			}
			String sss = sendNotification.getLatestTransmissionAttempt();
			log.info("第几次尝试. .."+sss);
			boolean successful = sendNotification.isSuccessful();
			log.info("推送是否成功  "+successful);
			boolean transmissionCompleted = sendNotification.isTransmissionCompleted();
			log.info("传送是否完成... "+ transmissionCompleted);
			if(!transmissionCompleted){
				log.info("向苹果推送失败...");
				errorcode = "B000P";
				msg = "向苹果推送失败...";
			}
			if(successful){
				log.info("用户存在");
				errorcode = "00000";
				msg = "成功";
			}else{
				//不存在的可以用这个判断
				log.info("用户不存在");
				errorcode = "B000P";
				msg = "用户不存在...";
			}
			
			ResponsePacket response = sendNotification.getResponse();
			if(response!=null){
				int status = response.getStatus();
				System.out.println("响应状态 ... "+status);
				String message = response.getMessage();
				System.out.println("消息内容 "+message);
			}
			pushManager.stopConnection();
		} catch (Exception e) {
			log.error("向苹果请求推送异常 " + e.getMessage(), e);
			errorcode = "B000P";
			msg = "向苹果apns请求推送异常";
			// P代表apple
		}
		res.put("errorcode", errorcode);
		res.put("msg", msg);
		return res.toString();
		
	}

	/**
	 * TODO 推送给多个,暂不需要
	 * @return
	 */
	public String pushToIphoneList(){
		String deviceToken = "426cf19e714ad88614404ef59f2266fd678ba36678ba377e65a262a4ad2edae6";
        String alert = "我的push测试";//push的内容
        int badge = 1;//图标小红圈的数值
        String sound = "Candylan.aiff";//铃音

        List<String> tokens = new ArrayList<String>();
        tokens.add(deviceToken);
        String certificatePath = PropertyFactory
				.getProperty("CERTIFICATEADDR");
        String certificatePassword = "123456";//此处注意导出的证书密码不能为空因为空密码会报错
        boolean sendCount = true;
        try
        {
            PushNotificationPayload payLoad = new PushNotificationPayload();
            payLoad.addAlert(alert); // 消息内容
            payLoad.addBadge(badge); // iphone应用图标上小红圈上的数值
            payLoad.addSound(sound);//铃音
            PushNotificationManager pushManager = new PushNotificationManager();
            //true：表示的是产品发布推送服务 false：表示的是产品测试推送服务
            pushManager.initializeConnection(new AppleNotificationServerBasicImpl(certificatePath, certificatePassword, true));
            List<PushedNotification> notifications = new ArrayList<PushedNotification>();
            // 发送push消息
            if (sendCount)
            {
                Device device = new BasicDevice();
                device.setToken(tokens.get(0));
                PushedNotification notification = pushManager.sendNotification(device, payLoad, true);
                notifications.add(notification);
            }
            else
            {
                List<Device> device = new ArrayList<Device>();
                for (String token : tokens)
                {
                    device.add(new BasicDevice(token));
                }
                notifications = pushManager.sendNotifications(payLoad, device);
            }
            List<PushedNotification> failedNotifications = PushedNotification.findFailedNotifications(notifications);
            List<PushedNotification> successfulNotifications = PushedNotification.findSuccessfulNotifications(notifications);
            int failed = failedNotifications.size();
            System.out.println("失败了 ...." +failed);
            int successful = successfulNotifications.size();
            System.out.println("成功了 ...." +successful);
            pushManager.stopConnection();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
	}
}
