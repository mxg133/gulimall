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
    public static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCltaukx1VM76pgPFCflpLYPjEASkN0qyoWlUlxr+lGvp4q8Yjuh2nduf6PJpjCKNonOvMqigIbMRCv8vhKXBl6Wt2oWC/4EDPbdp9rBy1APxjiE9U+wrWvoHHGU1ztMKktqPnEnoPIsI1Emuz59jYOoXJQz/UJ5ZjQDt//sM8yeE+FE9Gjay9kzQP0U3Aji9r56aiX/GvhEwpMDmQc+HOvBJnPuAxBDpPYCAu6yUPMuD7K65ZJbz/kIh4KUxWowsHA8vDvFjtPLFPLy5rnv1RNZjUH0cRUijriGyCcG1nmfdKgXpDHiyZwnStdKmH1KCv0wXa4w/P6t/fol9XLqVKvAgMBAAECggEAQMPGN/qoPg+VmVM5d4YUEmwMHr7nqluTcYwbTlGgbMPncYVBLWjDWaNt+GjvX0M0MAz8KO4TcKk2im392KIyWv3UkA0Xgm5iti2glOYTK379rK5vs73J4MBmd0rxkMpp9KyMIeqKOw+1xtB+OhueeQ1kbm04qguQNBD8PyTNdj2qAfax7ZDtJ0YajpNI4mvyiYjiBEwSN8IwB42eJN+qkyt1z7OhEZ7FHUZP9x5DQLey2U7Lpcoq9w63oBSxHvkLwjhCC3lJY8tlne2WYsRMXuY3FCg4IzKPeERx8SizgVn20E6R99T5WG5YhdgQhVfi5uHebaw5qhzIOrqNSaXVEQKBgQDc6HokWHJwK03tFFgqMQLTDQX4e2L2y+wkVebMa6OlDVGTPF4CgFF3/1y8qK02xkbxK3F1jrtz9irgbeqewHnS3atVM8zAtNrZZklzdYOGldF/GoSk/XpBapMtilh4nSNGDch4Bw6AEScs3T5IFGEjnRjODiY5p8S2nN8uHnfGiwKBgQDACHlqaCWs2kakVK3mpHg7BcgWZ2rq632c7JA9BHKdyjye4XRgQMpQb+mFvxzYw5HUa08kTS8fIb2a+NB5v8kORyo4xLVkKg1C5MRLdoZAYebizHdbO7C9aVb0Rc5tVFVAF3rUL3TxPOckiXie3wjLTuA8QBZ9CL8PaHATE2gM7QKBgQCLZniNLvMoLWuDS+9G0J7Dp9g4sUQGETD2zO5T2SjvIrRzgQih8291lcNjmd2UTxK9ZXi3glZwGst7zGDdxQAbPJ69qW5vvLDNt2pqXaGd8DS/THCf3QSk+yfc+QxTMHXt4wy6EVnpHunlucdvnMyfuKbKgMtGXsQXqG59n+RdeQKBgAhPVZG5OAgm2e+6ilv0ug8zzNjLWOEZl7H2urcpPhx95+moL/UKWLbqYNyczI7Ex8a9bgtQG73pI99U7RCXsAeiJL2YJZFUEIbYh3YMhvq6QdeGqZOVHazUpiKj8q7ge1/upC4Sb6kQzZ/TIxkc5bCGlUWGMHLwsfPkvRTWPiZxAoGAHh66ktPf8L2CHGQ+5I9orijZ+Rphzgs8AKhs6QDeniuzcx9obOlD5GueDrGtlN7rDK1ZCg/m8NDJUcaQiCx5un3LuZTcAjUgcygSuiMT73YltHSynv49VxF1UrDTAMFEP5MsmuvaY8lQLA/7EpWMv7VM25OpRfwrZj3fkRQ3ygY=";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmUhLMVkQAD3yJzQcQ0d/WyJoiiXYy+mHhiP4yHJvAEkVJCdtUfDU22AZjXOXYhEKnhsgexSoVtnjD1O412XCKG6emKm4NVDu4rv7F+f7uPSphVyUBQP6QLfPVEm/VPif+hOZnkc3/wZbBNBeAzZ6iw8c35nMucKrAN0FtsVI/UrAOSweh/2qr0y9Ua90/eRnk0YnC0nTlhhpjz0JK8FoZtI3wRh6bVUCq95ubiUaQ15vQv/MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqCj8qLozDuTCgXnLBFH5w64JUba9lqCcSu50984XmouvC76cH/JQWjMhQVrqFX3S7shWufUF5m5Te0OuK+9YhBv48lgkOLWxKOIqBOrMYT0cXyKSj/UWtkSFFwZcHBFXilE/7WLdDuG7ZDSBeiMFSFFZjZbgzS4SD161UDK97/OGU9ZHaw0EYBlazY8ZgVTiLFzT4WKqBqhurgeaahVFC/Xhod7I7PCWLfIjKoxsnSXtbcHk73W7OXovW2ZGPnnRegq0B9PTONCiZBR4cLyxDRjdY3cdFj/+4KdG+wXqnXZ5jn8rmXFbwZHAZGfaNgam/2t61BpezbwriSRvt2NoDQIDAQAB";

    // 服务器【异步通知】页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    public static String notify_url = "http://lta90jctj6.52http.tech/alipay.trade.page.pay-JAVA-UTF-8/notify_url.jsp";

    // 页面跳转【同步通知】页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    public static String return_url = "http://lta90jctj6.52http.tech/alipay.trade.page.pay-JAVA-UTF-8/return_url.jsp";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

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

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;
    }
}
