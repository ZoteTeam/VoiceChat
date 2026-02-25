package com.reider745.voicechat.service.network.impl;

import com.reider745.voicechat.data.HandlerSoundClient;
import com.reider745.voicechat.service.network.ClientNetworkService;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class SocketClientNetworkServiceImpl implements ClientNetworkService {
    private static final NetworkJsAdapter ADAPTER = AdaptedScriptAPI.MCSystem.getNetwork();

    private SocketClientVoice clientVoice;
    private HandlerSoundClient handler = (buff, length) -> {};

    public SocketClientNetworkServiceImpl() {
        ADAPTER.addClientPacket("voice.request", (data, meta, type) -> {
            stop();

            if(data instanceof CharSequence) {
                String remoteHost = data.toString();
                if(remoteHost.equals("127.0.0.1") || remoteHost.equals("0.0.0.0")) {
                    remoteHost = NativeCallback.getStringParam("host");

                    if(remoteHost.isEmpty()) {
                        remoteHost = "0.0.0.0";
                    }
                }

                connect(remoteHost, Integer.parseInt(meta));
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
                        final byte[] bytes = clientVoice.handleClient();
                        if (bytes.length > 0) {
                            handler.apply(bytes, bytes.length);
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

        clientVoice.send(buff, length);
    }

    @Override
    public void setHandler(HandlerSoundClient listener) {
        this.handler = listener;
    }

    @Override
    public void stop() {
        if(clientVoice == null) return;
        clientVoice.close();
    }
}
