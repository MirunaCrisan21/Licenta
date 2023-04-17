package com.example.akshika.opencvtest;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;

public class VideoService {
    public VideoService(String path,Size size){
        this.path = path;
        outputPath = path+"/"+VIDEO_FILE_PREFIX+System.currentTimeMillis()+VIDEO_FILE_EXTENSION;
        matSize = size;
    }
    private String path;
    private static final String VIDEO_FILE_PREFIX = "VID_";
    private static final String VIDEO_FILE_EXTENSION = ".mp4";
    private static final int FRAME_RATE = 30;
    private static final int FRAME_WIDTH = 640;
    private static final int FRAME_HEIGHT = 480;
    private ArrayList<String> videos = new ArrayList<>();
    private Size matSize;
    private VideoWriter mVideoWriter;
    private Thread videoSavingThread = new Thread(new Runnable() {
        @Override
        public void run() {
            try{
                ThreadFunction();
            }
            catch (Exception ex){

            }
        }
    });
    private Queue<Mat> images = new PriorityQueue<>();
    private String outputPath;
    ReentrantLock lock = new ReentrantLock();
    private void ThreadFunction() throws Exception {
        if(outputPath== null){
            throw new Exception("outputPath cannot be null");
        }
        if(FRAME_RATE < 0 ){
            throw new Exception("frame_rate cannot be null");
        }
        if(FRAME_HEIGHT < 0 ){
            throw new Exception("frame_height cannot be null");
        }
        if(FRAME_WIDTH < 0 ){
            throw new Exception("frame_width cannot be null");
        }
        if(matSize == null){
            throw new Exception("mat size cannot be null");
        }

        // Create a VideoWriter object
        VideoWriter writer = new VideoWriter( outputPath, VideoWriter.fourcc('H', '2', '6', '4'),
                                                FRAME_RATE, matSize, true);

        while(true){
            lock.lock();
            try {
                // Critical section here
                if(!images.isEmpty())
                    mVideoWriter.write(images.poll());
            } finally {
                lock.unlock();
            }
        }
    };

    public void Save(Mat frame){
        lock.lock();
        images.add(frame);
        lock.unlock();
    }

    public void Close(){
        if(videoSavingThread.isAlive()){
            videoSavingThread.interrupt();
            if(mVideoWriter!= null && mVideoWriter.isOpened()){
                mVideoWriter.release();
                videos.add(outputPath);
                outputPath = path+VIDEO_FILE_PREFIX+System.currentTimeMillis()+VIDEO_FILE_EXTENSION;
            }
        }
    }

    public void MakeVideo(){
        if(!videoSavingThread.isAlive()){
            videoSavingThread.run();
        }
    }
    public static boolean process = false;
    static public int[] GetBinaryMessage(String message) {
        // we get the number of bytes in the message we want to encode
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        // we prepare an array of int that will be added to the pixels where each array of an int consists of two bits
        int[] encodingValue = new int[bytes.length*4];
        // current index of encoded values
        int addr=0;

        for(int i=0;i<bytes.length;i++){
            for(int j=0;j<8;j+=2){
                if(j>0)
                    encodingValue[addr++] = (bytes[i] & (2^j + 2^(j+1)))>>(2^j+1);
                else
                    encodingValue[addr++] = bytes[i] & 3;
            }
        }
        return encodingValue;
    }

    public Mat applyLSBWaterMark(Mat aInputFrame){

        // current index of encoded values
        int addr=0;
        String messageToEncode = "I love watermarking dolphins";
        // mesajul encodat
        int[] encodedMessage = GetBinaryMessage(messageToEncode);

        for(int i=0;i<aInputFrame.rows();i++){
            for(int j=0;j<aInputFrame.cols();j++){
                double[] pixel = aInputFrame.get(i,j);
                for(int channel=0;channel<pixel.length;channel++){
                    // we change last bits and move using addr trough the bits of the encoded message
                    pixel[channel] = ((int)pixel[channel] ) +  (encodedMessage[addr] << 30);
                    //pixel[channel] = ((int)pixel[channel] ) +  encodedMessage[addr];
                    // we change current bit position and reset in case of overshoot
                    addr=++addr% encodedMessage.length;
                }
                aInputFrame.put(i,j,pixel);
            }
        }


        return aInputFrame;
    }

}

