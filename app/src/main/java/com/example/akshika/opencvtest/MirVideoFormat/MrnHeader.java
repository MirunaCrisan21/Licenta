package com.example.akshika.opencvtest.MirVideoFormat;


/**
 * The header of a .mrn object.
 */
public class MrnHeader {
    public int delayBetweenFramesInMs;
    public int dataRate;
    public int waterMarking;
    public int EncodedMessageSize;
    public int numberOfFrames;
    public int numberPreviewOfFrames;
    public int numberOfStreams;
    public int bufferSizeInBytes;
    public int width;
    public int height;
    public int timeScale;
    public int dateRate;
    public int startingTime;
    public int sizeOfAviChinkInTimeScaleUnits;
    public MrnHeader(byte[] buffer){
        delayBetweenFramesInMs = VideoFile.GetIntFromByteArray(0,buffer);
        dataRate = VideoFile.GetIntFromByteArray(4,buffer);
        waterMarking = VideoFile.GetIntFromByteArray(8,buffer);
        EncodedMessageSize = VideoFile.GetIntFromByteArray(12,buffer);
        numberOfFrames = VideoFile.GetIntFromByteArray(16,buffer);
        numberOfStreams = VideoFile.GetIntFromByteArray(20,buffer);
        numberPreviewOfFrames = VideoFile.GetIntFromByteArray(24,buffer);
        bufferSizeInBytes = VideoFile.GetIntFromByteArray(28,buffer);
        width = VideoFile.GetIntFromByteArray(32,buffer);
        height = VideoFile.GetIntFromByteArray(36,buffer);
        timeScale = VideoFile.GetIntFromByteArray(40,buffer);
        dateRate = VideoFile.GetIntFromByteArray(44,buffer);
        startingTime = VideoFile.GetIntFromByteArray(48,buffer);
        sizeOfAviChinkInTimeScaleUnits = VideoFile.GetIntFromByteArray(52,buffer);
    }

    /**
     * Converts the current header object into a 56 byte array to be saved in the file.
     * @return
     */
    public byte[] ToByteArray(){
        return CreateFileHeader(delayBetweenFramesInMs,dateRate,waterMarking,EncodedMessageSize,
                numberOfFrames,numberPreviewOfFrames,numberOfStreams,bufferSizeInBytes,
                width,height,timeScale,dateRate,startingTime,sizeOfAviChinkInTimeScaleUnits);
    }

    /**
     * <a href="https://www.filefix.org/format/avi.html">SOURCE</a>
     * AVI format contains a 56-byte header. The structure of AVI format is very normal. In this file contains two parts, a header followed by chunks of information. It contains metadata of the file such as file size, frame rates and similar characteristics whereas chunks store actual video and audio information.
     * @param delayBetweenFramesInMs time delay between frames in microseconds 1/fps basically
     * @param dataRate data rate of AVI data (frame rate = data rate / time scale)
     * @param waterMarking waterMarking style applied to the video.
     * @param EncodedMessageSize The size of the encoded message in bytes. (Applies to LSB)
     * @param numberOfFrames number of video frames
     * @param numberPreviewOfFrames number of preview frames in video
     * @param numberOfStreams number of data streams (1 or 2)
     * @param bufferSizeInBytes size of a frame in bytes with the encoding. The encoded message will be stored as a byte array at the beginning of the frame.
     * @param width width of video image in pixels
     * @param height height of video image in pixels
     * @param timeScale time scale, typically 30
     * @param dateRate data rate
     * @param startingTime starting time, typically 0
     * @param sizeOfAviChinkInTimeScaleUnits size of AVI data chunk in time scale units
     * @return byte[] returns the bytes of the header of the AVI file.
     */
    public static byte[] CreateFileHeader(int delayBetweenFramesInMs, int dataRate, int waterMarking, int EncodedMessageSize,
                                          int numberOfFrames, int numberPreviewOfFrames, int numberOfStreams, int bufferSizeInBytes,
                                          int width, int height, int timeScale, int dateRate, int startingTime, int sizeOfAviChinkInTimeScaleUnits){
        byte[] result = new byte[56];

        insertIntIntoByteArray(delayBetweenFramesInMs,0,result);
        insertIntIntoByteArray(dataRate,4,result);
        insertIntIntoByteArray(waterMarking,8,result);
        insertIntIntoByteArray(EncodedMessageSize,12,result);
        insertIntIntoByteArray(numberOfFrames,16,result);
        insertIntIntoByteArray(numberPreviewOfFrames,20,result);
        insertIntIntoByteArray(numberOfStreams,24,result);
        insertIntIntoByteArray(bufferSizeInBytes,28,result);
        insertIntIntoByteArray(width,32,result);
        insertIntIntoByteArray(height,36,result);
        insertIntIntoByteArray(timeScale,40,result);
        insertIntIntoByteArray(dateRate,44,result);
        insertIntIntoByteArray(startingTime,48,result);
        insertIntIntoByteArray(sizeOfAviChinkInTimeScaleUnits,52,result);

        return result;
    }

    /**
     *
     * @param i The int we want to insert
     * @param resultArray the array where we want it inserted into
     * @param offset the offset where we want it inserted
     */
    public static void  insertIntIntoByteArray(int i,int offset,byte[] resultArray)
    {
        resultArray[offset+0] = (byte) (i >> 24);
        resultArray[offset+1] = (byte) (i >> 16);
        resultArray[offset+2] = (byte) (i >> 8);
        resultArray[offset+3] = (byte) i;
    }
}
