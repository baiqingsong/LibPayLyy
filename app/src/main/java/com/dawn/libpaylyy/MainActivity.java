package com.dawn.libpaylyy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PayLyyFactory.getInstance().payInit(new OnCustomLyyPayListener() {
            @Override
            public void onPayConnectStatus(boolean status) {
                Log.i("dawn", "pay connect status " + status);
            }

            @Override
            public void getPayId(String payId) {
                Log.i("dawn", "pay id " + payId);
            }

            @Override
            public void getBindQrCode(String qrCode) {
                Log.i("dawn", "pay bind code " + qrCode);
            }

            @Override
            public void onPayBindSuccess() {
                Log.i("dawn", "pay bind success");
            }

            @Override
            public void onPayUnbindSuccess() {
                Log.i("dawn", "pay unbind success");
            }

            @Override
            public void getPayQrCode(String key, String qrCode) {
                Log.i("dawn", "get pay qr code " + key + ", " + qrCode);
            }

            @Override
            public void onPaySuccess(String key) {
                Log.i("dawn", "pay success");
            }

            @Override
            public void getPayPrice(int price) {
                Log.i("dawn", "get pay price " + price);
            }

            @Override
            public void onRemotePaySuccess() {
                Log.i("dawn", "remote pay success");
            }
        });
    }
}