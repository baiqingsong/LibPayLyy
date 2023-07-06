package com.dawn.lyy;

/**
 * 乐摇摇socket请求实体类
 */
public class LyySocketReqModel {
    private String app;
    private String data;

    public LyySocketReqModel(String app, String data) {
        this.app = app;
        this.data = data;
    }

    public LyySocketReqModel(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }
}
