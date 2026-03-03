package com.reider745.voicechat.service.network.impl;

import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.data.HandlerSoundClient;
import com.reider745.voicechat.data.VoiceEntry;
import com.reider745.voicechat.service.network.ClientNetworkService;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClientNetworkServiceImpl implements ClientNetworkService {
    private static final NetworkJsAdapter ADAPTER = AdaptedScriptAPI.MCSystem.getNetwork();

    private SocketClientVoice clientVoice;
    private HandlerSoundClient handler = (entry) -> {};
    private Consumer<ClientConfig> refreshConfig = (config) -> {};

    public SocketClientNetworkServiceImpl() {
        ADAPTER.addClientPacket("voice.request", (data, meta, type) -> {
            stop();

            if(data instanceof CharSequence) {
                try {
                    final ClientConfig clientConfig = new ClientConfig(new JSONObject(data.toString()));

                    this.refreshConfig.accept(clientConfig);

                    String remoteHost = clientConfig.getHost();
                    if (remoteHost.equals("127.0.0.1") || remoteHost.equals("0.0.0.0")) {
                        remoteHost = NativeCallback.getStringParam("host");

                        if (remoteHost.isEmpty()) {
                            remoteHost = "0.0.0.0";
                        }
                    }

                    connect(remoteHost, clientConfig.getPort());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    public void connect(String host, int port) {
        Logger.info("VoiceMod", "New socket request connection " + host + ":" + port);

        try {
            final Socket socket = new Socket(host, port);
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            final DataInputStream in = new DataInputStream(socket.getInputStream());

            out.writeLong(Network.getSingleton().getClient().getPlayerUid());
            out.flush();

            this.clientVoice = new SocketClientVoice(socket, in, out);

            final Thread thread = new Thread(() -> {
                while(clientVoice != null) {
                    try {
                        final VoiceEntry voiceEntry = clientVoice.handleClient();
                        if (voiceEntry != null && voiceEntry.getSound().length > 0) {
                            handler.apply(voiceEntry);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    Thread.yield();
                }
            });
            thread.setName("voice-speak");
            thread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendToServer(short[] buff, int length) {
        if(clientVoice == null) return;

        clientVoice.sendToServer(buff, length);
    }

    @Override
    public void setHandler(HandlerSoundClient listener) {
        this.handler = listener;
    }

    @Override
    public void setRefreshConfigHandler(Consumer<ClientConfig> handler) {
        this.refreshConfig = handler;
    }

    @Override
    public void stop() {
        if(clientVoice == null) return;
        clientVoice.close();
        clientVoice = null;
    }
}
