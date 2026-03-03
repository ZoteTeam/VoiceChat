package com.reider745.voicechat.service;

import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.data.HandlerSound;

public interface MicService {
    void setListener(HandlerSound listener);

    void start();

    void stop();

    void refreshConfig(ClientConfig clientConfig);

    boolean isRecording();
}
