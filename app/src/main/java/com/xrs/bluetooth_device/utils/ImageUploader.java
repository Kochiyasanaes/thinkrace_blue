package com.xrs.bluetooth_device.utils;

import android.os.Build;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @ClassName ImageUploader
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2023/8/17 16:13
 * ^_^^_^^_^^_^^_^^_^^_^
 */
public class ImageUploader {

    public void uploadImage(String imagePath) {

        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建 Socket 对象，连接服务器
                    OrderUtil.getInstance().sendMsg("00000");
                    Thread.sleep(5000);
                    // 打开文件输入流，读取本地图片文件
                    File imageFile = new File(imagePath);
                    FileInputStream fileInputStream = new FileInputStream(imageFile);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

                    // 获取文件长度
                    long fileSize = imageFile.length();

                    // 设置每个图片包的长度
                    int packageSize = 256;

                    // 计算需要分包的总数
                    int totalPackages = (int) Math.ceil((double) fileSize / packageSize);

                    // 读取和发送图片数据
                    byte[] buffer = new byte[packageSize];
                    int bytesRead;
                    int currentPackage = 1;

                    while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                        // 发送图片包信息
                        Log.e("byte:",bytesRead + "");
                        if (currentPackage == totalPackages) {
                            StringBuilder packageInfoBuilder = new StringBuilder();
                            packageInfoBuilder.append("IWAP42,").append(getCurrentTimestamp()).append(",").append(totalPackages).append(",").append(currentPackage).append(",").append(bytesToHexString(buffer, bytesRead).length()*2).append(",").append(bytesToHexString(buffer, bytesRead)).append("#");
                     /*       String packageInfo = "IWAP42," + getCurrentTimestamp() + "," + totalPackages + "," + currentPackage + "," + bytesToHexString(buffer, bytesRead).length() + ","+  bytesToHexString(buffer, bytesRead) +"#";*/

                            // 发送最后一个包的十六进制字符串
                            OrderUtil.getInstance().sendMsg(packageInfoBuilder.toString());
                            Thread.sleep(3000);
                        } else {
                            Log.e("byte:",currentPackage+"");
                            StringBuilder packageInfoBuilder = new StringBuilder();
                            packageInfoBuilder.append("IWAP42,").append(getCurrentTimestamp()).append(",").append(totalPackages).append(",").append(currentPackage).append(",").append(packageSize * 2).append(",").append(bytesToHexString(buffer, bytesRead)).append("#");
                            // 发送最后一个包的十六进制字符串
                            OrderUtil.getInstance().sendMsg(packageInfoBuilder.toString());
                            Thread.sleep(3000);
                        }
                        currentPackage++;
                    }

                    // 关闭流和 Socket 连接
                    bufferedInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        uploadThread.start();
    }

    private String bytesToHexString(byte[] bytes, int length) {
        StringBuilder hexBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            hexBuilder.append(String.format("%02X", bytes[i])); // 将每个字节转换为两位的十六进制字符串
        }
        return hexBuilder.toString();
    }

    public static String bytesToString(byte[] bytes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        return null;
    }

    private String getCurrentTimestamp() {
        return System.currentTimeMillis() + "";
        // 获取当前时间戳的代码，可以参考之前提供的获取当前时间戳的方法
        // 确保时间戳的格式与所需的规则一致
    }

    public String stringToHex(String input) {
        StringBuilder stringBuilder = new StringBuilder();

        for (char c : input.toCharArray()) {
            stringBuilder.append(String.format("%02X", (int) c));
        }

        return stringBuilder.toString();
    }

}
