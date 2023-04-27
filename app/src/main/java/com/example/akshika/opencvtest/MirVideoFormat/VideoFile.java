package com.example.akshika.opencvtest.MirVideoFormat;

import org.opencv.core.Mat;

public class VideoFile{
    public static final String FILEFORMAT = "mrn";
    public static final int HEADERSIZE = 56; //bytes


    /**
     *
     * @param sourceArray The array from which we want the data.
     * @param offset The offset where the int starts from.
     */
    public static int GetIntFromByteArray(int offset,byte[] sourceArray)
    {
        int result =0;
        result+= sourceArray[offset+0] & 255;
        result = result << 8;
        result+= sourceArray[offset+1] & 255;
        result = result << 8;
        result+= sourceArray[offset+2] & 255;
        result = result << 8;
        result+= sourceArray[offset+3] & 255;
        return result;
    }

    /**
     *
     * @param i The int we want to insert
     * @param resultArray the array where we want it inserted into
     * @param offset the offset where we want it inserted
     */
    public static void  insertIntIntoByteArray(long i,int offset,byte[] resultArray)
    {
        resultArray[offset+0] = (byte) (i >> 56);
        resultArray[offset+1] = (byte) (i >> 48);
        resultArray[offset+2] = (byte) (i >> 40);
        resultArray[offset+3] = (byte) (i >> 32);
        resultArray[offset+4] = (byte) (i >> 24);
        resultArray[offset+5] = (byte) (i >> 16);
        resultArray[offset+6] = (byte) (i >> 8);
        resultArray[offset+7] = (byte) (i /*>> 0*/);
    }

    public static Mat CreateMatFromByteArray(byte[] array,int width, int heigth){
        Mat matrix = new Mat(width,heigth,0);
        for(int i=0;i<heigth;i++){
            for(int j=0;j<width;j++){
                matrix.put(i,j,GetPixel(array,i,j));
            }
        }

        return matrix;
    }

    private static double[] GetPixel(byte[] array, int line, int col){
        double[] pixel = new double[4];

        return pixel;
    }
}
