package com.example.akshika.opencvtest.MirVideoFormat.WaterMarker;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class DirectCosineTransformWaterMarkingService implements IWaterMarkingService{
    private String key;
    private Random random;
    public DirectCosineTransformWaterMarkingService(String key){
        this.key = key;

        // Create a Random object with the seed
        random = new Random(GetSeed());
    }
    public long GetSeed(){
        long result =0;
        for(int i=0;i<key.length();i++){
            result += (int)key.charAt(i);
        }
        return result;
    }

    @Override
    public void ApplyWaterMarking(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2RGB);
        for(int i=0;i<key.length();i++){
            int line = random.nextInt(mat.height()) ;
            int column = random.nextInt(mat.width());
            PutInMat(mat,line,column,key.charAt(i));
        }
    }

    private void PutInMat(Mat target,int lineOffSet, int columnOffSet,char charaterToApply) {
        Mat mat = matToAdd(charaterToApply);
        for (int i = 0; i < 8; i++) {
            int j=0;
            double[] pixel = target.get(lineOffSet + i, lineOffSet + j);
            double[] wm = mat.get(i, j);
            for (int k = 0; k < 3; k++) {
                    pixel[k] = (pixel[k] + wm[0]/25);
                    if(pixel[k]>256){
                        pixel[k] -=256;
                    }
                //result[k] = 200;
            }
            target.put(lineOffSet + i, columnOffSet + j, pixel);

        }
        for (int j = 0; j < 8; j++) {
            int i=0;
            double[] pixel = target.get(lineOffSet + i, lineOffSet + j);
            double[] wm = mat.get(i, j);
            for (int k = 0; k < 3; k++) {
                    pixel[k] = (pixel[k] + wm[0]);
                    if(pixel[k]>256){
                        pixel[k] -=256;
                    }

                }
                //result[k] = 200;

            target.put(lineOffSet + i, columnOffSet + j, pixel);

        }
    }

    private Mat matToAdd(char charaterToApply) {
        Mat result;
        if(charaterToApply>='A' && charaterToApply<='Z'){
            charaterToApply = (char) (charaterToApply-'A'+'a');
        }
        if(charaterToApply>='a' && charaterToApply<='z'){
            int offset = charaterToApply-'a';
            Range rowrange = new Range(offset/6*8,offset/6*8+8);
            Range colRange = new Range(offset%6*8,offset%6*8+8);
            result = cosineMat.submat(rowrange,colRange);
        }else{
            Range rowrange = new Range(0,8);
            Range colRange = new Range(0,8);
            result = cosineMat.submat(rowrange,colRange);
        }
        return result;
    }
    private Mat cosineMat = getCosineMat();

    @Override
    public String IdentifyWaterMarking(Mat markedMat,Mat clearMat) {
        String res = "";
        for(int i=0;i<key.length();i++){
            int line = random.nextInt(markedMat.height()) ;
            int column = random.nextInt(markedMat.width());
            res+= GetChar(markedMat,clearMat,line,column);
        }
        return  res;
    }

    private char GetChar(Mat markedMat, Mat clearMat, int line, int column) {
        Mat miinmarked = new Mat();
        Imgproc.cvtColor(markedMat.submat(new Range(line,line+8),new Range(column,column+8)), miinmarked, Imgproc.COLOR_RGB2GRAY);

        Mat miinclear = new Mat();
        Imgproc.cvtColor(clearMat.submat(new Range(line,line+8),new Range(column,column+8)), miinclear, Imgproc.COLOR_RGB2GRAY);
        Mat diff = new Mat(8,8,CvType.CV_8UC1);
        Core.subtract(miinmarked,miinclear,diff);

        // Convert Mat to Bitmap
        Bitmap bitmap = Bitmap.createBitmap(diff.cols(), diff.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(diff, bitmap);
        for(int charaterToApply='a';charaterToApply<='z';charaterToApply++){
            Mat mat = matToAdd((char)charaterToApply);
            if(IsInMatch(diff,mat,200)){
                return (char)charaterToApply;
            }
            return 'a';
        }

        return 0;
    }

    private boolean IsInMatch(Mat inputMat, Mat referenceMat, int tolerance){
        Mat diff = new Mat();
        Core.subtract(referenceMat,inputMat,diff);
        // Convert Mat to Bitmap
        Bitmap bitmap = Bitmap.createBitmap(diff.cols(), diff.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(diff, bitmap);

        int result = 0;
        for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
                result += diff.get(i,j)[0];
            }
        }
        return result<=tolerance;
    }


    private static Mat getCosineMat(){
        Mat mat = new Mat(64,64, CvType.CV_8UC1,new Scalar(0));

        for(int i=0;i<64;i+=8){
            for( int j=0;j<64;j+=8)
            {
                for(int subLine = 0;subLine < 8;subLine++){
                    for(int subColumn = 0;subColumn < 8;subColumn++){
                        int val = (int)(128+64*Math.cos((subLine*i)*Math.PI/64)+64*Math.cos((subColumn*j)*Math.PI/64));
                        mat.put(i+subLine,j+subColumn,(int)(val/25));
                    }
                }
            }
        }

        return mat;
    }

}
