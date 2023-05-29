package com.example.akshika.opencvtest.MirVideoFormat.FileReaders;

import android.os.Message;

import com.example.akshika.opencvtest.MainActivity;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.NoWaterMarker;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.util.ArrayList;

public class SimpleFileReader extends IFileReader{
    private VideoCapture videoCapture1;
    public SimpleFileReader(ArrayList<String> path, MainActivity.MyFrameRunner runner) {
        super(path, runner,new NoWaterMarker());
    }

    @Override
    public void read() {
        // Open the second video file
        videoCapture1 = new VideoCapture(videoFile1);
        // Open the first video file

        if (!videoCapture1.isOpened()) {
            System.err.println("Error opening video files.");
            return;
        }

        // Iterate over the frames of the videos
        Mat frame1 = new Mat();

        while (videoCapture1.read(frame1)) {
            Message message =  runner.obtainMessage();
            message.what = 2;
            message.obj = frame1;
            runner.sendMessage(message);
        }

        // Release the video capture objects
        videoCapture1.release();
    }
    @Override
    public void close(){
        if(videoCapture1.isOpened())
            videoCapture1.release();
    }
}
