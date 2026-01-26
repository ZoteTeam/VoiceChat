package com.reider745.voicechat.service;

import com.reider745.voicechat.data.HandlerSound;
import com.reider745.voicechat.data.HandlerSoundServer;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;

public interface NetworkService {
    void sendToServer(short[] buff, int length);

    void sendToClient(ConnectedClient client, byte[] buff, int length);

    void setClientHandler(HandlerSound listener);

    void setServerHandler(HandlerSoundServer listener);

    void startServer();

    void stop();
}
