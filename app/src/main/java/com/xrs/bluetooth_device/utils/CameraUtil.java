package com.xrs.bluetooth_device.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ToneGenerator;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.xrs.bluetooth_device.MainActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class CameraUtil implements SurfaceTexture.OnFrameAvailableListener {
    Context context;
    ImageUploader imageUploader = new ImageUploader();
    private Camera.PictureCallback jpegCallback = (param1ArrayOfByte, param1Camera) -> {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("camera format=");
        stringBuilder.append(CameraUtil.this.mPs.getPictureFormat());
        /*LogUtils.i(stringBuilder.toString());*/
        if (CameraUtil.this.mPs.getPictureFormat() == 256) {
            String str = CameraUtil.this.savePicture(param1ArrayOfByte);
            if (str != null) {
                LogUtils.i("保存成功:"+str);
                imageUploader.uploadImage(str);
        /*        if (CoreService.getInstance() != null) {
                    //str就是path.
                    CoreService.DevliverStringMessage("PhotoRemoteTaken", str);
                }*/
            } else {
                LogUtils.i("camera 空间不足,保存失败");
            }
        }
        CameraUtil.this.releaseCamera();
    };

    private Camera mCamera;

    String mNumber;

    private int mOrientation;
    private int custom_camera_orientation = 0;
    private int custom_camera_mirror = 0;
    private Camera.Parameters mPs;

    private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            LogUtils.i("camera shutterCallback");
        }
    };

    private SurfaceTexture surfaceTexture;

    private ToneGenerator tone;

    public CameraUtil(Context paramContext) {
        this.context = paramContext;
    }

    private Camera openFacingBackCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        int i = Camera.getNumberOfCameras();
        Camera camera = null;
        byte b = 0;
        while (b < i) {
            Camera.getCameraInfo(b, cameraInfo);

            if (cameraInfo.facing == 0) {
                try {
                    camera = Camera.open(b);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
            b++;
        }
        return camera;
    }

    private void releaseCamera() {
        if (this.mCamera != null)
            try {
                if (this.surfaceTexture != null)
                    this.surfaceTexture.setOnFrameAvailableListener(null);
                this.mCamera.setPreviewTexture(null);
                this.mCamera.stopPreview();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        this.mCamera.release();
    }

    private String savePicture(byte[] paramArrayOfByte) {
        int jpg_quality = 50;
        String str = null;
        try {
            if (Environment.getExternalStorageState().equals("mounted")) {

                long availableBytes = new StatFs(Environment.getExternalStorageDirectory().toString()).getAvailableBytes();
                str = MainActivity.sContext.getFilesDir() + "";
                clearDirectory(MainActivity.sContext.getFilesDir());
                if (availableBytes < paramArrayOfByte.length) {
                    return null; // 存储空间不足
                }


                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                stringBuilder.append("/TakePhoto_");
                stringBuilder.append(this.mNumber);
                stringBuilder.append(".jpg");
                str = stringBuilder.toString();
                Bitmap bitmap1 = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);
                Bitmap bitmap2 = bitmap1;

                // 移除了所有关于custom_camera_mirror的逻辑
                Matrix matrix = new Matrix();

                matrix.reset();
                switch (custom_camera_orientation) {
                    case 90:
                    case 270:
                  /*      matrix.postRotate(custom_camera_orientation);
                        bitmap2 = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);*/
                        break;
                    case 0:
                    case 180:
                        // 这里不再有镜像操作
                        break;
                    default:
                        break;
                }


                matrix.setScale(-1, 1); // 这是水平镜像变换
                matrix.postTranslate(-bitmap2.getWidth(), 0); // 移动图片使其回到视图内
                matrix.postRotate(270); // 逆时针旋转90度

                bitmap2 = Bitmap.createBitmap(bitmap2, 0, 0, bitmap2.getWidth(), bitmap2.getHeight(), matrix, true);

                // 保存图片
                File file = new File(str);
                if (!file.exists()) {
                    file.createNewFile();
                }
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                bitmap2.compress(Bitmap.CompressFormat.JPEG, jpg_quality, bufferedOutputStream);
                bufferedOutputStream.flush();
                bufferedOutputStream.close();

                LogUtils.i("camera Take Photo and Save to " + str);
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearDirectory(File directory) {
        if (directory.isDirectory()) {
            String[] items = directory.list();
            for (String item : items) {
                File file = new File(directory, item);
                if (file.isFile()) {
                    if (!file.delete()) {
                        // 如果文件无法删除，可以选择记录日志或者抛出异常
                        Log.e("TAG", "Could not delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private void takePicture(Context paramContext) {
        if (this.mCamera != null) {
            SurfaceView surfaceView = new SurfaceView(paramContext);
            int width = 640;
            int height = 480;
            try {
                this.mCamera.setPreviewDisplay(surfaceView.getHolder());
                this.mPs = this.mCamera.getParameters();
                String picsize = PropertiesUtil.getSystemProperties("persist.sys.camera_capsize", "640x480");

                if (!picsize.trim().isEmpty()) {
                    String sizes[] = picsize.toLowerCase().split("x");
                    if (sizes.length == 2) {
                        try {
                            int tmpw = Integer.parseInt(sizes[0]);
                            int tmph = Integer.parseInt(sizes[1]);
                            width = tmpw;
                            height = tmph;
                        } catch (Exception e) {
                        }
                    }
                }

                LogUtils.i("CameraUtil---setCamera capture size " + width + "x" + height);
                this.mPs.setPictureSize(width, height);
                setCameraDisplayOrientation(paramContext, 0, this.mCamera);
                this.mCamera.setParameters(this.mPs);
                LogUtils.i("CameraUtil---startPreview");
                this.surfaceTexture = new SurfaceTexture(10);
                this.surfaceTexture.setOnFrameAvailableListener(this);
                this.mCamera.setPreviewTexture(this.surfaceTexture);
                this.mCamera.startPreview();
                this.mCamera.enableShutterSound(false);
                (new Thread(() -> {
                    try {
                        CameraUtil.this.mCamera.takePicture(CameraUtil.this.shutterCallback, null, CameraUtil.this.jpegCallback);
                        PropertiesUtil.mSleep(5000);
                        return;
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        CameraUtil.this.releaseCamera();
                        return;
                    }
                })).start();
                return;
            } catch (Exception e) {
                e.printStackTrace();
                releaseCamera();
            }
        }
    }

    public int getDisplayRotation(Context paramContext) {
        int i = ((WindowManager) paramContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("camera getDisplayRotation rotation=");
        stringBuilder.append(i);
        LogUtils.i(stringBuilder.toString());

        switch (i) {
            default:
                return 0;
            case 3:
                return 270;
            case 2:
                return 180;
            case 1:
                return 90;
            case 0:
                break;
        }
        return 0;
    }

    public void getPicture(String paramString) {
        this.mCamera = openFacingBackCamera();
        takePicture(this.context);
        this.mNumber = paramString;
    }

    public void onFrameAvailable(SurfaceTexture paramSurfaceTexture) {
        LogUtils.i("camera onFrameAvailable");
    }

    public void setCameraDisplayOrientation(Context context, int cameraId, Camera camera) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);
        int rotation = getDisplayRotation(context); // 获取设备当前方向

        // 计算相机预览的方向
        int degrees = 0;
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // 前置摄像头
            // 需要反转方向，因为前置摄像头的自然方向与设备的方向相反
            degrees = (cameraInfo.orientation - rotation + 360) % 360;
        } else { // 后置摄像头
            // 直接使用相机的方向，但需要根据设备的当前方向进行调整
            degrees = (cameraInfo.orientation + rotation) % 360;
        }

        // 设置相机预览的方向
        camera.setDisplayOrientation(degrees);
        LogUtils.i("Camera display orientation set to: " + degrees);
    }
}

