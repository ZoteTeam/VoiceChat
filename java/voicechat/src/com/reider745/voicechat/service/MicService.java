package com.reider745.voicechat.service;

import com.reider745.voicechat.data.HandlerSound;

public interface MicService {
    void setListener(HandlerSound listener);

    void start();

    void stop();

    boolean isRecording();
}
