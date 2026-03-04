package com.reider745.voicechat.service.network;

import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.data.HandlerSoundClient;

import java.util.function.Consumer;

public interface ClientNetworkService {
    void sendToServer(short[] buff, int length);

    void setHandler(HandlerSoundClient listener);

    void setRefreshConfigHandler(Consumer<ClientConfig> handler);

    boolean isConnected();

    void stop();
}
