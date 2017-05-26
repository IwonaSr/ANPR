package com.example.ejwon.anpr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AndroidCameraApi extends AppCompatActivity {
    private static final String TAG = "AndroidCameraApi";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 0);
//        ORIENTATIONS.append(Surface.ROTATION_90, 90);
//        ORIENTATIONS.append(Surface.ROTATION_180, 180);
//        ORIENTATIONS.append(Surface.ROTATION_270, 270);
//    }

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private static final int mImageFormat = ImageFormat.YUV_420_888;
    private int mHeigth;
    int[] mRgbBuffer;
    public File folder;
    int i = 0;
    private int count = 0;
    private int rotation = 0;
//    private Size[] jpegSizes = null;
    public static Point screenParametersPoint = new Point();
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_camera_api);
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;

        Boolean checkOpenCV = OpenCVLoader.initAsync(
                OpenCVLoader.OPENCV_VERSION_3_0_0,
                getApplicationContext(),
                mOpenCVCallBack);

        if (checkOpenCV) {
            try {

            } catch (Exception e1) {
                Log.e("MissingOpenCVManager", e1.toString());
            }
        }

        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                takePicture();
            }
        });
    }

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //Load Cascade Classifier
                    CascadeClassifierUtil.loadCascadeClassifier(getResources(), getApplicationContext());
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            setUpCameraOutputs(width, height);
//            openCamera(width, height);
//            configureTransform(width, height);
            openCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            configureTransform(width, height);
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    static int getDisplayRotation(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
        return 0;
    }
//    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
//        @Override
//        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            super.onCaptureCompleted(session, request, result);
//            Toast.makeText(AndroidCameraApi.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
//            createCameraPreview();
//        }
//    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
//        return (sensorOrientation + deviceOrientation + 360) % 360;
        return (deviceOrientation+ sensorOrientation + 270) % 360;
    }
    //tutaj
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(texture);

            Surface mImageSurface = imageReader.getSurface(); //dopisałam -22.05
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);
            captureRequestBuilder.addTarget(mImageSurface); // dopisalam - 22.05 - jeśli nie dodamy imageReaderSurface do buildera wtedy nie odpali się imageReaderListener

           rotation = getWindowManager().getDefaultDisplay().getRotation();
            int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;

                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(AndroidCameraApi.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
//        setUpCameraOutputs(width, height);
//        configureTransform(width, height);
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AndroidCameraApi.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
//            manager.openCamera(cameraId, stateCallback, null);
            manager.openCamera(mCameraId, stateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }
    static class CompareSizeByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum( (long)(lhs.getWidth() * lhs.getHeight()) -
                    (long)(rhs.getWidth() * rhs.getHeight()));
        }

    }
    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for(Size option : choices) {
            if(option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if(bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    CameraCharacteristics characteristics;
    private Size mPreviewSize;
    private Size mImageSize;
    private String mCameraId;
    private int mSensorOrientation;
    //dodane 22.05
    private void setUpCameraOutputs(int width, int height) {
//        if (null == cameraDevice) {
//            Log.e(TAG, "cameraDevice is null");
//            return;
//        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
//            cameraId = manager.getCameraIdList()[0];
            for(String cameraId : manager.getCameraIdList()) {
                characteristics = manager.getCameraCharacteristics(cameraId);
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;

               int value =  getDisplayRotation(this);
//                sensorRotation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

//                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
//                mTotalRotation = sensorToDeviceRotation(characteristics, deviceOrientation);
//                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
//
//                int displayRotation = this.getWindowManager().getDefaultDisplay().getRotation();
//                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//                boolean swappedDimensions = false;
//                switch (displayRotation) {
//                    case Surface.ROTATION_0:
//                    case Surface.ROTATION_180:
//                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
//                            swappedDimensions = true;
//                        }
//                        break;
//                    case Surface.ROTATION_90:
//                    case Surface.ROTATION_270:
//                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
//                            swappedDimensions = true;
//                        }
//                        break;
//                    default:
//                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
//                }
//                Point displaySize = new Point();
//                this.getWindowManager().getDefaultDisplay().getSize(displaySize);
//                int rotatedPreviewWidth = width;
//                int rotatedPreviewHeight = height;
//
//                if (swappedDimensions) {
//                    rotatedPreviewWidth = height;
//                    rotatedPreviewHeight = width;
//                }


//                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
//                mTotalRotation = sensorToDeviceRotation(characteristics, deviceOrientation);
//                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
//
                int rotatedWidth = width;
                int rotatedHeight = height;
//                if(swapRotation) {
//                    rotatedWidth = height;
//                    rotatedHeight = width;
//                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mImageSize = chooseOptimalSize(map.getOutputSizes(mImageFormat), rotatedWidth, rotatedHeight);
                imageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), mImageFormat, 5); // YUV - 30fps at 8MP - image processing

                // We fit the aspect ratio of TextureView to the size of preview we picked.
//                int orientation = getResources().getConfiguration().orientation;
//                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
////                    textureView.setAspectRatio(
////                            mPreviewSize.getWidth(), mPreviewSize.getHeight());
//                } else {
////                    textureView.setAspectRatio(
////                            mPreviewSize.getHeight(), mPreviewSize.getWidth());
//                }

                mCameraId = cameraId;


                folder = new File(Environment.getExternalStorageDirectory() + File.separator + "ANPR/");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                } else {
                    System.out.println("Cannot create a folder");
                }
//                final int finalHeight = height;

                imageReader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

                return;

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private int mTotalRotation;
    //do obortu camera
    private void configureTransform(int viewWidth, int viewHeight) {

        if (textureView == null || mPreviewSize  == null) return;

        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90  == rotation || Surface.ROTATION_270  == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        textureView.setTransform(matrix);
    }


    private final ImageReader.OnImageAvailableListener readerListener =
            new ImageReader.OnImageAvailableListener() {

                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.e(TAG, "onImageAvailable: " + count++);
                    Image image = null;
                    Bitmap bmp = null;
                    try {
                        image = reader.acquireLatestImage();
                        if (image == null) {
                            System.out.println("Image is not available.");
                            return;
                        }
                        int fmt = reader.getImageFormat();
                        Log.d(TAG, "bob image fmt:" + fmt);

                        //for old example jpeg
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.capacity()];
//                        buffer.get(bytes);

                        // RowStride of planes may differ from width set to image reader, depends
                        // on device and camera hardware, for example on Nexus 6P the rowStride is
                        // 384 and the image width is 352.
//                        save(bytes);
                        int width = image.getWidth();
                        int height = image.getHeight();
                        if (image.getFormat() == mImageFormat) {
                            Log.e(TAG, " process image, i: " + i);
                            Mat matImage = ImageFormatConversion.convertYuv420888ToMat(image, true);
                            bmp = ImageFormatConversion.getBitmapFromMat(matImage);
                            ImageFormatConversion.saveBitmapAsJpegFile(bmp, folder);
                        }

                        image.close();

                    }//                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Log.e(TAG, "in the finally! ------------");
                        if (image != null) {
                            image.close();
                        }
                    }
                    if (bmp != null) {
                        Log.e(TAG, "Decoding successful!");
                    } else {
                        Log.d(TAG, "No image was processing…");
                    }
                }

            };

    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
                private void process(CaptureResult result) {
                }

                @Override
                public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                                CaptureResult partialResult) {
                    process(partialResult);
                }

                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                               TotalCaptureResult result) {
                    process(result);
                }

            };

    //Wyświetlanie obrazu z kamery w widoku
    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);


//            takePicture();


        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(AndroidCameraApi.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            setUpCameraOutputs(textureView.getWidth(), textureView.getHeight());
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }
}