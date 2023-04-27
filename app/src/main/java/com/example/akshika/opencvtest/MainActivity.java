package com.example.akshika.opencvtest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.akshika.opencvtest.MirVideoFormat.VideoReader;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    Thread playerThread;
    boolean IsLoaded = false;
    private Drawable backGroundColor;
    private static final int PICKFILE_REQUEST_CODE = 223; // request code for the file picker activity
    private VideoSavingService videoservice;
    private MyFrameRunner frameReader;
    private SurfaceView surfaceView;
    private Surface surface ;
    private Button buttonLoad;
    private Button buttonStop;
    private Button buttonRecord;
    private Button buttonWaterMark;
    boolean saveToVideo = false;
    boolean applyWatermark = false;
    private static final String TAG = "OCVSample::Activity";
    private static final int REQUEST_PERMISSION = 100;
    private int actualVideoWidth, actualVideoHeight;
    private CameraBridgeViewBase CameraView;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
    }

    private void CheckPermission(String permission){
        int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 0;
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    CameraView.enableView();
                    try {
                        initializeOpenCVDependencies();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    private void initializeOpenCVDependencies() throws IOException {
        CameraView.enableView();
    }

    private boolean cameraIsOn = false;

    public MainActivity() {

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.layout);
        CameraView =(CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }
        buttonWaterMark = (Button) findViewById(R.id.buttonWaterMarking);
        buttonWaterMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaterMarkVideo();
            }
        });
        buttonRecord = (Button) findViewById(R.id.buttonRecord);
        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveVideoCommand();
            }
        });
        buttonStop = (Button) findViewById(R.id.buttonStop);
        backGroundColor = buttonStop.getBackground();
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stop();
            }
        });
        buttonLoad = (Button) findViewById(R.id.buttonLoad);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Load();
            }
        });

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surface = surfaceView.getHolder().getSurface();
        surfaceView.setVisibility(View.INVISIBLE);
        StartCamera();

        File outputDirectory = new File(String.valueOf(Environment.getExternalStorageDirectory()));
        videoservice = new VideoSavingService(outputDirectory.getPath(),new Size(CameraView.getHeight(),CameraView.getWidth()));

        CheckPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        CheckPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        CheckPermission(Manifest.permission.CAMERA);
        CheckPermission(Manifest.permission.MANAGE_DOCUMENTS);

    }

    public void SaveVideoCommand(){
        if(!cameraIsOn)
            StartCamera();
        else{
            saveToVideo = !saveToVideo;
            if(saveToVideo){
                try {
                    videoservice.MakeVideo();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                buttonRecord.setBackgroundColor(0xAA00AA00);
                buttonRecord.setEnabled(false);
            }

        }
    }

    private void WaterMarkVideo(){
        if(applyWatermark){
            applyWatermark = false;

            buttonWaterMark.setBackgroundColor(0xAA00AA00);// 0xAARRGGBB
        }else{
            applyWatermark = true;
            buttonWaterMark.setBackground(backGroundColor);
        }
    }

    private void Stop(){
        if(saveToVideo){
            saveToVideo = false;
            videoservice.StopWritingVideo();
            buttonRecord.setBackground(backGroundColor);
            buttonRecord.setEnabled(true);
            buttonWaterMark.setBackground(backGroundColor);
            applyWatermark = false;
        }
        saveToVideo = false;
        if(playerThread!=null){
            IsLoaded = false;
            playerThread.interrupt();
            videoservice.StopReadingVideo();
            buttonRecord.setBackground(backGroundColor);
            buttonRecord.setEnabled(true);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                String name ="/storage/emulated/0/"+ fileUri.getPath().split(":")[1];
                frameReader = new MyFrameRunner(name,videoservice);
            }
        }
    }


    private void Load(){
        if(!IsLoaded){
            // create an intent to launch the file picker activity
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*"); // specify that any file type can be selected

            // start the activity for result
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            buttonLoad.setBackgroundColor(0xAAAA0000);
        }else{
            //videoservice.OpenVideoReader(name,true);
            CameraView.setVisibility(View.INVISIBLE);
            surfaceView.setVisibility(View.VISIBLE);

            //videoViewer.setVisibility(View.VISIBLE);
            cameraIsOn = false;
            saveToVideo = false;

            playerThread = new Thread(frameReader);
            playerThread.start();
            buttonLoad.setBackgroundColor(0xAA00AA00);
            buttonLoad.setEnabled(false);
        }
        IsLoaded = !IsLoaded;
    }


    private void StartCamera(){
        CameraView.setVisibility(View.VISIBLE);
        surfaceView.setVisibility(View.INVISIBLE);
        //videoViewer.setVisibility(View.INVISIBLE);
        CameraView.setCvCameraViewListener(this);
        CameraView.enableView();
        CameraView.setMaxFrameSize(400,400);
        cameraIsOn = true;
        saveToVideo = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (CameraView != null)
            CameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (CameraView != null)
            CameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        actualVideoWidth = width;
        actualVideoHeight = height;
        videoservice.matSize = new Size(width,height);
    }

    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        if(saveToVideo){
            videoservice.Save(inputFrame.rgba(),applyWatermark);
        }
        return inputFrame.rgba();
    }

    public class MyFrameRunner implements Runnable {
        private Mat frame;
        private VideoSavingService service;
        private String filePath;
        public MyFrameRunner(String path,VideoSavingService videoService) {
            // store parameter for later user
            frame = new Mat();
            filePath = path;
            service = videoService;
        }
        @Override
        public void run() {
            service.StopWritingVideo();
            service.StopReadingVideo();
            //Canvas canvas = surface.lockCanvas(null);
            //surface.unlockCanvasAndPost(canvas);
            try {
                VideoReader reader = service.OpenVideoReader(filePath,true);
                while(reader.ReadAnotherFrame(frame)){
                    //canvas = surface.lockCanvas(null);
                    Bitmap bitmap = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(frame, bitmap);

                    //canvas.drawBitmap(bitmap, 0, 0, null);
                    //surface.unlockCanvasAndPost(canvas);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                //if(canvas!=null)
                //    surface.unlockCanvasAndPost(canvas);
            }
        }
    }
}