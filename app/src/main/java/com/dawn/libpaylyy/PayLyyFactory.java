package com.dawn.libpaylyy;

import android.webkit.JavascriptInterface;

import com.dawn.lyy.OnPayListener;
import com.dawn.lyy.PayFactory;

/**
 * 乐摇摇支付的工厂类
 */
public class PayLyyFactory {
    //单例模式
    private static PayLyyFactory instance = null;

    private PayLyyFactory() {
        payFactory = PayFactory.getInstance(MyApp.getContext());
    }

    public static PayLyyFactory getInstance() {
        if (instance == null) {
            synchronized (PayLyyFactory.class) {
                if (instance == null) {
                    instance = new PayLyyFactory();
                }
            }
        }
        return instance;
    }

    private final PayFactory payFactory;//支付工厂类

    /**支付相关*************************************************************************************/
    //支付初始化
    public void payInit(OnCustomLyyPayListener listener){
        payFactory.initPayData("ehw.leyaoyao.com", 5923, "abada1fe8939ed43", "38f24d77d68b3829ee0705cef979f147")
                .setListener(new OnPayListener() {
                    @Override
                    public void onPayConnectStatus(boolean status) {
                        if(listener != null)
                            listener.onPayConnectStatus(status);
                    }

                    @Override
                    public void getPayId(String payId) {
                        if(listener != null)
                            listener.getPayId(payId);
                    }

                    @Override
                    public void getBindQrCode(String qrCode) {
                        if(listener != null)
                            listener.getBindQrCode(qrCode);
                    }

                    @Override
                    public void onPayBindSuccess() {
                        if(listener != null)
                            listener.onPayBindSuccess();
                    }

                    @Override
                    public void onPayUnbindSuccess() {
                        if(listener != null)
                            listener.onPayUnbindSuccess();
                    }

                    @Override
                    public void getPayQrCode(String key, String qrCode) {
                        if(listener != null)
                            listener.getPayQrCode(key, qrCode);
                    }

                    @Override
                    public void onPaySuccess(String key) {
                        if(listener != null)
                            listener.onPaySuccess(key);
                    }

                    @Override
                    public void getPayPrice(int price) {
                        if(listener != null)
                            listener.getPayPrice(price);
                    }

                    @Override
                    public void onRemotePaySuccess() {
                        if(listener != null)
                            listener.onRemotePaySuccess();
                    }
                }).startService(Constant.deviceId, Constant.price, Constant.giftInventory);
    }

    //支付获取支付二维码
    @JavascriptInterface
    public void payGetQrCode(String key, int price){
        payFactory.sendGetPayQrCode(key, price);
    }
    //支付获取游戏结果
    @JavascriptInterface
    public void payGetGameResult(String key, boolean status){
        payFactory.sendGameResult(key, status);
    }
}
