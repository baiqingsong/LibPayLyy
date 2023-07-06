package com.dawn.lyy;

/**
 * 乐摇摇socket请求的实体类
 */
public class LyySocketModel {
    private String a;//action 协议指令
    private LyySocketModelP p;//parameter 指令参数
    private String k;//key 指令唯一码

    @Override
    public String toString() {
        return "a : " + a + " p : " + (p == null ? null : p.toString()) + " k : " + k;
    }

    public class LyySocketModelP{
        private String error;//错误信息
        private String u;//设备uuid
        private String d;//随机字符串
        private String q;//绑定二维码内容
        private String t;//类型0-支付，1-游戏/每个货道总消耗的商品数量
        private String uid;//付款用户ID
        private Object i;//仓位编号
        private String n;//商品名称
        private String g;//商品价格
        private String p;//支付价格
        private String c;//中奖概率
        private String ca;//容量
        private String cu;//库存，剩余
        private String cn;//货道名称
        private String a;//减少的数量
        private String v;//设备编号
        private String o;//礼品来源类型，GAME：游戏，BUY：购买
        private String f;//登录标识
        private String ci;//产品编号
        private String si;//礼品在服务器上的ID
        private String s;//商品库存

        @Override
        public String toString() {
            return "error : " + error + " u : " + u + " d : " + d + " q : " + q + " t : " + t + " uid : " + uid + " i : " + i + " n : " + n + " g : " + g + " p : " + p + " c : " + c + " ca : " + ca + " cu : " + cu + " v : " + v;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }

        public String getSi() {
            return si;
        }

        public void setSi(String si) {
            this.si = si;
        }

        public String getCi() {
            return ci;
        }

        public void setCi(String ci) {
            this.ci = ci;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getU() {
            return u;
        }

        public void setU(String u) {
            this.u = u;
        }

        public String getD() {
            return d;
        }

        public void setD(String d) {
            this.d = d;
        }

        public String getQ() {
            return q;
        }

        public void setQ(String q) {
            this.q = q;
        }

        public String getT() {
            return t;
        }

        public void setT(String t) {
            this.t = t;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Object getI() {
            return i;
        }

        public void setI(String i) {
            this.i = i;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }

        public String getG() {
            return g;
        }

        public void setG(String g) {
            this.g = g;
        }

        public String getP() {
            return p;
        }

        public void setP(String p) {
            this.p = p;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public String getCa() {
            return ca;
        }

        public void setCa(String ca) {
            this.ca = ca;
        }

        public String getCu() {
            return cu;
        }

        public void setCu(String cu) {
            this.cu = cu;
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getV() {
            return v;
        }

        public void setV(String v) {
            this.v = v;
        }

        public String getO() {
            return o;
        }

        public void setO(String o) {
            this.o = o;
        }

        public String getF() {
            return f;
        }

        public void setF(String f) {
            this.f = f;
        }

        public String getCn() {
            return cn;
        }

        public void setCn(String cn) {
            this.cn = cn;
        }
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public LyySocketModelP getP() {
        return p;
    }

    public void setP(LyySocketModelP p) {
        this.p = p;
    }

    public String getK() {
        return k;
    }

    public void setK(String k) {
        this.k = k;
    }
}
