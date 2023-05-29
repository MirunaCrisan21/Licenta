package com.example.akshika.opencvtest.MirVideoFormat;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;

import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.IWaterMarkingService;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VideoSaverWithWaterMarkingService {
    private int frames = 1;
    private String path;
    private FileOutputStream fileStream;
    private IWaterMarkingService service;

    private VideoWriter videoWriter ;
    public VideoSaverWithWaterMarkingService(String path,Size matSize, IWaterMarkingService service) throws IOException {
        this.service = service;
        this.path = path;
        this.videoWriter = new VideoWriter(
                path,
                VideoWriter.fourcc('M','J','P','G'),
                24,
                new Size( (int)(long)matSize.width, (int)(long)matSize.height));
        //this.videoWriter.release();
        //fileStream = new FileOutputStream(path,true);
        //writeDC();
    }

    private void writeDC() throws IOException {
        fileStream.write(0x30);
        fileStream.write(0x30);
        fileStream.write(0x64);
        fileStream.write(0x62);
    }

    public void write(Mat mat) throws IOException {
        service.ApplyWaterMarking(mat);
        videoWriter.write(mat);
        //videoWriter.release();
        /*
        * byte[] array = new byte[(int) (mat.total() * mat.elemSize())];
        mat.get(0, 0, array);
        fileStream.write(array);
        frames++;*/
    }

    public void release(){
        videoWriter.release();
        /*
        * try {
            fileStream.close();
            overrideNumberOfFrames();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        * */

    }

    private byte[] getLittleEndian(int number){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(number);
        byte[] byteArray = buffer.array();
        return byteArray;
    }

    private void overrideNumberOfFrames(){
        File fileInfo  = new File(path);
        // Open the file stream in read-write mode
        try (RandomAccessFile file = new RandomAccessFile(fileInfo, "rw")) {
            // Move the file pointer to the desired offset
            file.seek(48);
            // Write the new byte value
            file.write(getLittleEndian(frames));

            // Move the file pointer to the desired offset
            file.seek(140);
            // Write the new byte value
            file.write(getLittleEndian(frames));

            // Move the file pointer to the desired offset
            file.seek(176);
            // Write the new byte value
            file.write(getLittleEndian(0));

            // Move the file pointer to the desired offset
            file.seek(232);
            // Write the new byte value
            file.write(getLittleEndian(frames));

            // Move the file pointer to the desired offset
            file.seek(4);
            // Write the new byte value
            file.write(getLittleEndian((int)(fileInfo.length()/8-1)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
