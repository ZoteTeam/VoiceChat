package com.reider745.voicechat.service;

import com.reider745.voicechat.config.ClientConfig;

public interface SpeakService {
    void refreshConfig(ClientConfig clientConfig);

    void play(byte[] buff, int length);
}
