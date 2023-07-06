package com.dawn.lyy;

public class PayConstant {
    public static final String RECEIVER_PAY = "receiver_pay_lyy";//支付服务的广播
    public static String payUrl = "";//乐摇摇socket域名
    public static int payPort ;//乐摇摇端口号
    public static String appId = "";//乐摇摇登录的APPID
    public static String appSecret = "";//乐摇摇登录的秘钥

    public static String deviceId;//设备ID
    public static int price;//支付金额
    public static int inventory;//库存

    public static OnPayListener mListener;
}
