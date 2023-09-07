package com.dawn.libpaylyy;

import java.io.FileReader;
import java.io.Reader;

public class SystemUtil {

    /**
     * 获取主板序列号
     */
    public static String getDeviceId() {
        String deviceId = null;
        try {//有线的mac
            deviceId = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
            deviceId = deviceId.trim().replace(":", "").replace("-", "") + "00000000";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deviceId;
    }
    /**
     * 根据路径获取文件内容
     * @param fileName
     * @return
     * @throws Exception
     */
    private static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }
    /**
     * 根据流获取内容
     * @param reader
     * @return
     * @throws Exception
     */
    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

}
