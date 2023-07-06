package com.dawn.lyy;

/**
 * 保存支付的相关信息
 */
public class PayMsgModel {
    private int gridIndex;//福袋的位置
    private long time;//支付的时间

    public PayMsgModel(int gridIndex, long time) {
        this.gridIndex = gridIndex;
        this.time = time;
    }

    public int getGridIndex() {
        return gridIndex;
    }

    public void setGridIndex(int gridIndex) {
        this.gridIndex = gridIndex;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
