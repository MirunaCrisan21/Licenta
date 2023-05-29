package com.example.akshika.opencvtest.MirVideoFormat.FileReaders;

import android.graphics.Bitmap;
import android.os.Message;

import com.example.akshika.opencvtest.MainActivity;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.IWaterMarkingService;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class DualFileReader extends IFileReader{
    private String videoFile2;
    private int[] encodedMessage;
    private String key;
    private  VideoCapture videoCapture2;

    private VideoCapture videoCapture1;

    public DualFileReader(ArrayList<String> path, MainActivity.MyFrameRunner runner, IWaterMarkingService waterMarkingService){
        super(path,runner,waterMarkingService);
        videoFile2 = path.get(1);
        key = path.get(2);
        encodedMessage = GetEncodedMessage(key);
    }

    @Override
    public void read(){
        // Open the second video file
        videoCapture1 = new VideoCapture(videoFile1);
        // Open the first video file

        // Check if the video files opened successfully
        videoCapture2 = new VideoCapture(videoFile2);
        if (!videoCapture1.isOpened() || !videoCapture2.isOpened()) {
            System.err.println("Error opening video files.");
            return;
        }

        // Iterate over the frames of the videos
        Mat frame1 = new Mat();
        Mat frame2 = new Mat();
        boolean decoded = false;
        while (videoCapture1.read(frame1) && videoCapture2.read(frame2)) {
            // Do diff within frames
            Message message =  runner.obtainMessage();
            message.what = 2;
            if(!decoded) {
                // Convert Mat to Bitmap
                Bitmap bitmap = Bitmap.createBitmap(frame2.cols(), frame2.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(frame2, bitmap);

                String result = identifyWaterMarking(frame2,frame1);
                decoded = true;
            }
            message.obj = frame2;
            runner.sendMessage(message);
            try {
                Thread.sleep(1000/24);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // Release the video capture objects
        videoCapture1.release();
        videoCapture2.release();
    }
    @Override
    public void close(){
        if(videoCapture1.isOpened())
            videoCapture1.release();
        if(videoCapture2.isOpened())
            videoCapture2.release();
    }

    private long GetSeed(){
        long result =0;
        for(int i=0;i<key.length();i++){
            result += (int)key.charAt(i);
        }
        return result;
    }

    private String identifyWaterMarking(Mat wmMat,Mat clearMat){
        return waterMarkingService.IdentifyWaterMarking(wmMat,clearMat);

    }


    /**
     * Converts string to int array where each int only consists of
     * @param message
     * @return
     */
    private int[] GetEncodedMessage(String message){
        // we get the number of bytes in the message we want to encode
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);//string.length() * 4
        // we prepare an array of int that will be added to the pixels where each array of an int consists of two bits
        int[] encodingValue = new int[bytes.length*4]; // string.length()*16 so 16 ints per char
        // current index of encoded values
        int addr=0;

        for(int i=0;i<bytes.length;i++){
            for(int j=6;j>=0;j-=2){
                if(j>0)
                    encodingValue[addr++] = (bytes[i] & (0x03<<j))>>j;
                else
                    encodingValue[addr++] = bytes[i] & 0x03;
            }
        }
        return encodingValue;
    }

    private String DecodeMessage(int[] intArray){
        byte[] result = new byte[intArray.length/4];
        int bitaddr = 0;
        for(int i=0;i<intArray.length;i+=4){
            int c = 0x00;
            for(int j=0;j<4 && i+j<intArray.length;j++){
                c = c + (intArray[i+j]<<j*2);
            }
        result[bitaddr++] = (byte) c;
        }

        return new String(result,StandardCharsets.UTF_8);
    }
}
