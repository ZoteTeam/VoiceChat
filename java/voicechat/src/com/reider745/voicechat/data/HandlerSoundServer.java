package com.reider745.voicechat.data;

import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;

public interface HandlerSoundServer {
    void apply(ConnectedClient client, byte[] buff, int length);
}
