package com.example.akshika.opencvtest;

import android.os.Message;
import android.util.Log;

import com.example.akshika.opencvtest.MirVideoFormat.Messages.VideoSavingMessage;
import com.example.akshika.opencvtest.MirVideoFormat.VideoSaverWithWaterMarkingService;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.DirectCosineTransformWaterMarkingService;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.LeastSignificantBitWaterMarkingService;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.NoWaterMarker;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import java.io.IOException;
import java.util.ArrayList;


public class VideoSavingService extends android.os.Handler {
    private static final String VIDEO_FILE_PREFIX = "VID_";
    private static final String VIDEO_FILE_EXTENSION_WITH_DCT = "DCT.avi";
    private static final String VIDEO_FILE_EXTENSION_WITH_LSB = "LSB.avi";
    private static final String VIDEO_FILE_EXTENSION = ".avi";

    private ArrayList<VideoSaverWithWaterMarkingService> videoWriters = new ArrayList<>();

    public Size matSize;
    public String OutputPath;
    public String OutputPathWithLSB;
    public String OutputPathWithDCT;
    public VideoSavingService(String OutputPath, Size size){
        this.OutputPath = OutputPath+"/"+VIDEO_FILE_PREFIX+System.currentTimeMillis()+VIDEO_FILE_EXTENSION;
        this.OutputPathWithLSB = OutputPath+"/"+VIDEO_FILE_PREFIX+System.currentTimeMillis()+ VIDEO_FILE_EXTENSION_WITH_LSB;
        this.OutputPathWithDCT = OutputPath+"/"+VIDEO_FILE_PREFIX+System.currentTimeMillis()+ VIDEO_FILE_EXTENSION_WITH_DCT;
        matSize = size;
    }

    @Override
    public void handleMessage(Message msg) {
        switch(msg.what){
            // start to save a new file
            case 1:
                try {
                    VideoSavingMessage message = (VideoSavingMessage)msg.obj;
                    switch(message.Type){
                        case NoWaterMarking:
                            MakeVideo();
                            break;
                        case LSBWaterMarking:
                            MakeDoubleVideo(message.Message);
                            break;
                        case DCTWaterMarking:
                            MakeVideo();
                            break;
                        case DFTWaterMarking:

                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + message.Type);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
                //OpenVideoSaver((OpenVideoMessage)msg.obj);
            // we recieved a frame to save
            case 2:
                try{
                    Mat mat = (Mat)msg.obj;
                    for(int i=0;i<videoWriters.size();i++) {
                        videoWriters.get(i).write(mat);
                        //videoWriters.get(i).release();
                    }
                }
                catch (Exception ex){
                    Log.e("Saving image",ex.getMessage());
                }
                break;
            // we recieved a frame to save and process
            case 3:
                try{

                }
                catch (Exception ex){
                    Log.e("Saving image",ex.getMessage());
                }
                break;
            // stop the saving process and close file
            case 4:
                for(int i=0;i<videoWriters.size();i++)
                    videoWriters.get(i).release();
                break;
            default:

            break;
        }
        if (msg.what == 1) {
            // Check if the message has the correct message code

        }
    }

    /**
     * Stops safely closes videoWriters.
     */
    public void StopWritingVideo(){
        if(videoWriters.size()>0){
            for(int i=0;i<videoWriters.size();i++)
                videoWriters.get(i).release();
        }
        videoWriters.clear();
    }

    /**
     * Creates the file where the video and allows for frames to be saved one by one.
     */
    public void MakeDoubleVideo(String message) throws IOException {
        videoWriters.add(new VideoSaverWithWaterMarkingService(OutputPath,matSize,new NoWaterMarker()));
        videoWriters.add(new VideoSaverWithWaterMarkingService(OutputPathWithLSB,matSize,new LeastSignificantBitWaterMarkingService(message)));
        videoWriters.add(new VideoSaverWithWaterMarkingService(OutputPathWithDCT,matSize,new DirectCosineTransformWaterMarkingService(message)));
    }

    /**
     * Creates the file where the video and allows for frames to be saved one by one.
     */
    public void MakeVideo() throws IOException {
       videoWriters.add(new VideoSaverWithWaterMarkingService(OutputPath,matSize,new NoWaterMarker()));
    }

    public String GetWMNameOfFile(String filePath) {
        return filePath.replace(".avi","LSB.avi");
    }

    public String GetNameOfWMFile(String filePath) {
        return filePath.replace("LSB","");
    }

    public String GetDCTNameOfFile(String filePath) {
        return filePath.replace(".avi","DCT.avi");
    }

    public String GetNameOfDCTFile(String filePath) {
        return filePath.replace("DCT","");
    }

}
