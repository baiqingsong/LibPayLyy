package com.dawn.lyy;

import java.text.DecimalFormat;

/**
 * 字符串工具类
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class LStringUtil {

    /**
     * 将null转化成""
     * @param str 处理的字符串
     *
     * @return String 处理后的字符串
     */
    public static String parseEmpty(String str) {
        if (str == null || "null".equals(str.trim())) {
            str = "";
        }
        return str.trim();
    }

    /**
     * 是否是空字符串
     * @param str 判断的字符串
     *
     * @return boolean 是否是空字符串
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 字符串长度
     * @param str 需要判断的字符串
     *
     * @return int 字符串长度
     */
    public static int strLength(String str) {
        int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        if (!isEmpty(str)) {
            for (int i = 0; i < str.length(); i++) {
                String temp = str.substring(i, i + 1);
                if (temp.matches(chinese)) {
                    valueLength += 2;
                } else {
                    valueLength += 1;
                }
            }
        }
        return valueLength;
    }

    /**
     * 是否是中文
     * @param str 判断的字符串
     *
     * @return boolean 是否是中文
     */
    public static Boolean isChinese(String str) {
        boolean isChinese = true;
        String chinese = "[\u0391-\uFFE5]";
        if (!isEmpty(str)) {
            for (int i = 0; i < str.length(); i++) {
                String temp = str.substring(i, i + 1);
                isChinese = temp.matches(chinese);
            }
        }
        return isChinese;
    }

    /**
     * 是否包含中文
     * @param str 判断的字符串
     *
     * @return boolean 是否包含中文
     */
    public static Boolean isContainChinese(String str) {
        boolean isChinese = false;
        String chinese = "[\u0391-\uFFE5]";
        if (!isEmpty(str)) {
            for (int i = 0; i < str.length(); i++) {
                String temp = str.substring(i, i + 1);
                isChinese = temp.matches(chinese);
            }
        }
        return isChinese;
    }

    /**
     * 指定小数输出
     * @param s 处理的小数
     * @param format #.## 保留两位小数，可能少于两位小数。比实际位数多，不变。比实际位数少，整数不变东，小数部分，四舍五入
     *               0.00 保留两位小数，确定两位小数。比实际位数多，不足补0。比实际位数少，整数不改动，小数部分，四舍五入
     *
     * @return String 处理后的字符串
     */
    public static String decimalFormat(double s, String format) {
        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(s);
    }

    /**
     * 字节数组转为字符串
     * @param byteArray 字节数组
     *
     * @return String 字符串
     */
    public static String toHexString(byte[] byteArray) {
        if (byteArray == null || byteArray.length < 1)
            throw new IllegalArgumentException("this byteArray must not be null or empty");

        final StringBuilder hexString = new StringBuilder();
        for(byte b : byteArray){
            if ((b & 0xFF) < 0x10)
                hexString.append("0");
            hexString.append(Integer.toHexString(0xFF & b));
        }
        return hexString.toString().toUpperCase();
    }

    /**
     * 字符串转为字节数组
     * @param hexString 字符串
     *
     * @return byte[] 字节数组
     */
    public static byte[] toByteArray(String hexString) {
        if (hexString == null)
            throw new IllegalArgumentException("this hexString must not be empty");

        hexString = hexString.toUpperCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {
            // 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xFF);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xFF);
            byteArray[i] = (byte) (high << 4 | low & 0xFF);
            k += 2;
        }
        return byteArray;
    }

    /**
     * 十进制转十六进制，少于两位补位
     * @param hex 十进制
     */
    public static String format(int hex) {
        String hexStr = Integer.toHexString(hex).toUpperCase();
        int len = hexStr.length();
        if (len < 2) {
            hexStr = "0" + hexStr;
        }
        return hexStr;
    }

    /**
     * 取反
     */
    public static String parseHex2Opposite(String str) {
        String hex;
        //十六进制转成二进制
        byte[] er = LStringUtil.toByteArray(str);
        //取反
        byte erBefore[] = new byte[er.length];
        for (int i = 0; i < er.length; i++) {
            erBefore[i] = (byte) ~er[i];
        }
        //二进制转成十六进制
        hex = LStringUtil.toHexString(erBefore);
        // 如果不够校验位的长度，补0,这里用的是两位校验
        hex = (hex.length() < 2 ? "0" + hex : hex);

        return hex;
    }

    /**
     * 异或
     * @param str 需要异或的十六进制字符串
     */
    public static String getXor(String str){
        byte[] bytes = toByteArray(str);
        byte temp = bytes[0];
        for (int i = 1; i <bytes.length; i++) {
            temp ^=bytes[i];
        }
        byte[] data = new byte[1];
        data[0] = temp;
        return toHexString(data);
    }

}
