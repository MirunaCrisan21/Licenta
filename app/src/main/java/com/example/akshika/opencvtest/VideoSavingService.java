package com.example.akshika.opencvtest;

import android.util.Log;

import com.example.akshika.opencvtest.MirVideoFormat.VideoReader;
import com.example.akshika.opencvtest.MirVideoFormat.VideoSaver;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
public class VideoSavingService {
    private static final String VIDEO_FILE_PREFIX = "VID_";
    private static final String VIDEO_FILE_EXTENSION = ".mrn";

    private VideoSaver videoSaver;

    private VideoReader videoReader;
    private static final int FRAME_RATE = 20;
    private Queue<String> videos = new LinkedList<>();
    public Size matSize;
    private Queue<Mat> images = new LinkedList<>();
    private String outputPath;
    private Thread videoSavingThread;
    private ReentrantLock lock = new ReentrantLock();
    public VideoSavingService(String OutputPath, Size size){
        this.OutputPath = OutputPath;
        outputPath = OutputPath+"/"+VIDEO_FILE_PREFIX+System.currentTimeMillis()+VIDEO_FILE_EXTENSION;
        matSize = size;
        videoSavingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ThreadFunction();
                }
                catch (Exception ex){

                }
            }
        });
    }
    public String OutputPath;

    /**
     * Creates a new videoSaver to a specific path.
     * @param path Path to output file.
     * @param fps The frame per second of the recorded video.
     * @param width The width of the video.
     * @param height The height of the video.
     * @param keepOpen True if you want the stream to the output file to be kept open.
     */
    public void OpenVideoSaver(String path,int fps, int width, int height,boolean keepOpen) throws IOException {
        if(videoSaver!=null){
            StopWritingVideo();
        }
        videoSaver = new VideoSaver(path,fps,width,height,keepOpen);
    }

    /**
     * Opens a new videoReader for a path.
     * @param path The path of the .mrn file.
     */
    public VideoReader OpenVideoReader(String path,boolean keepOpen) throws IOException {
        if(videoReader!=null){
            StopReadingVideo();
        }
        videoReader = new VideoReader(path,keepOpen);
        return videoReader;
    }

    /**
     * Safely stops videoReader.
     */
    public void StopReadingVideo(){
        if(videoReader!= null){
            try {
                videoReader.Close();
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        }
        videoReader = null;
    }

    /**
     * Stops safely closes videoWriter.
     */
    public void StopWritingVideo(){
        if(videoSaver!= null){
            try {
                videoSaver.Close();
            } catch (IOException e) {
                //throw new RuntimeException(e);
            }
        }
        videoSaver = null;
    }



    /**
     * Saves and processes frame to a .mrn in a different thread.
     * @param frame The frame that is going to be saved.
     * @param applyWatermark If true the watermark will be applied on the frame and than the modified frame will be saved.
     */
    public void Save(Mat frame,boolean applyWatermark){
        Thread thread = new Thread(new MyRunnable(frame,applyWatermark));
        thread.start();
    }

    /**
     * Stops the video writer and closes the stream to the file.
     */
    public void CloseVideo() throws IOException {
        videoSaver.Close();
    }

    /**
     * Creates the file where the video and allows for frames to be saved one by one.
     */
    public void MakeVideo() throws IOException {
        videoSaver = new VideoSaver(outputPath,30,(int)(long)matSize.width,(int)(long)matSize.height,true);
        videoSavingThread.start();
    }

    /**
     * Converts a message intro an array of ints to be used in the watermarking algorithm.
     * @param message The message we want to encode.
     * @return The array of ints that is going to be embedded in the frame.
     */
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

    /**
     * Applies Least Significant Bit watermarking on the input frame.
     * @param aInputFrame The frame that is going to be encoded.
     * @return Returns the encoded frame.
     */
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
                    //pixel[channel] = ((int)pixel[channel] ) +  (encodedMessage[addr] << 30);
                    pixel[channel] = ((int)pixel[channel] ) +  encodedMessage[addr];
                    // we change current bit position and reset in case of overshoot
                    addr=++addr% encodedMessage.length;
                }
                aInputFrame.put(i,j,pixel);
            }
        }

        return aInputFrame;
    }

    /**
     * The function that will write available images from the queue in the file when there are images available.
     * @throws Exception
     */
    private void ThreadFunction() throws Exception {
        if(outputPath== null){
            throw new Exception("outputPath cannot be null");
        }
        if(FRAME_RATE < 0 ){
            throw new Exception("frame_rate cannot be null");
        }
        if(matSize == null){
            throw new Exception("mat size cannot be null");
        }

        try {
            while(true){
                // Critical section here
                if(!images.isEmpty()){
                    lock.lock();
                    videoSaver.Save(images.poll());
                }
            }
            /*
            mVideoWriter = new VideoWriter();
            mVideoWriter.open(outputPath, VideoWriter.fourcc('H','2','6','4'),
                    FRAME_RATE, matSize, true);

            while(true){
                lock.lock();
                try {
                    // Critical section here
                    if(!images.isEmpty() && mVideoWriter.isOpened())
                        mVideoWriter.write(images.poll());
                } catch (Exception ex){
                    Log.e("Saving image",ex.getMessage());
                }
                finally {
                    lock.unlock();
                }
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }



    };

    /**
     * A class that will process each image on a different thread.
     */
    public class MyRunnable implements Runnable {
        private Mat frame;
        private boolean Watermark;
        public MyRunnable(Mat parameter, boolean applyWatermark) {
            // store parameter for later user
            frame = parameter;
            Watermark = applyWatermark;
        }
        public void run() {
            try{
                Mat image;
                if(Watermark)
                    image = applyLSBWaterMark(frame);
                else
                    image = frame;
                lock.lock();
                images.add(image);
            }
            catch (Exception ex){
                Log.e("Saving image",ex.getMessage());
            }
            finally{
                lock.unlock();
            }
        }
    }
}
