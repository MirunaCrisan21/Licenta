package com.example.akshika.opencvtest;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.akshika.opencvtest.MirVideoFormat.FileReaders.DualFileReader;
import com.example.akshika.opencvtest.MirVideoFormat.FileReaders.IFileReader;
import com.example.akshika.opencvtest.MirVideoFormat.FileReaders.SimpleFileReader;
import com.example.akshika.opencvtest.MirVideoFormat.Messages.VideoSavingMessage;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.DirectCosineTransformWaterMarkingService;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.WaterMarkingType;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private EditText editText;
    boolean isloaded = false;
    private Drawable backGroundColor;
    private static final int PICKFILE_REQUEST_CODE = 223; // request code for the file picker activity
    private VideoSavingService videoService;
    private MyFrameRunner frameReader;
    private Button buttonLoad;
    private Button buttonStop;
    private Button buttonLast;
    private Button buttonRecord;
    private Button buttonWaterMark;
    boolean saveToVideo = false;
    private static final String TAG = "OCVSample::Activity";
    private static final int REQUEST_PERMISSION = 100;
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
        CameraView =(CameraBridgeViewBase) findViewById(R.id.cameraView);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION);
        }
        editText =(EditText) findViewById(R.id.edit_text);
        editText.setText("theremustbeaway");

        buttonWaterMark = (Button) findViewById(R.id.buttonWaterMarking);
        buttonWaterMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnableOrDisableWaterMark();
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
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Stop();
            }
        });
        backGroundColor = buttonStop.getBackground();

        buttonLoad = (Button) findViewById(R.id.buttonLoad);
        buttonLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Load();
            }
        });

        buttonLast = (Button) findViewById(R.id.buttonLast);
        buttonLast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
                handlerThread.start();
                frameReader = new MyFrameRunner(videoService.OutputPath,videoService,handlerThread.getLooper());

                CameraView.disableView();
                saveToVideo = false;

                buttonLoad.setBackgroundColor(0xAA00AA00);
                buttonLoad.setEnabled(false);

                frameReader.start(WaterMarkingType.LSBWaterMarking);
                ClearBackGround();
                isloaded = true;
            }
        });
        GetVideoService();

        CheckPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        CheckPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        CheckPermission(Manifest.permission.CAMERA);
        CheckPermission(Manifest.permission.MANAGE_DOCUMENTS);
        StartCamera();
    }

    private void GetVideoService(){
        File outputDirectory = new File(String.valueOf(
                Environment.getExternalStorageDirectory()));

        videoService = new VideoSavingService(outputDirectory.getPath(),
                new Size(CameraView.mFrameWidth,CameraView.mFrameHeight));
    }

    private void dummy(){
        // Create a Mat object
        Mat mat = new Mat(480, 640, CvType.CV_8UC3, new Scalar(0, 0, 255));

        // Create a VideoWriter object
        VideoWriter videoWriter = new VideoWriter("/storage/emulated/0/VID.avi", VideoWriter.fourcc('M', 'J', 'P', 'G'), 30, new Size(640, 480));

        // Check if VideoWriter is opened successfully
        if (!videoWriter.isOpened()) {
            System.out.println("Failed to open the output video file.");
            return;
        }

        // Write the Mat object to the AVI file
        for (int i = 0; i < 300; i++) {
            videoWriter.write(mat);
        }

        // Release the VideoWriter
        videoWriter.release();

        System.out.println("AVI file saved successfully.");
    }
    public void SaveVideoCommand(){

        if(!CameraView.isEnabled()) {
            StartCamera();
            SetColorToDefault(buttonRecord);
            buttonRecord.setEnabled(true);
        }
        else{
            if(editText.getText().toString().length()==0){
                Toast.makeText(this,"Watermarker needs to be longer",Toast.LENGTH_LONG).show();
            }else{
                GetVideoService();
                Message msg = videoService.obtainMessage();// Set the message data
                msg.what = 1;
                VideoSavingMessage object = new VideoSavingMessage(editText.getText().toString(),WaterMarkingType.LSBWaterMarking);

                msg.obj = object;
                videoService.sendMessage(msg);
                SetColorToGreen(buttonRecord);

                buttonRecord.setEnabled(false);
                buttonWaterMark.setEnabled(false);
                saveToVideo = true;
            }
        }
    }

    private void SetColorToDefault(Button target){
        target.setBackgroundColor(0xAABBBBBB);
    }

    private void SetColorToGreen(Button target){
        target.setBackgroundColor(0xAA00AA00);
    }

    private void SetColorToBlue(Button target){
        target.setBackgroundColor(0xAABBBBBB);
    }

    private void EnableOrDisableWaterMark(){
         SetColorToGreen(buttonWaterMark);
    }

    private void Stop(){
        if(saveToVideo){
            Message msg = videoService.obtainMessage();// Set the message data
            msg.what = 4; // Set the message type
            videoService.sendMessage(msg);
        }
        ClearBackGround();
        CameraView.enableView();
        saveToVideo = false;
        isloaded = false;
        SetColorToDefault(buttonRecord);
        SetColorToDefault(buttonWaterMark);
        SetColorToDefault(buttonLoad);
        buttonLoad.setEnabled(true);
        buttonRecord.setEnabled(true);
        buttonWaterMark.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                String name ="/storage/emulated/0/"+ fileUri.getPath().split(":")[1];
                GetVideoService();
                HandlerThread handlerThread = new HandlerThread("MyHandlerThread");
                handlerThread.start();
                frameReader = new MyFrameRunner(name,videoService,handlerThread.getLooper());
            }
        }
    }
    private void Load(){
        if(!isloaded){
            // create an intent to launch the file picker activity
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("*/*"); // specify that any file type can be selected

            // start the activity for result
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
            buttonLoad.setBackgroundColor(0xAAAA0000);
        }else{
            CameraView.disableView();
            saveToVideo = false;

            buttonLoad.setBackgroundColor(0xAA00AA00);
            buttonLoad.setEnabled(false);
            frameReader.start(WaterMarkingType.LSBWaterMarking);

            ClearBackGround();
        }
        isloaded = !isloaded;
    }

    private void ClearBackGround(){
        Canvas canvas = CameraView.getHolder().lockCanvas();
        canvas.drawARGB(255,0,0,0);
        CameraView.getHolder().unlockCanvasAndPost(canvas);
    }


    private void StartCamera(){
        CameraView.setCvCameraViewListener(this);
        CameraView.enableView();
        CameraView.setVisibility(View.VISIBLE);
        CameraView.setMaxFrameSize(400,400);
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
            OpenCVLoader.initAsync(
                    OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onCameraViewStarted(int width, int height) {
        videoService.matSize = new Size(width,height);
    }

    public void onCameraViewStopped() {

    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        // Create a new Mat file to store the pixel data as uints
        Mat result = inputFrame.rgba();

       if(saveToVideo){
            Message msg = videoService.obtainMessage();// Set the message data
            msg.what = 2; // Set the message type
            msg.obj = result; // Set the message data
            videoService.sendMessage(msg);
        }

        return result;
    }

    public class MyFrameRunner extends Handler {
        public IFileReader reader;
        private Mat frame;
        private VideoSavingService service;
        private String filePath;

        public MyFrameRunner(String path, VideoSavingService videoService, Looper looper) {
            super(looper);
            // store parameter for later user
            frame = new Mat();
            filePath = path;
            service = videoService;
            // Create an ExecutorService with a single thread
        }

        public void start(WaterMarkingType type){
            service.StopWritingVideo();
            ArrayList<String> input;
            switch (type) {
                case NoWaterMarking:
                    input = new ArrayList<>();
                    input.add(filePath);
                    reader = new SimpleFileReader(input,this);

                    // Create a new Thread object and pass the Runnable to its constructor

                    break;
                case LSBWaterMarking:
                    input = new ArrayList<>();
                    if(!filePath.contains("DCT"))
                    {
                        input.add(filePath);
                        input.add(videoService.GetDCTNameOfFile(filePath));
                    }else{
                        input.add(videoService.GetNameOfDCTFile(filePath));
                        input.add(filePath);
                    }
                    input.add(editText.getText().toString());

                    reader = new DualFileReader(input,this,new DirectCosineTransformWaterMarkingService(editText.getText().toString()));

                    // Create a new Thread object and pass the Runnable to its constructor

                    break;
                case DCTWaterMarking:
                    break;
                case DFTWaterMarking:
                    break;
            }
          Thread myThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Perform your task here
                      reader.read();
               }
                });

            // Start the new thread
            myThread.start();


        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 2:
                    frame  = (Mat) msg.obj;
                    if(!frame.empty())
                        DrawFrame();
                    break;
                case 3:
                     reader.close();
                    break;
                default:
                    break;
            }
        }

        private void DrawFrame(){
            final Bitmap bitmap = Bitmap.createBitmap(
                    frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(frame, bitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try{
                        // Display the Bitmap in the CameraBridgeViewBase
                        Canvas canvas = CameraView.getHolder().lockCanvas();
                        Rect rect = new Rect();
                        CameraView.getDrawingRect(rect);
                        if (canvas != null) {
                            Bitmap newbitmap = Bitmap.createScaledBitmap(bitmap, rect.width(), rect.height()/2, false);
                            canvas.drawBitmap(newbitmap, 0, rect.height()/4, null);
                            CameraView.getHolder().unlockCanvasAndPost(canvas);
                        }
                    }catch(Exception ex){
                        Log.e("AAA",ex.getMessage());
                    }
                }
            });

        }
    }
}