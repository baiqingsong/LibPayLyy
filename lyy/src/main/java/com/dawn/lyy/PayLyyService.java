package com.dawn.lyy;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dawn.library.LCipherUtil;
import com.dawn.socket.LSocketUtil;
import com.google.gson.GsonBuilder;


public class PayLyyService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private PayReceiver mReceiver;//支付接收广播

    /**
     * 注册广播
     */
    private void registerReceiver() {
        mReceiver = new PayReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PayConstant.RECEIVER_PAY);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null)
            unregisterReceiver(mReceiver);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("dawn","支付服务开启");
        registerReceiver();//广播注册
        payConnect();
    }

    private final static int h_pay_connect_time_out = 0x101;//socket连接失败或者超时
    private final static int h_pay_setting_time_out = 0x102;//支付数据设置超时
    private final static int h_heart = 0x104;//心跳
    private final static int h_connect_success = 0x105;//登录成功后操作
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case h_pay_connect_time_out://连接超时
                case h_pay_setting_time_out://数据设置超时
                    payConnect();//30秒超时重连,或者设置超时重连
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.onPayConnectStatus(false);
                    break;
                case h_heart://心跳
                    getCycleHeart();
                    break;
                case h_connect_success://登录后操作
                    mHandler.removeMessages(h_pay_setting_time_out);//取消设置超时
                    mHandler.sendEmptyMessageDelayed(h_pay_setting_time_out, 30 * 1000);//数据设置超时重连
                    sendLYYCommand(getLoginRandom());//连接成功,发送获取登录的随机字符串
                    break;
            }
        }
    };

    private LSocketUtil mSocketUtil;//乐摇摇的socket连接
    private final static int giftAmount = 500;//单轨道总数量
    private boolean isLogin = false;//是否上传数据都成功
    private int heartFailNum = 0;//心跳检测失败次数

    /**
     * 乐摇摇支付连接
     */
    private void payConnect() {
        if(mSocketUtil != null)
            mSocketUtil.disConnect();//防止产生多个连接，先断开之前的连接
        mSocketUtil = new LSocketUtil();//socket工具类
        mSocketUtil.connect(PayConstant.payUrl, PayConstant.payPort, new LSocketUtil.SocketListener() {
            @Override
            public void connectSuccess() {//乐摇摇连接成功
                Log.i("dawn","支付socket连接成功");
                heartFailNum = 0;//心跳检测失败次数
                mHandler.removeMessages(h_pay_connect_time_out);//取消连接超时
                mHandler.sendEmptyMessageDelayed(h_connect_success, 3000);//5秒后请求数据
            }

            @Override
            public void receiverMsg(String msg) {
                resolveReceiverData(msg);//接收信息
            }

            @Override
            public void connectFail() {
                Log.e("dawn","支付socket连接失败");
                isLogin = false;//登录失败
            }
        });
        mHandler.removeMessages(h_pay_connect_time_out);
        mHandler.sendEmptyMessageDelayed(h_pay_connect_time_out, 30 * 1000);//连接失败了重新连接

    }

    /**
     * 重新连接
     */
    private void restartConnect(){
        mHandler.removeMessages(h_heart);
        isLogin = false;
        if(PayConstant.mListener != null)
            PayConstant.mListener.onPayConnectStatus(false);
        payConnect();//重新创建连接
    }

    /**
     * socket发送信息
     *
     * @param msg 信息内容
     */
    private void sendMsg(final String msg) {
        if (mSocketUtil != null && !TextUtils.isEmpty(msg)) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    mSocketUtil.sendMsg(msg + "\r\n");
                }
            }.start();
        }
    }

    /**
     * 循环心跳
     * 30秒循环发送心跳
     */
    private void getCycleHeart() {
//        LLog.i("pay send heart");
        heartFailNum ++;
        if(heartFailNum > 5){//失败超过3次
            restartConnect();
        }else{
            LyySocketReqModel lyySocketReqModel = new LyySocketReqModel("heartbeat");
            sendMsg(new GsonBuilder().create().toJson(lyySocketReqModel));
            mHandler.removeMessages(h_heart);
            mHandler.sendEmptyMessageDelayed(h_heart, 10 * 1000);//30秒后发送心跳
        }
    }

    /**
     * 发送乐摇摇指令
     * @param lyySocketModel 发送指令实体类
     */
    private void sendLYYCommand(LyySocketModel lyySocketModel){
        try {
            String data = LCipherUtil.encryptAES(new GsonBuilder().create().toJson(lyySocketModel), PayConstant.appSecret);
//            Log.i("dawn", "send data " + data);
            sendMsg(new GsonBuilder().create().toJson(new LyySocketReqModel(PayConstant.appId, data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理支付的返回数据
     *
     * @param data 返回数据
     */
    private void resolveReceiverData(String data) {
        if (TextUtils.isEmpty(data)) return;
        try {
            LyySocketReqModel lyySocketReqModel = new GsonBuilder().create().fromJson(data, LyySocketReqModel.class);
            if ("heartbeat".equals(lyySocketReqModel.getData())) {//单独判断心跳
                heartFailNum = 0;//收到心跳，重新计算心跳连接次数
                return;
            }
            String dataStr = LCipherUtil.decryptAES(lyySocketReqModel.getData(), PayConstant.appSecret);
            LyySocketModel lyySocketModel = new GsonBuilder().create().fromJson(dataStr, LyySocketModel.class);
            String a = lyySocketModel.getA();//乐摇摇的操作指令
            if (TextUtils.isEmpty(a)) return;
            a = a.trim();
            Log.i("dawn","get msg " + dataStr);
            switch (a) {
                case "rsr"://请求的登录的随机字符串的返回结果//{"a":"rsr","p":{"d":"随机字符串"}}
                    String randomStr = lyySocketModel.getP().getD();//获取登录随机字段
                    sendLYYCommand(getLogin(randomStr));//发送登录指令
                    break;
                case "lr"://请求登录的返回结果//{"a":"lr","p":{"d":"1","q":"绑定二维码内容"}}
                    Log.i("dawn","乐摇摇登录成功 " + dataStr);
                    isLogin = true;//登录成功标志
                    String d = lyySocketModel.getP().getD();//设备是否绑定,0是已绑定，1是未绑定，1的时候q才有内容
                    String v = lyySocketModel.getP().getV();//支付序列号
                    if (!TextUtils.isEmpty(v)){//支付序列号不为空，保存支付序列号
                        if(PayConstant.mListener != null)
                            PayConstant.mListener.getPayId(v);
                    }
                    if ("1".equals(d)) {//未绑定
                        if(PayConstant.mListener != null)
                            PayConstant.mListener.getBindQrCode(lyySocketModel.getP().getQ());//获取绑定二维码
                        Log.i("dawn","乐摇摇未绑定");
                        mHandler.removeMessages(h_pay_setting_time_out);//取消设置超时
                        if(PayConstant.mListener != null)
                            PayConstant.mListener.onPayConnectStatus(true);
                        getCycleHeart();//未绑定，只发送心跳，不进行后面操作
                    } else {
                        sendLYYCommand(getParamSetting());//发送参数设置
                    }
                    break;
                case "mbpr"://新的货道设置成功
                    Log.i("dawn","乐摇摇数据上传成功");
                    mHandler.removeMessages(h_pay_setting_time_out);//取消设置超时
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.onPayConnectStatus(true);
                    sendLYYCommand(uploadProductMsg());//上传商品信息
                    break;
                case "rgr"://上传商品信息的返回
                    Log.i("dawn","上传商品信息成功");
                    sendLYYCommand(uploadParamProduct(lyySocketModel.getP() == null ? "": lyySocketModel.getP().getSi()));//匹配货道和商品信息
                    break;
                case "rpr"://上传商品和仓位的对应关系的返回
                    Log.i("dawn","上传商品和仓道关系成功");
                    getCycleHeart();//所有操作结束，发送心跳
                    break;
                case "b"://绑定成功//{"a":"b","k":"123456"}
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.onPayBindSuccess();
                    sendLYYCommand(getBindSuccess());//发送绑定成功回复指令
                    sendLYYCommand(getParamSetting());//数据设置上传
                    break;
                case "ub"://解绑成功//{"a":"ub","k":"123456"}
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.onPayUnbindSuccess();
                    sendLYYCommand(getUnbindSuccess());//发送解除绑定成功回复指令
                    sendLYYCommand(getBindQrCode());//发送获取绑定二维码指令
                    break;
                case "bqr":// 获取绑定二维码
                    //{"a":"bqr","p":{"d":"绑定二维码内容"}}
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.getBindQrCode(lyySocketModel.getP().getD());

                    break;
                case "pqr"://请求支付二维码返回的结果
                    //{"a":"pqr","p":{"d":"支付二维码内容"},"k":"123456"}
                    String key = lyySocketModel.getK();
                    String qr_code = lyySocketModel.getP().getD();
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.getPayQrCode(key,qr_code);
                    break;
                case "pr"://支付成功
                    //{"a":"pr","p":{"uid":"付款用户id"},"k":"123456"}
                    key = lyySocketModel.getK();
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.onPaySuccess(key);
                    sendLYYCommand(getPaySuccess(key));//支付成功的响应
                    break;
                case "srr"://游戏结果的响应
                    //{"a":"srr","k":"123456"}
                    if (PayConstant.mListener != null)
                        PayConstant.mListener.onPaySuccess(lyySocketModel.getK());
                    break;
                case "gr"://退款的返回信息
                    //{"a":"gr","p":{"i":"3","n":"口红","pi":"URL","g":"10","p":"200","co":"100","c":"20","cu":"4"},"k":"123456"}
                    //i 仓位编号，n 商品名称，pi 商品图片url，g 游戏价格（单位分），p 支付价格（单位分），co 成本价格（单位分），c 概率，cu 库存
                    break;
//                case "spi"://服务器设置存货和支付价格
                    //{"a":"spi","p":{"i":"3","n":"口红","pi":"URL","g":"10","p":"200","c":"20","cu":"4"},"k":"123456"}
                case "bspi"://服务端批量设置
                    //{"a":"bspi","p":{"i":["3","4",…],"n":"口红", "pi":"URL","g":"10","p":"200","c":"20","cu":"4","cn":"仓位名","co":"100", "ce":"1","ca":"50"},"k":"082014568475"}
//                case "ip"://下发仓位设置
                    //{"a":"ip","p":{"i":["1"],"ci":"1001","si":"1000001","n":"A01", "p":"2500","s":"50","gn":"可乐","gi":" https://img.leyaoyao.com","gp":"1000"},"k":"3e09f9"}

                    //i 仓位编号，n 商品名称， pi 商品图片URL，g 游戏价格（单位分），p 支付价格（单位分），co 成本价格（单位分），c 概率，cu 库存
                    int price = Integer.parseInt(lyySocketModel.getP().getP());
                    key = lyySocketModel.getK();
                    sendLYYCommand(getSettingResult(key));
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.getPayPrice(price);
                    break;
                case "ras"://远程上分
                    //{"a":"ras","p":{"i":"10","t":"0"},k":"123456"}
                    key = lyySocketModel.getK();
                    sendLYYCommand(getRemoteResult(key));//发送远程上分的回复结果
                    if(PayConstant.mListener != null)
                        PayConstant.mListener.onRemotePaySuccess();
                    break;
                case "eqb"://自定义设置查询
                    getCustomParam(lyySocketModel.getK(), lyySocketModel.getP() != null ? lyySocketModel.getP().getF() : "");
                    break;
                case "esb"://自定义参数设置
                    getCustomParamSetting(lyySocketModel.getK(), lyySocketModel.getP() != null ? lyySocketModel.getP().getD() : "");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取登录的随机参数
     * {"a":"rs","p":{"u":"设备UUID"}}
     */
    private LyySocketModel getLoginRandom() {
        Log.i("dawn","乐摇摇支付获取登录随机数");
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("rs");//指令
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setU(PayConstant.deviceId);//设备UUID
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 支付登录
     *
     * @param randomStr 生成随机数
     *                  {"a":"l","p":{"d":"随机字符串","u":"设备UUID","v":"SDK版本号"}}
     */
    private LyySocketModel getLogin(String randomStr) {
        if (TextUtils.isEmpty(randomStr)) return null;
        Log.i("dawn","乐摇摇支付登录");
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("l");//指令
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setD(randomStr);//随机数
        lyySocketModelP.setU(PayConstant.deviceId);//设备UUID
        lyySocketModelP.setV("2.2.1");//sdk版本号
        lyySocketModelP.setF("4738");//登陆标识，乐摇摇后台查看
        lyySocketModelP.setT("SHJ");
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 新的上传仓位信息
     * {"a":"mbp","p":{"i":"3","g":"10","p":"200",
     * "c":"20","ca":"8","co":"12","ce":"1","po":"2,1"},"k":"hc628"}
     */
    private LyySocketModel getParamSetting(){
        Log.i("dawn","乐摇摇支付数据上传");
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("mbp");//指令
        lyySocketModel.setK(System.currentTimeMillis() + "");
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setI("1");//仓位编号
        lyySocketModelP.setG("1");//商品价格
        lyySocketModelP.setP(PayConstant.price + "");//支付价格
        lyySocketModelP.setC("100");//出奖概率
        lyySocketModelP.setCa("" + giftAmount);//容量
//        lyySocketModelP.setCu("" + Constant.giftInventory);//库存
        lyySocketModelP.setCu(PayConstant.inventory + "");//库存
        lyySocketModel.setP(lyySocketModelP);
//        Log.i("dawn", "param " + lyySocketModel.toString());
        return lyySocketModel;
    }

    /**
     * 上传商品信息接口
     * {"a":"rg","p":{"ci":"1001","o":"put","si":"1000001","n":"可乐",
     * "i":"https://img.leyaoyao.com","p":"1202"},"k":"3e8sf9"}
     */
    private LyySocketModel uploadProductMsg(){
        Log.i("dawn","乐摇摇支付上传商品信息数据");
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("rg");//指令
        lyySocketModel.setK(System.currentTimeMillis() + "");
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setCi("1001");//产品编号
        lyySocketModelP.setO("put");
        lyySocketModelP.setN("照片底片");//商品名称
        lyySocketModelP.setI("");//图片
        lyySocketModelP.setP("1");//成本
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 上传仓道和商品对应的关系
     * {"a":"rp","p":{"o":"put","i":"1","si":"1000001","n":"A01",
     * "p":"2500","s":"50"},"k":"3e99f9"}
     */
    private LyySocketModel uploadParamProduct(String productId){
        Log.i("dawn","乐摇摇支付上传商品和仓道对应关系数据");
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("rp");//指令
        lyySocketModel.setK(System.currentTimeMillis() + "");
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setO("put");
        lyySocketModelP.setI("1");//仓位
        lyySocketModelP.setSi(productId);//礼品在服务器上id
        lyySocketModelP.setN("A01");//货道名称
        lyySocketModelP.setP(PayConstant.price + "");//支付价格
        lyySocketModelP.setS("" + PayConstant.inventory);//剩余库存
//        lyySocketModelP.setS("" + Constant.giftInventory);//剩余库存
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 设备绑定成功的响应
     * {"a":"br","k":"123456"}
     */
    private LyySocketModel getBindSuccess() {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("br");//指令
        lyySocketModel.setK(System.currentTimeMillis() + "");
        return lyySocketModel;
    }

    /**
     * 设备解绑成功的响应
     * {"a":"ubr","k":"123456"}
     */
    private LyySocketModel getUnbindSuccess() {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("ubr");//指令
        lyySocketModel.setK(System.currentTimeMillis() + "");
        return lyySocketModel;
    }

    /**
     * 获取绑定二维码
     * {"a":"bq"}
     */
    private LyySocketModel getBindQrCode() {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("bq");//指令
        return lyySocketModel;
    }

    /**
     * 获取支付二维码
     *              {"a":"pq","p":{"d":"1,1,100;2,3,600;3,1,300","t":"0"},"k":"123456"}
     *              仓位+数量+总金额(单位分)
     */
    private LyySocketModel getPayQrCode(String payKey, int price) {
        if(TextUtils.isEmpty(payKey))
            payKey = System.currentTimeMillis() + "";
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("pq");//指令
        lyySocketModel.setK(payKey);//流水号
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setD("1,1," + price);//仓道，数量，总金额
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 支付成功的响应
     *
     * @param key 支付的唯一标识
     *            {"a":"prr","k":"123456"}
     */
    private LyySocketModel getPaySuccess(String key) {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("prr");//指令
        lyySocketModel.setK(key);
        return lyySocketModel;
    }

    /**
     * 处理游戏结果
     */
    private void operateGameResult(String key, boolean status ) {
        sendLYYCommand(getGameResult(key, status));//发送结果指令
        sendLYYCommand(getRemainder(key));
    }

    /**
     * 游戏结果的响应
     *
     * @param status 游戏结果
     * @param key    支付订单
     */
    private LyySocketModel getGameResult(String key, boolean status) {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("sr");//指令
        lyySocketModel.setK(key);
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setD(status ? "0" : "1");
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 退款
     */
    private LyySocketModel getRefundResult(String key, String pay) {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("sr");//指令
        lyySocketModel.setK(key);
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setD("1");
        lyySocketModelP.setF("1,1," + pay);
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 上传退礼数据
     *
     * @param key 支付唯一标识
     *                  {"a":"g","p":{"i":"3","a":"2","t":"5","o":"GAME"},"k":"123456"}
     *                  i 仓位编号，a 退礼增量，t 退礼总量，o 礼品来源GAME:游戏 BUY: 购买
     */
    private LyySocketModel getRemainder(String key) {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("g");
        lyySocketModel.setK(key);
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setI("1");
        lyySocketModelP.setA("1");//本次退礼数量
        lyySocketModelP.setT((giftAmount - PayConstant.inventory) + "");//退礼总数量
        lyySocketModelP.setO("BUY");
        lyySocketModel.setP(lyySocketModelP);
        return lyySocketModel;
    }

    /**
     * 服务器设置的响应
     *
     * @param key 设置的唯一标识
     */
    private LyySocketModel getSettingResult(String key) {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("ipr");//指令
        lyySocketModel.setK(key);
        return lyySocketModel;
    }




    /**
     * 上传单个格子的参数
     * {"a":"pi","p":{"i":"3","n":"口红","g":"10","p":"200","c":"20","ca":"8","cu":"4"},"k":"123456"}
     * i 仓位编号，n 商品名称，g 游戏价格（单位分），p 支付价格（单位分），co 成本价格（单位分），c 概率，ca（容量），cu（库存）
     */
    private void getParamItemSetting(int index, String name, int price, int sum, int inventory) {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("pi");//指令
        lyySocketModel.setK(System.currentTimeMillis() + "");
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setI(index + "");
        lyySocketModelP.setN(TextUtils.isEmpty(name) ? "" : name);
        lyySocketModelP.setG(price + "");
        lyySocketModelP.setP(price + "");
        lyySocketModelP.setC("");
        lyySocketModelP.setCa("" + sum);
        lyySocketModelP.setCu("" + inventory);
        lyySocketModel.setP(lyySocketModelP);
        try {
            String data = LCipherUtil.encryptAES(new GsonBuilder().create().toJson(lyySocketModel), PayConstant.appSecret);
            sendMsg(new GsonBuilder().create().toJson(new LyySocketReqModel(data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量设置的回复
     *
     * @param key 设置的唯一标识
     */
    private void getAllSettingResult(String key) {
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("bspir");
        lyySocketModel.setK(key);
        try {
            String data = LCipherUtil.encryptAES(new GsonBuilder().create().toJson(lyySocketModel), PayConstant.appSecret);
            sendMsg(new GsonBuilder().create().toJson(new LyySocketReqModel(data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送远程上分的回复结果
     * {"a":"rasr","p":{"i":"10","t":"0"},"k":"123456"}
     */
    private LyySocketModel getRemoteResult(String key){
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("rasr");//指令
        lyySocketModel.setK(key);
        return lyySocketModel;
    }

    /**
     * 发送自定参数查询的回复
     */
    private void getCustomParam(String key, String f){
//        boolean give_activity = SharedPreferencesUtil.getBuyOneFreeOne(this);
        boolean give_activity = false;
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("eqbr");
        lyySocketModel.setK(key);
        LyySocketModel.LyySocketModelP lyySocketModelP = new LyySocketModel().new LyySocketModelP();
        lyySocketModelP.setF(f);
        lyySocketModelP.setD(give_activity ? "0100" : "0000");
        lyySocketModel.setP(lyySocketModelP);
        try {
            String data = LCipherUtil.encryptAES(new GsonBuilder().create().toJson(lyySocketModel), PayConstant.appSecret);
            sendMsg(new GsonBuilder().create().toJson(new LyySocketReqModel(data)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送自定义参数设置的回复
     * @param d 设置内容
     */
    private void getCustomParamSetting(String key, String d){
        if(TextUtils.isEmpty(d) || d.length() < 4)
            return;
//        if("01".equals(d.substring(2,4)))
//            SharedPreferencesUtil.setBuyOneFreeOne(this, true);
//        else
//            SharedPreferencesUtil.setBuyOneFreeOne(this, false);
//        if("01".equals(d.substring(4,6))){
//            SharedPreferencesUtil.getInstance(PayService.this).setPassword(PayService.this, Const.DEFULT_PASSWORD);
//        }
        LyySocketModel lyySocketModel = new LyySocketModel();
        lyySocketModel.setA("esbr");
        lyySocketModel.setK(key);
        try {
            String data = LCipherUtil.encryptAES(new GsonBuilder().create().toJson(lyySocketModel), PayConstant.appSecret);
            sendMsg(new GsonBuilder().create().toJson(new LyySocketReqModel(data)));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 支付广播
     */
    private class PayReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String command = intent.getStringExtra("command");
            if (TextUtils.isEmpty(command)) return;
            switch (command) {
                case "get_pay_qr_code"://获取支付二维码
                    Log.i("dawn","广播收到页面请求支付二维码");
                    String key = intent.getStringExtra("key");
                    int price = intent.getIntExtra("price", PayConstant.price);//获取价格
                    if (isLogin) {
                        sendLYYCommand(getPayQrCode(key, price));//获取支付二维码
                    } else {
                        Log.i("dawn","获取支付二维码中没有登录成功");
                        if(PayConstant.mListener != null)
                            PayConstant.mListener.getPayQrCode(key, null);
                    }
                    break;
                case "game_status"://游戏结果
                    Log.i("dawn", "广播接收到页面游戏结果");
                    key = intent.getStringExtra("key");
                    boolean status = intent.getBooleanExtra("status", false);
                    operateGameResult(key, status);//处理游戏结果
                    break;

                case "update_status"://游戏结果
                    sendLYYCommand(getParamSetting());//发送参数设置
                    break;

                case "refund_status"://退款
                    key = intent.getStringExtra("key");
                    String pay = intent.getStringExtra("pay");
                    sendLYYCommand(getRefundResult(key, pay));
                    break;
            }
        }
    }
}
