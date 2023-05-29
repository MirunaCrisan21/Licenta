package com.example.akshika.opencvtest.MirVideoFormat.WaterMarker;

import org.opencv.core.Mat;

/**
 * High level interface of a WaterMarking Service.
 * This service is supposed to allow to Add or Remove waterMarks from Mat objects.
 */
public interface IWaterMarkingService  {

    /**
     * Apply waterMarking on the mat file.
     * The Mat object itself will be modified.
     * @param mat The Mat that we want to waterMark.
     */
    void ApplyWaterMarking(Mat mat);

    /**
     * remove watermarking from the mat object.
     * @param markedMat .
     * @param clearMat .
     */
    String IdentifyWaterMarking(Mat markedMat,Mat clearMat);
}