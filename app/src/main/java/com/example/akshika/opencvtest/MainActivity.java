package com.example.akshika.opencvtest;

import static java.lang.Integer.MAX_VALUE;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.VideoView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private VideoService videoservice;
    private VideoView videoViewer;
    private Button buttonLoad;
    private Button buttonStop;
    private Button buttonStart;
    private Button buttonWaterMark;
    private boolean IsRecording = false;
    private boolean IsReplaying = false;

    private static final String TAG = "OCVSample::Activity";
    private static final int REQUEST_PERMISSION = 100;
    private int w, h;
    private CameraBridgeViewBase CameraView;
    TextView tvName;
    FeatureDetector detector;
    DescriptorExtractor descriptor;
    Mat descriptors2;
    MatOfKeyPoint keypoints2;

    static {
        if (!OpenCVLoader.initDebug())
            Log.d("ERROR", "Unable to load OpenCV");
        else
            Log.d("SUCCESS", "OpenCV loaded");
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
        detector = FeatureDetector.create(FeatureDetector.ORB);
        descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        /*
        *         matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        img1 = new Mat();
        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open("a.jpeg");
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        Utils.bitmapToMat(bitmap, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        img1.convertTo(img1, 0); //converting the image to match with the type of the cameras image
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);
        * */

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
        buttonStart = (Button) findViewById(R.id.buttonRecord);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartCamera();
            }
        });
        buttonStop = (Button) findViewById(R.id.buttonStop);
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

        videoViewer = (VideoView) findViewById(R.id.videoView1);
        videoViewer.setVisibility(View.INVISIBLE);

        StartCamera();

        File outputDirectory = new File(String.valueOf(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)));
        videoservice = new VideoService(outputDirectory.getPath(),new Size(CameraView.getHeight(),CameraView.getWidth()));
    }

    private void WaterMarkVideo(){

    }

    private void Stop(){

        
    }

    private void Load(){
        IsReplaying = false;
        CameraView.setVisibility(View.INVISIBLE);
        videoViewer.setVisibility(View.VISIBLE);
    }


    private void StartCamera(){
        CameraView.setVisibility(View.VISIBLE);
        videoViewer.setVisibility(View.INVISIBLE);
        CameraView.setCvCameraViewListener(this);
        CameraView.enableView();
        CameraView.setMaxFrameSize(400,400);
        tvName = (TextView) findViewById(R.id.text1);

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
        w = width;
        h = height;
    }

    VideoWriter videoWriter;

    public void onCameraViewStopped() {


    }
    public void Write(Mat frame) {
        if(videoWriter.isOpened()==false){
            videoWriter.release();
            throw new IllegalArgumentException("Video Writer Exception: VideoWriter not opened,"
                    + "check parameters.");
        }
        //Write video
        videoWriter.write(frame);
    }
    boolean saveToVideo = false;
    Mat lastFrame;
    int skipper = 0;
    boolean applyWatermark = false;
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
            if(saveToVideo){
                videoservice.Save(inputFrame.rgba());
            }
            return inputFrame.rgba();
    }
}