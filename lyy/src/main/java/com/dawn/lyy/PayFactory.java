package com.dawn.lyy;

import android.content.Context;
import android.content.Intent;

/**
 * 支付工厂类
 */
public class PayFactory {
    private Context mContext;
    //单例模式
    private static PayFactory instance;

    private PayFactory(Context context) {
        mContext = context;
    }

    public static PayFactory getInstance(Context context) {
        if (instance == null) {
            synchronized (PayFactory.class) {
                if (instance == null) {
                    instance = new PayFactory(context);
                }
            }
        }
        return instance;
    }

    /**
     * 设置支付监听
     */
    public void setListener(OnPayListener listener) {
        PayConstant.mListener = listener;
    }

    /**
     * 获取支付监听
     */
    public OnPayListener getListener(){
        return PayConstant.mListener;
    }

    /**
     * 初始化支付数据
     * @param payUrl 乐摇摇socket域名
     * @param payPort 乐摇摇端口号
     * @param appId 乐摇摇登录的APPID
     * @param appSecret 乐摇摇登录的秘钥
     */
    public void initPayData(String payUrl, int payPort, String appId, String appSecret){
        PayConstant.payUrl = payUrl;
        PayConstant.payPort = payPort;
        PayConstant.appId = appId;
        PayConstant.appSecret = appSecret;
    }

    /**
     * 开启服务
     */
    public void startService(String deviceId, int price, int inventory){
        PayConstant.deviceId = deviceId;
        PayConstant.price = price;
        PayConstant.inventory = inventory;
        Intent netIntent = new Intent(mContext, PayService.class);
        mContext.startService(netIntent);
    }

    /**
     * 发送获取支付二维码的请求
     * @param key 支付流水，唯一标识
     * @param price 支付金额
     */
    public void sendGetPayQrCode(String key, int price){
        Intent intent = new Intent(PayConstant.RECEIVER_PAY);
        intent.putExtra("command", "get_pay_qr_code");
        intent.putExtra("key", key);
        intent.putExtra("price", price);
        mContext.sendBroadcast(intent);
    }

    /**
     * 发送游戏结果的指令
     * @param key 支付流水，唯一标识
     * @param status 游戏结果
     */
    public void sendGameResult(String key, boolean status){
        Intent intent = new Intent(PayConstant.RECEIVER_PAY);
        intent.putExtra("command", "game_status");
        intent.putExtra("key", key);
        intent.putExtra("status", status);
        mContext.sendBroadcast(intent);
    }
}
