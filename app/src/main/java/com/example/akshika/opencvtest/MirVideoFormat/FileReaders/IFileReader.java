package com.example.akshika.opencvtest.MirVideoFormat.FileReaders;

import com.example.akshika.opencvtest.MainActivity;
import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.IWaterMarkingService;

import java.util.ArrayList;

public abstract class IFileReader {
    protected IWaterMarkingService waterMarkingService;
    protected String videoFile1;
    protected MainActivity.MyFrameRunner runner;
    public IFileReader(ArrayList<String> path, MainActivity.MyFrameRunner runner,IWaterMarkingService waterMarkingService){
        this.runner = runner;
        this.videoFile1 = path.get(0);
        this.waterMarkingService = waterMarkingService;
    }
    public abstract void read();
    public abstract void close();
}
