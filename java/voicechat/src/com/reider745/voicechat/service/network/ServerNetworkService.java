package com.reider745.voicechat.service.network;

import com.reider745.voicechat.config.ServerConfig;
import com.reider745.voicechat.data.HandlerSoundServer;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;

public interface ServerNetworkService {
    void sendToClient(ConnectedClient client, byte[] buff, int length);

    void setHandler(HandlerSoundServer listener);

    void start(ServerConfig config);

    void stop();
}
