package com.reider745.voicechat.service.network;

import com.reider745.voicechat.data.HandlerSoundClient;

public interface ClientNetworkService {
    void sendToServer(short[] buff, int length);

    void setHandler(HandlerSoundClient listener);

    void stop();
}
