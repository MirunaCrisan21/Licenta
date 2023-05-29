package com.example.akshika.opencvtest.MirVideoFormat.WaterMarker;

import static org.opencv.imgproc.Imgproc.CV_RGBA2mRGBA;
import static org.opencv.imgproc.Imgproc.cvtColor;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class LeastSignificantBitWaterMarkingService implements IWaterMarkingService {
    private int[] encodedMessage;
    private String key;

    /**
     * Creates a new instances of a LSB watermarking Service where the message used will be the key.
     * @param key The message that will be used to encode.
     */
    public LeastSignificantBitWaterMarkingService(String key){
        encodedMessage = GetEncodedMessage(key);
        this.key = key;
    }

    /**
     * Converts string to int array where each int only consists of
     * @param message
     * @return
     */
    private int[] GetEncodedMessage(String message){
        // we get the number of bytes in the message we want to encode
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        // we prepare an array of int that will be added to the pixels where each array of an int consists of two bits
        int[] encodingValue = new int[bytes.length*4];
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

    public long GetSeed(){
        long result =0;
        for(int i=0;i<key.length();i++){
            result += (int)key.charAt(i);
        }
        return result;
    }

    /**
     * Apply LSB watermarking ti Mat based on the key passed to the constructor of the object.
     * @param mat The Mat that we want to waterMark.
     */
    @Override
    public void ApplyWaterMarking(Mat mat) {
        int addr=0;
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB);
        // Create a Random object with the seed
        Random random = new Random(GetSeed());
        for(int i=0;i<encodedMessage.length;i++){
            int line = random.nextInt(mat.height()) ;
            int column = random.nextInt(mat.width());
            double[] pixel = mat.get(line,column);
            for(int channel=0;channel<pixel.length;channel++){
                // we change last bits and move using addr trough the bits of the encoded message
                //pixel[channel] = 0;
                pixel[channel] = (pixel[channel]  + encodedMessage[addr] )% 256;

                //pixel[channel] = ((int)pixel[channel] ) +  encodedMessage[addr];
                // we change current bit position and reset in case of overshoot
                addr=++addr% encodedMessage.length;
            }
            mat.put(line,column,pixel);
        }
    }

    /**
     * Removes LSB watermarking from Mat based on the key passed to the consturctor of the object.
     */
    @Override
    public String IdentifyWaterMarking(Mat markedMat,Mat clearMat) {
        return "";
    }
}
