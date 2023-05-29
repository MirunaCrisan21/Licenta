package com.example.akshika.opencvtest.MirVideoFormat.WaterMarker;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * This Service dose not modify the the Mat objects at all.
 */
public class NoWaterMarker implements IWaterMarkingService{

    /**
     *  Doesn't process the frame at all.
     * @param mat The Mat that we want to waterMark.
     */
    @Override
    public void ApplyWaterMarking(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB);
    }

    /**
     *  Doesn't process the frame at all.
     */
    @Override
    public String IdentifyWaterMarking(Mat markedMat,Mat clearMat) {
        return "";
    }
}
