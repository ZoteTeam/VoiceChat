package com.reider745.voicechat.config;

import com.reider745.voicechat.data.Constants;
import com.zhekasmirnov.apparatus.adapter.innercore.EngineConfig;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ServerConfig {
    @Builder.Default
    private int rate = Constants.RATE_DEFAULT;
    @Builder.Default
    private int distance = 32;

    @Builder.Default
    private boolean debug = EngineConfig.isDeveloperMode();

    @Builder.Default
    private String host = "0.0.0.0";
    @Builder.Default
    private int port = Network.getSingleton().getConfig().getDefaultPort() + 1;
}
