package com.dawn.lyy;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 文件工具类
 */
@SuppressWarnings("unused")
public class LFileUtil {
    private final static String TAG = LFileUtil.class.getSimpleName();
    /**
     * 关闭io流
     */
    @SuppressWarnings("WeakerAccess")
    public static void closeIO(Closeable... closeables) {
        if (null == closeables || closeables.length <= 0) {
            return;
        }
        for (Closeable cb : closeables) {
            try {
                if (null == cb) {
                    continue;
                }
                cb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件
     * @param filename 文件名称
     */
    @SuppressWarnings("WeakerAccess")
    public static void deleteFile(String filename) {
        new File(filename).delete();
    }

    /**
     * 删除文件夹中的所有文件
     * @param directory 文件夹
     */
    @SuppressWarnings("WeakerAccess")
    public static void deleteFileByDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                file.delete();
            }
        }
    }

    /**
     * 文件是否存在
     * @param filePath 文件路径
     */
    public static boolean isFileExist(String filePath) {
        return new File(filePath).exists();
    }

    /**
     * 将字符串写入到文件中
     * @param filename 文件名称
     * @param content 上下文
     * @param append 是否在文件后继续写
     */
    public static boolean writeFile(String filename, String content, boolean append) {
        boolean isSuccess = false;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(filename, append));
            bufferedWriter.write(content);
            isSuccess = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(bufferedWriter);
        }
        return isSuccess;
    }

    /**
     * 从文件中读取字符串
     * @param filename 文件名称
     */
    public static String readFile(String filename) {
        File file = new File(filename);
        BufferedReader bufferedReader = null;
        String str = null;
        try {
            if (file.exists()) {
                bufferedReader = new BufferedReader(new FileReader(filename));
                str = bufferedReader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(bufferedReader);
        }
        return str;
    }

    /**
     * 从文件中读取字符串（可设置编码）
     * @param file 文件
     * @param charsetName 编码
     */
    public static StringBuilder readFile(File file, String charsetName) {
        StringBuilder fileContent = new StringBuilder();
        if (file == null || !file.isFile()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            closeIO(reader);
        }
    }

    /**
     * 复制文件
     * @param in 输入流
     * @param out 输出流
     */
    public static void copyFile(InputStream in, OutputStream out) {
        try {
            byte[] b = new byte[2 * 1024 * 1024]; //2M memory
            int len;
            while ((len = in.read(b)) > 0) {
                out.write(b, 0, len);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(in, out);
        }
    }

    /**
     * 快速复制
     * @param in 输入流
     * @param out 输出流
     */
    public static void copyFileFast(File in, File out) {
        FileChannel filein = null;
        FileChannel fileout = null;
        try {
            filein = new FileInputStream(in).getChannel();
            fileout = new FileOutputStream(out).getChannel();
            filein.transferTo(0, filein.size(), fileout);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(filein, fileout);
        }
    }

    /**
     * 分享文件
     * @param context 上下文
     * @param title 标题
     * @param filePath 文件路径
     */
    @SuppressWarnings("WeakerAccess")
    public static void shareFile(Context context, String title, String filePath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        Uri uri = Uri.parse("file://" + filePath);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, title));
    }

    /**
     * zip压缩
     * @param is 输入流
     * @param os 输出流
     */
    public static void zip(InputStream is, OutputStream os) {
        GZIPOutputStream gzip = null;
        try {
            gzip = new GZIPOutputStream(os);
            byte[] buf = new byte[1024];
            int len;
            while ((len = is.read(buf)) != -1) {
                gzip.write(buf, 0, len);
                gzip.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(is, gzip);
        }
    }

    /**
     * zip解压
     * @param is 输入流
     * @param os 输出流
     */
    public static void unzip(InputStream is, OutputStream os) {
        GZIPInputStream gzip = null;
        try {
            gzip = new GZIPInputStream(is);
            byte[] buf = new byte[1024];
            int len;
            while ((len = gzip.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(gzip, os);
        }
    }

    /**
     * 格式化文件大小
     * @param context 上下文
     * @param size 文件大小
     */
    public static String formatFileSize(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }

    /**
     * 将输入流写入到文件
     * @param is 输入流
     * @param file 文件
     */
    public static void Stream2File(InputStream is, File file) {
        byte[] b = new byte[1024];
        int len;
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(file);
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeIO(is, os);
        }
    }

    /**
     * 创建文件夹
     * @param filePath 文件目录
     */
    public static boolean createFolder(String filePath) {
        return createFolder(filePath, false);
    }

    /**
     * 创建文件夹（支持覆盖已存在的同名文件夹）
     * @param filePath 文件夹目录
     * @param recreate 是否覆盖
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean createFolder(String filePath, boolean recreate) {
        String folderName = getFolderName(filePath);
        if (folderName == null || folderName.length() == 0 || folderName.trim().length() == 0) {
            return false;
        }
        File folder = new File(folderName);
        if (folder.exists()) {
            if (recreate) {
                deleteFile(folderName);
                return folder.mkdirs();
            } else {
                return true;
            }
        } else {
            return folder.mkdirs();
        }
    }

    /**
     * 获取文件名
     * @param filePath 文件目录
     */
    public static String getFileName(String filePath) {
        if (LStringUtil.isEmpty(filePath)) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * 获取文件大小
     * @param filepath 文件名
     */
    public static long getFileSize(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            return -1;
        }
        File file = new File(filepath);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    /**
     * 重命名文件|文件夹
     * @param filepath 原来文件路径
     * @param newName 新文件路径
     */
    public static boolean rename(String filepath, String newName) {
        File file = new File(filepath);
        return file.exists() && file.renameTo(new File(newName));
    }

    /**
     * 获取文件夹名称
     * @param filePath 文件名称
     */
    @SuppressWarnings("WeakerAccess")
    public static String getFolderName(String filePath) {
        if (filePath == null || filePath.length() == 0 || filePath.trim().length() == 0) {
            return filePath;
        }
        int filePos = filePath.lastIndexOf(File.separator);
        return (filePos == -1) ? "" : filePath.substring(0, filePos);
    }

    /**
     * 获取文件夹下所有文件
     * @param path 文件路径
     */
    @SuppressWarnings("WeakerAccess")
    public static ArrayList<File> getFilesArray(String path) {
        File file = new File(path);
        File files[] = file.listFiles();
        ArrayList<File> listFile = new ArrayList<>();
        if (files != null) {
            for(File fileSingle: files){
                if(fileSingle.isFile())
                    listFile.add(fileSingle);
                if(fileSingle.isDirectory())
                    listFile.addAll(getFilesArray(fileSingle.toString()));
            }
        }
        return listFile;
    }

    /**
     * 删除文件夹下的所有文件
     * @param folder 文件路径
     */
    public static boolean deleteFiles(String folder) {
        if (folder == null || folder.length() == 0 || folder.trim().length() == 0) {
            return true;
        }
        File file = new File(folder);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * 打开图片
     * @param mContext 上下文
     * @param imagePath 图片路径
     */
    public static void openImage(Context mContext, String imagePath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(imagePath));
        intent.setDataAndType(uri, "image/*");
        mContext.startActivity(intent);
    }

    /**
     * 打开视频
     * @param mContext 上下文
     * @param videoPath 视频路径
     */
    public static void openVideo(Context mContext, String videoPath) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(videoPath));
        intent.setDataAndType(uri, "video/*");
        mContext.startActivity(intent);
    }

    /**
     * 打开URL
     * @param mContext 上下文
     * @param url 地址
     */
    public static void openURL(Context mContext, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);
    }

    /**
     * 下载文件
     * @param context 上下文
     * @param fileurl 文件路径
     */
    public static void downloadFile(Context context, String fileurl) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(fileurl));
        request.setDestinationInExternalPublicDir("/Download/", fileurl.substring(fileurl.lastIndexOf("/") + 1));
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if(downloadManager == null)
            return;
        downloadManager.enqueue(request);
    }

    /**
     * 是否挂在SD卡
     */
    public static boolean isSDCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获取应用在SD卡上的工作路径
     */
    public static String getAppExternalPath(Context context) {
/*        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append("Android/data/");
        sb.append(packageName);
        return sb.toString();*/
        return context.getObbDir().getAbsolutePath();
    }

    /**
     * 获取SD卡上的目录的路径
     * @param folder 文件路径
     */
    @Deprecated
    public static String getExtraPath(String folder) {
        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + folder;
        File file = new File(storagePath);
        if (!file.exists()) {
            file.mkdir();
        }
        return storagePath;
    }

    /**
     * 下载文件
     * @param context 上下文
     * @param urlPath 地址路径
     * @param filePath 保存路径
     */
    public static File downloadFile(Context context, String urlPath, String filePath) throws Exception {
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        InputStream is = conn.getInputStream();
        File file = new File(filePath);
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] buffer = new byte[1024];
        int len;
        int total = 0;
        while((len = bis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
            total += len;
        }
        fos.close();
        bis.close();
        is.close();
        return file;
    }

    /**
     * 根据路径获取文件内容
     * @param fileName 文件名称
     */
    @SuppressWarnings("WeakerAccess")
    public static String loadFileAsString(String fileName) throws Exception {
        FileReader reader = new FileReader(fileName);
        String text = loadReaderAsString(reader);
        reader.close();
        return text;
    }

    /**
     * 根据流获取内容
     * @param reader Reader
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

    /**
     * 获取文件大小
     * @param file 文件
     */
    @SuppressWarnings("WeakerAccess")
    public static long getFolderSize(File file) {
        long size = 0;
        try {
            File[] fileList = file.listFiles();
            for(File fileSingle: fileList){
                if(fileSingle.isDirectory())
                    size += getFolderSize(fileSingle);
                else
                    size += fileSingle.length();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * 获得文件存储路径
     */
    public static String getFilePath(Context context, String pathName) {
        if(context == null)
            return null;
        if (!Environment.isExternalStorageRemovable()) {//如果外部储存可用
            File file = context.getExternalFilesDir(null);
            if(file != null && file.exists()){
                return file.getPath() + "/" + pathName + "/";//获得外部存储路径,默认路径为 /storage/emulated/Android/data/com.../files/Logs/log_2018-03-14.txt
            }else
                return null;
        } else {
            return context.getFilesDir().getPath() + "/" + pathName + "/";//直接存在/data/data里，非root手机是看不到的
        }
    }
    /**
     * 获取文件名称，包含后缀
     * @param url 路径
     */
    public static String getAllFileName(String url){
        if(LStringUtil.isEmpty(url))
            return null;
        try{
            int endIndex = url.lastIndexOf("/");
            return url.substring(endIndex + 1);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 文件转字节数组
     * @param file 文件
     */
    public static byte[] fileToByteAry(File file) {
        RandomAccessFile rf = null;
        byte[] data = null;
        try {
            rf = new RandomAccessFile(file, "r");
            data = new byte[(int) rf.length()];
            rf.readFully(data);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (rf != null) {
                    rf.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return data;
    }
}
