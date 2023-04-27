package com.example.akshika.opencvtest.MirVideoFormat;

import android.provider.MediaStore;

import org.opencv.core.Mat;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class made to read from .mrn files where we store .Mat objects.
 */
public class VideoReader {
    private boolean keepOpen;
    private File targetFile;
    private FileInputStream fileStream;
    private MrnHeader header;
    private int offset = 0;

    public VideoReader(String path,boolean keepOpen) throws IOException {
        targetFile = new File(path);
        /*
        * if(!targetFile.exists()){
            throw new IOException("target file doesn't exists");
        }
        * */
        // VideoFile.FILEFORMAT
        if(!getExtensionTypeOfFile(path).equals("mrn"))
            //VideoFile.FILEFORMAT
            throw new IOException("Invalid file format it should be " + "mrn");
        this.keepOpen = keepOpen;
        ReadHeader();
    }

    /**
     * Gets the extension of a file
     * @param fileName The name of the file
     * @return The extension identified by being the string after the last '.'
     */
    private static String getExtensionTypeOfFile(String fileName){
        return fileName.substring(fileName.lastIndexOf('.')+1);

    }

    /**
     * Reads the first 56 bytes from the file and interprets it by the predefined structure of a '.mrn' header.
     */
    private void ReadHeader() throws IOException {
        int headersize = 56;
        //VideoFile.HEADERSIZE
        byte[] buffer = new byte[headersize];
        fileStream = new FileInputStream(targetFile);
        // read exactly the number of bytes we need into the buffer
        fileStream.read(buffer, 0, headersize);
        ReadHeaderInfoFromBytes(buffer);

        offset+=56;
        if(!this.keepOpen){
            fileStream.close();
        }
    }

    /**
     * Converts the bytes into ints and extracts the info about the video.
     * @param buffer The first 56 bytes of a '.mrn' file.
     */
    private void ReadHeaderInfoFromBytes(byte[] buffer) throws IOException {
        //VideoFile.HEADERSIZE
        if(buffer.length!= 56){
            throw new IOException("header size was wrong");
        }
        header = new MrnHeader(buffer);
    }

    /**
     * Reads the entire video file and returns all the images in a list. It resets offset at the end.
     * @return A list of frames that are the entire video.
     */
    public List<Mat> GetAllImages() throws IOException {
        List<Mat> video = new ArrayList<>();
        if(header==null)
            throw new IOException("lease read header first");
        FileInputStream inputStream = new FileInputStream(targetFile);
        if(header.numberOfStreams == 0){
            byte[] image = new byte[header.bufferSizeInBytes];
            // a double is 4 bytes
            // there are 4 doubles per pixel
            for(int i=0;i<56+header.bufferSizeInBytes;i++){
                inputStream.read(image,offset,header.bufferSizeInBytes);
                offset+= header.bufferSizeInBytes;
                video.add(VideoFile.CreateMatFromByteArray(image,header.width, header.height));
            }
            inputStream.close();
            //VideoFile.HEADERSIZE
            offset = 56;
            return video;
        }
        else{
            throw new IOException("Not yet implemented.");
        }

    }

    /**
     * Opens target file reads the next frame and closes file.
     * @return The next frame in video.
     */
    public Mat ReadNext() throws IOException {
        if(header==null)
            throw new IOException("lease read header first");
        if(!keepOpen)
            fileStream = new FileInputStream(targetFile);
        if(header.numberOfStreams == 0){
            byte[] image = new byte[header.bufferSizeInBytes];
            int read = fileStream.read(image, offset, header.bufferSizeInBytes);
            if(read< header.bufferSizeInBytes){
                throw new EOFException();
            }
            offset+= header.bufferSizeInBytes;
            if(!keepOpen)
                fileStream.close();

            return VideoFile.CreateMatFromByteArray(image,header.width, header.height);
        }
        else{
            throw new IOException("Not yet implemented.");
        }

    }

    /**
     * Closes the stream.
     */
    public void Close() throws IOException {
        if(keepOpen){
            fileStream.close();
        }
    }

    public boolean ReadAnotherFrame(Mat frame) throws IOException {
        if(!keepOpen){
            throw new IOException("Keep the stream open.");
        }
        try{
            ReadNext().copyTo(frame);
        }catch (EOFException ex){
            return false;
        }

        return true;
    }
}
