package com.example.akshika.opencvtest.MirVideoFormat;

import android.util.Log;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * A class made to save to .mrn files where we store .Mat objects.
 */
public class VideoSaver {
    private File targetFile;
    private FileOutputStream fileStream;
    private boolean hasHeader = false;
    private int offset =0;
    private int hasAudio = 0;
    private int fps;
    private int width;
    private int heigth;
    private boolean keepOpen = false;

    /**
     * Creates a new instance of a videoSave. It can only save .mrn files.
     * @param path The path to the file including the name and '.mrn'
     * @param fps The desired frame rate
     * @param width Width of a frame
     * @param height Heigth of a frame
     */

    public VideoSaver(String path,int fps, int width, int height,boolean keepOpen) throws IOException {
        targetFile = new File(path);
        if(targetFile.exists()){
            throw new IOException("target file already exists");
        }
        else{
            if(!targetFile.createNewFile())
                throw new IOException("couldn't create file");
        }
        this.fps = fps;
        this.width = width;
        this.heigth = height;
        this.keepOpen = keepOpen;
        CreateHeader();

    }

    /**
     * Saves a frame into the video file.
     * @param frame The frame that is going to be converted into a byte array then saved.
     */
    public void Save(Mat frame) throws IOException {
        if(frame.width()!=width || frame.height()!=heigth){
            throw new IllegalArgumentException("Width and height are not respected");
        }
        if(!hasHeader){
            throw new IOException("Something went wrong with the header");
        }
        byte[] result = new byte[frame.width()*frame.height()*8*4];
        frame.get(0,0,result);
        Write(result);
    }

    /**
     * Writes an array to the previously targeted file.
     * @param data The data we want to save
     */
    private void Write(byte[] data) throws IOException {

        if(keepOpen){
            fileStream.write(data);
        }else{
            fileStream = new FileOutputStream(targetFile.getName(),true);
            fileStream.write(data);
            fileStream.close();
        }
    }

    /**
     * If the filestream is kept open it will close it.
     * @throws IOException
     */
    public void Close() throws IOException {
        if(keepOpen){
            fileStream.close();
        }
    }

    /**
     * Creates a standard header for the .mrn file based on our video format.
     */
    private void CreateHeader() throws IOException {
        hasHeader = true;
        // each pixel has 4 doubles so 4*8 bytes = 32 bytes.
        // each image has width*height in pixels.
        int bufferSizeInBytes = 4*8*width*heigth;
        byte[] header = MrnHeader.CreateFileHeader(0,0,0,0,0,0,
                0,bufferSizeInBytes,this.width,this.heigth,30,this.fps*30,0,1);
        if(keepOpen){
            fileStream = new FileOutputStream(targetFile);
        }
        Write(header);
    }

}
