/*
package com.xrs.bluetooth_device.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CheckVersionUtils {
    private Context mContext;
    private Dialog mDialog;
    private TextView tvUpdate, tvProgress;
    private ProgressBar progressBar;

    //下载地址
    private String apkUrl = "https://api.beehome360.com:8443/apk/test.apk";
    private List<String> apkDes;
    private String newVersion;

    public CheckVersionUtils(Context context, String apkUrl, List<String> apkDes, String newVersion) {
        this.mContext = context;
        this.apkUrl = apkUrl;
        this.apkDes = apkDes;
        this.newVersion = newVersion;
    }

    public CheckVersionUtils(Context context) {
        this.mContext = context;
        initDownload();
    }

    */
/**
     * 版本更新弹框
     *//*

    @SuppressLint("SetTextI18n")
    public void showUpdateVersion() {

    }

    */
/**
     * 下载apk
     *//*

    private void initDownload() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder()
                .url(apkUrl)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("apk下载失败：",e.getMessage());
                apkUrl = apkUrl.replace("https", "http");
                initDownload();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                InputStream inputStream = body.byteStream();
                saveFile(inputStream, Environment.getExternalStorageDirectory() + "/" + "demo.apk", body.contentLength());
            }
        });
    }

    */
/**
     * @param saveFile   存放的地址
     * @param fileLength 文件的长度
     *//*

    @SuppressLint("SetTextI18n")
    private void saveFile(InputStream inputStream, String saveFile, final long fileLength) {
        long count = 0;
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(saveFile));
            int length = -1;
            byte[] bytes = new byte[1024 * 10];
            while ((length = inputStream.read(bytes)) != -1) {
                // 写入文件
                outputStream.write(bytes, 0, length);
                count += length;

               */
/* final long finalCount = count;
                ((Activity) mContext).runOnUiThread(() -> {
                    // 设置进度条最大值
                    progressBar.setMax((int) fileLength);
                    // 设置下载进度
                    progressBar.setProgress((int) finalCount);
                    // 设置进度文本 （100 * 当前进度 / 总进度）
                    tvProgress.setText((int) (100 * finalCount / fileLength) + "%");
                });*//*

            }
            inputStream.close();
            outputStream.close();
          */
/*  ((Activity) mContext).runOnUiThread(() -> {*//*

                //下载完成，自动安装
               */
/* mDialog.dismiss();*//*

               */
/* ((Activity) mContext).finish();*//*

            installApk(new File(Environment.getExternalStorageDirectory() + "/" + "demo.apk"));
           */
/* });*//*

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    */
/**
     * 安装apk文件
     *
     * @param apkFile 安装包所在目录
     *//*

    private void installApk(File apkFile) {
        //判断版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(mContext,
                    "com.carson.fileprovider", apkFile);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //对目标应用临时授权该Uri所代表的文件
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            mContext.startActivity(install);
        } else {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(install);
        }
    }

}
*/
