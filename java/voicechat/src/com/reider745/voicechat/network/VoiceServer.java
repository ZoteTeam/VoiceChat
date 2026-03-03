package com.reider745.voicechat.network;

import com.reider745.voicechat.config.ServerConfig;
import com.reider745.voicechat.service.network.ServerNetworkService;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import lombok.Getter;
import lombok.Setter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

@Getter
public class VoiceServer {
    private ServerNetworkService serverNetworkService;
    @Setter
    private ServerConfig serverConfig;

    public VoiceServer(ServerConfig serverConfig, ServerNetworkService serverNetworkService) {
        this.serverConfig = serverConfig;

        this.setServerNetworkService(serverNetworkService);

        Callback.addCallback("ServerLevelPreLoaded", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                getServerNetworkService().start(serverConfig);
                return null;
            }
        }, 0);

        Callback.addCallback("ServerLevelLeft", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                getServerNetworkService().stop();
                return null;
            }
        }, 0);
    }

    public void setServerNetworkService(ServerNetworkService serverNetworkService) {
        serverNetworkService.setHandler(((client, buff, length) -> {
            for(ConnectedClient speakClient : Network.getSingleton().getServer().getConnectedClients()) {
                float[] pos1 = new float[3];
                float[] pos2 = new float[3];

                NativeAPI.getPosition(speakClient.getPlayerUid(), pos1);
                NativeAPI.getPosition(client.getPlayerUid(), pos2);

                if((serverConfig.isDebug() || speakClient != client) && Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2) + Math.pow(pos1[2] - pos2[2], 2)) <= this.serverConfig.getDistance()) {
                    serverNetworkService.sendToClient(speakClient, buff, length);
                }
            }
        }));

        this.serverNetworkService = serverNetworkService;
    }
}
