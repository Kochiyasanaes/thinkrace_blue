package com.xrs.bluetooth_device.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName FileUtils
 * @Author kotlin * @Email 949390151@qq.com
 * @Date 2023/4/11 18:10
 * ^_^^_^^_^^_^^_^^_^^_^
 */
public class FileUtil {

    private static class FileUtilTypeClass {
        private final static FileUtil instance = new FileUtil();
    }

    public static FileUtil getInstance() {
        return FileUtilTypeClass.instance;
    }

    private String pluginRootPath;


    public void init(Context context) {
        File filesDir = context.getFilesDir();
        pluginRootPath = createFolder(filesDir.getAbsolutePath().concat("/plugin"));
    }

    public String getPluginRootPath() {
        return pluginRootPath;
    }

    /**
     * 插件文件夹
     *
     * @param path 需要创建的文件夹路径
     * @return 返回创建的文件夹绝对路径
     */
    public String createFolder(String path) {
        File file = new File(path);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                return null;
            }
        }
        return file.getAbsolutePath();
    }

    public File[] getFolders(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (file.isFile()) {
            return null;
        }
        return file.listFiles();
    }

    /**
     * 拷贝插件到插件目录
     *
     * @param path 插件文件绝对路径
     * @return 返回插件文件绝对路径
     */
    public String copyPluginApk(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (!file.isFile()) {
            return null;
        }
        return copyPluginApk(file);
    }

    /**
     * 拷贝插件到插件目录
     *
     * @param file 插件文件
     * @return 返回插件文件绝对路径
     */
    public String copyPluginApk(File file) {
        if (!file.exists()) {
            return null;
        }
        if (!file.isFile()) {
            return null;
        }
        return copyFileToFolder(file, pluginRootPath);
    }


    /**
     * 获取不带后缀的文件名称
     *
     * @param fileName 文件名
     * @return 返回没有后缀的文件名
     */
    public String getFileNameWithoutSuffix(String fileName) {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    /**
     * 拷贝文件到指定目录
     *
     * @param path       文件路径
     * @param folderPath 目录路径
     * @return 返回拷贝后文件的绝对路径
     */
    public String copyFileToFolder(String path, String folderPath) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        return copyFileToFolder(file, folderPath);
    }

    /**
     * 拷贝文件到指定目录
     *
     * @param file       文件
     * @param folderPath 目录路径
     * @return 返回拷贝后文件的绝对路径
     */
    public String copyFileToFolder(File file, String folderPath) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            File createFile = new File(folderPath, file.getName());
            if (!createFile.exists()) {
                boolean createFileNewFile = createFile.createNewFile();
                if (!createFileNewFile) {
                    return null;
                }
            }
            FileOutputStream outputStream = new FileOutputStream(createFile);
            byte[] data = new byte[1024];
            int length;
            while ((length = inputStream.read(data)) != -1) {
                outputStream.write(data, 0, length);
            }
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            return createFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public File getUriFile(Context context, Uri uri) {
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            return new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver contentResolver = context.getContentResolver();
            StringBuilder sb = new StringBuilder();
            sb.append("temp");
            sb.append(".");
            sb.append(MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri)));
            InputStream inputStream;
            try {
                inputStream = contentResolver.openInputStream(uri);
                String absolutePath = context.getCacheDir().getAbsolutePath();
                File catchFolder = new File(absolutePath);
                File[] files = catchFolder.listFiles();
                assert files != null;
                for (File f : files) {
                    f.delete();
                }
                File file = new File(absolutePath, sb.toString());
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] data = new byte[1024];
                while ((inputStream.read(data)) != -1) {
                    outputStream.write(data);
                }
                outputStream.close();
                inputStream.close();
                return file;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void deleteFile(String path) {
        deleteFile(new File(path), false);
    }

    public void deleteFile(File file) {
        deleteFile(file, false);
    }

    public void deleteFile(String path, boolean isDeleteFolder) {
        deleteFile(new File(path), isDeleteFolder);
    }

    public void deleteFile(File file, boolean isDeleteFolder) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for (File itemFile : files) {
            deleteFile(itemFile.getAbsolutePath(), isDeleteFolder);
        }
        if (isDeleteFolder) {
            file.delete();
        }
    }

    private String isExistDir(String saveDir) throws IOException {
        File downloadFile=new File(saveDir);
        if(!downloadFile.mkdirs()){
            downloadFile.createNewFile();
        }
        String savePath=downloadFile.getAbsolutePath();
        return savePath;
    }

    /**
     * 传输文件
     *
     * @param filePath
     */
    public void sendFile(String filePath) {
        
       /* TransferThread r;
        synchronized (this) {
            if (mState != STATE_TRANSFER) return;
            r = mTransferThread;
        }
        r.writeFile(filePath);*/
    }

}
