package com.example.akshika.opencvtest.MirVideoFormat.Messages;

import com.example.akshika.opencvtest.MirVideoFormat.WaterMarker.WaterMarkingType;

public class VideoSavingMessage {
    public final WaterMarkingType Type;
    public final String Message;
    public VideoSavingMessage(String message, WaterMarkingType type){
        this.Message = message;
        this.Type = type;
    }
}
