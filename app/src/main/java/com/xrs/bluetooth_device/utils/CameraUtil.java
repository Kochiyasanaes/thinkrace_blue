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
                if ((new StatFs(Environment.getExternalStorageDirectory().toString())).getAvailableBytes() < paramArrayOfByte.length)
                    return null;
                str = MainActivity.sContext.getFilesDir() + "";

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(str);
                stringBuilder.append("/TakePhoto_");
                stringBuilder.append(this.mNumber);
                stringBuilder.append(".jpg");
                str = stringBuilder.toString();
                Bitmap bitmap1 = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length);
                Bitmap bitmap2 = bitmap1;


//        if ((this.context.getResources().getConfiguration()).orientation == 1) {
                custom_camera_mirror = 0;
                Matrix matrix = new Matrix();
                matrix.reset();
                switch (custom_camera_orientation) {
                    case 90:
                    case 270:
                        matrix.postRotate(custom_camera_orientation);
                        if (custom_camera_mirror == -1)
                            matrix.postScale(-1, 1);
                        else if (custom_camera_mirror == -2)
                            matrix.postScale(1, -1);

                        bitmap2 = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);
                        break;
                    case 0:
                    case 180:
                        if (custom_camera_mirror < 0) {
                            if (custom_camera_mirror == -1)
                                matrix.postScale(-1, 1);
                            else if (custom_camera_mirror == -2)
                                matrix.postScale(1, -1);
                            bitmap2 = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);
                        }
                    default:
                        break;
                }

                Bitmap bitmap3 = bitmap2;
         /*       if (Config.needWaterMark()) {
                    bitmap3 = ImageUtil.drawTextToLeftTop(this.context, bitmap2, TimeUtil.getSystemTimeForCameraMark(), 50, Color.RED, 25, 25);
                }*/
                File file = new File(str);
                if (!file.exists())
                    file.createNewFile();
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                bitmap3.compress(Bitmap.CompressFormat.JPEG, jpg_quality, bufferedOutputStream);
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

    public void setCameraDisplayOrientation(Context paramContext, int paramInt, Camera paramCamera) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(paramInt, cameraInfo);
        paramInt = getDisplayRotation(paramContext);
        custom_camera_orientation = 0;
        LogUtils.i("camera getDisplayRotation1 " + paramInt + " cameraInfo.orientation " + cameraInfo.orientation + " custom_camera_orientation " + custom_camera_orientation);

        if (cameraInfo.facing == 1) {
            paramInt = (360 - (custom_camera_orientation + paramInt) % 360) % 360;
        } else {
            paramInt = (custom_camera_orientation - paramInt + 360) % 360;
        }
        /*
    if (cameraInfo.facing == 1) {
      paramInt = (360 - (cameraInfo.orientation + paramInt) % 360) % 360;
    } else {
      paramInt = (cameraInfo.orientation - paramInt + 360) % 360;
    }*/

        LogUtils.i("camera final display orientation " + paramInt);

        paramCamera.setDisplayOrientation(paramInt);
        this.mOrientation = paramInt;
    }
}

