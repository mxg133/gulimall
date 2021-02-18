package com.atguigu.gulimall.order.utils;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 买家账号：hemoiw9499@sandbox.com
 */

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000117611615";

    // 商户私钥，您的PKCS8格式RSA2私钥
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCYVHwq403yPt7qhMmSFwS9W5xwTT8397bAXeGGHdDrUg/oneBKnnaeB7VJzalgfTix2fH2Yk6Q/ZbFcYyZP96He+/9ng/6TZv9wV5F5B3yWpiHynET+yFI9gBB+APn5/Omx3Beel7qOsfFqnpfZYl1FEAv8GFsq+6lZB7Usen+hct5S6PirZaf8ADkdbJs2SWWW/a/mbSX9nKGx5r3AJi+tHNH9I6rXhbJz36SB5E7fl8IKCSTVgms9YJr2JvcW4GWHQfAGy4t1dtrlvsoSOAWySYwWMxT7tcrApj1jWY5RzE+d8+8aPksYpyu13WCfTGhrOhMCDoYy3Tq1b9J/2BNAgMBAAECggEAVTn3mptAUgeg+r8+c9ETIsqttRtTAoJCqlAcTMhDylVId1JVWgpcvT/8ywUY/dYxsTgKK1qHMJHhGCjWjCty2oSaGmCj+dwQKaJVl27w3laXvmmiaJZ54fNY44/ax5Y7+RTeps16azxl5w7hlpwdkva4jtnD+GQdikWBBmlgTLk7tYKqrU36+IP2Q8cylU1bcv7tEHopRCxR0+jkMqc2eXVd3jR8gTm7IAlo7UNJiZzJO59Fhlqv7bBW7g7dQLm4yT/p5qRsbkxQ7fq3f/b3dllQkgElibylzi9ZF+ISgFOXgF3RP3qzrpxvqwyffuKFgnzyyvoK2cYrgW9QYKY4oQKBgQDLLm96dp4H996XtZQcFNbG/kzifT9k/izU1RVdJ21uGLze3Tvq3giYGsAdN+3WeUhLEfotg1NwSi217t0BODC4xvKNA+eMCuNvyQrnUOF04kjq3jyUtp99UnLfZTxzTimGSzxc7CtmVg1O6pG+xtjGHmBLnjiCAHGz1BVnQbFKGQKBgQC/7e4jdbue5IJwpgAr/LrvFf5AsDH1S9/DGcvSZIqtByPpX7VjbOLoaQBc3zhaV1/JviP1jp2l4zcHHHm4Zn9Z0EHW3pdv4HYi+xJBqjb9hshOEITD5lLoah96LL3Di1/bI3P2JSjyx0B2FwSefSCkwZght4DTlWTIka/1gZS2VQKBgHwKi1BOXkgrER+5YQSLzFuMINAc+rjjW+dLd1F5opsWSygcFFpc1w5VNnggDvli7bm27e8fP69L+gAaJZ56+XO1MZrZiWC7vQIf0KJLhHjhNPb+WinIHDJcRXkQywjx7PPptp5cKFq/qjxFhFjd+LVrEgrYO1NShhftIGv7dQrxAoGAC+YVUG95HUWvbIpuEkwNzsq/Q4Vo43s7uR29TCLdkhKSQGKlnS9f9eaklh1kof0uvBQkqJGZ5nHqtG8ogK0iEhBeNJMCMnZo/IwyOo3x8M/NVUGi6X46/5usredeMGRQogZVpxnwqvR9Y5TTvXumbXBvHRACVZu6btDRZtgMvy0CgYEAtNLeq91ZjMPrw2f+R5j7ox2A9Xmgz0Mm7yRKC4u8oQ57TUWFMgtlc8TklXGdOE+1TI0iXU7HPzYB2RD6pQZsS8CffNTMGUL5JSA1cD6cSeqH10OMSUdNZPuAKQB61BZ+qJssd6p3UkxnedUHsUfxpgAcHsklkaaMqlgkgc2gYlA=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqCj8qLozDuTCgXnLBFH5w64JUba9lqCcSu50984XmouvC76cH/JQWjMhQVrqFX3S7shWufUF5m5Te0OuK+9YhBv48lgkOLWxKOIqBOrMYT0cXyKSj/UWtkSFFwZcHBFXilE/7WLdDuG7ZDSBeiMFSFFZjZbgzS4SD161UDK97/OGU9ZHaw0EYBlazY8ZgVTiLFzT4WKqBqhurgeaahVFC/Xhod7I7PCWLfIjKoxsnSXtbcHk73W7OXovW2ZGPnnRegq0B9PTONCiZBR4cLyxDRjdY3cdFj/+4KdG+wXqnXZ5jn8rmXFbwZHAZGfaNgam/2t61BpezbwriSRvt2NoDQIDAQAB";

    // 服务器【异步通知】页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    public static String notify_url = "http://lta90jctj6.52http.tech/payed/notify";

    // 页面跳转【同步通知】页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    public static String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    //支付超时时间
    private String timeout = "30m";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","//自动收单
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;
    }
}
