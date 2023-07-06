package com.dawn.lyy;

public interface OnPayListener {
    void onPayConnectStatus(boolean status);//获取支付连接状态
    void getPayId(String payId);//获取支付商户号
    void getBindQrCode(String qrCode);//获取绑定二维码
    void onPayBindSuccess();//绑定成功
    void onPayUnbindSuccess();//解绑成功
    void getPayQrCode(String key, String qrCode);//获取支付二维码
    void onPaySuccess(String key);//支付成功
    void getPayPrice(int price);//获取支付金额
    void onRemotePaySuccess();//远程支付成功

}
