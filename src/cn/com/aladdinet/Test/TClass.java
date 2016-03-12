package cn.com.aladdinet.Test;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.exceptions.RequestException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.LinkTemplate;

public class TClass {
	static String appId = "b03c5cfef65ed30108f0a3fd82c3f6b4";
    static String appkey = "110000";
    static String master = "a02a76119b20d4e31620d7597a3b4f35";
    static String CID = "873ffc4fec7bfd43d4705639eacb41d0";
    static String host = "http://sdk.open.api.igexin.com/apiex.htm";
     
    public static void main(String[] args) throws Exception {
        IGtPush push = new IGtPush(host, appkey, master);
        System.out.println("1");
        System.out.println("1");
        System.out.println("1");
        
        LinkTemplate template = linkTemplateDemo();
        SingleMessage message = new SingleMessage();
        message.setOffline(true);
        //离线有效时间，单位为毫秒，可选
        message.setOfflineExpireTime(24 * 3600 * 1000);
        message.setData(template);
        message.setPushNetWorkType(0); //可选。判断是否客户端是否wifi环境下推送，1为在WIFI环境下，0为不限制网络环境。
        Target target = new Target();
 
        target.setAppId(appId);
        target.setClientId(CID);
        //用户别名推送，cid和用户别名只能2者选其一
        //String alias = "个";
        //target.setAlias(alias);
        IPushResult ret = null;
        try{
            ret = push.pushMessageToSingle(message, target);
        }catch(RequestException e){
            e.printStackTrace();
            ret = push.pushMessageToSingle(message, target, e.getRequestId());
        }
        if(ret != null){
            System.out.println(ret.getResponse().toString());
        }else{
            System.out.println("服务器响应异常");
    }
}
    public static LinkTemplate linkTemplateDemo() {
        LinkTemplate template = new LinkTemplate();
        // 设置APPID与APPKEY
        template.setAppId(appId);
        template.setAppkey(appkey);
        // 设置通知栏标题与内容
        template.setTitle("请输入通知栏标题");
        template.setText("请输入通知栏内容");
        // 配置通知栏图标
        template.setLogo("icon.png");
        // 配置通知栏网络图标，填写图标URL地址
        template.setLogoUrl("");
        // 设置通知是否响铃，震动，或者可清除
        template.setIsRing(true);
        template.setIsVibrate(true);
        template.setIsClearable(true);
        // 设置打开的网址地址
        template.setUrl("http://www.baidu.com");
        return template;
}
}
