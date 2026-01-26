package com.reider745.voicechat.service.impl.network;

import com.reider745.voicechat.data.HandlerSound;
import com.reider745.voicechat.data.HandlerSoundServer;
import com.reider745.voicechat.service.NetworkService;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class NetworkInnerCoreServiceImpl implements NetworkService {
    private static final NetworkJsAdapter ADAPTER = AdaptedScriptAPI.MCSystem.getNetwork();

    private HandlerSound clientHandler;
    private HandlerSoundServer serverHandler;

    public NetworkInnerCoreServiceImpl() {
        ADAPTER.addClientPacket("voice.speak", (data, meta, type) -> {
            if(data instanceof byte[]) {
                final short[] bytes = decompress((byte[]) data);
                clientHandler.apply(bytes, bytes.length);
            }
        });

        ADAPTER.addServerPacket("voice.mic", (client, data, meta, type) -> {
            if(data instanceof byte[]) {
                final byte[] bytes = (byte[]) data;
                serverHandler.apply(client, bytes, bytes.length);
            }
        });
    }

    @Override
    public void sendToServer(short[] buff, int length) {
        ADAPTER.sendToServer("voice.mic", compress(Arrays.copyOfRange(buff, 0, length)));
    }

    @Override
    public void sendToClient(ConnectedClient client, byte[] buff, int length) {
        client.send("voice.speak", Arrays.copyOfRange(buff, 0, length));
    }

    @Override
    public void setClientHandler(HandlerSound listener) {
        this.clientHandler = listener;
    }

    @Override
    public void setServerHandler(HandlerSoundServer listener) {
        this.serverHandler = listener;
    }

    @Override
    public void startServer() {}

    @Override
    public void stop() {}

    private byte[] compress(short[] data) {
        final ByteBuffer buffer = ByteBuffer.allocate(data.length * 2);

        for(int i = 0; i < data.length; i++) {
            buffer.putShort(data[i]);
        }

        return buffer.array();
    }

    private short[] decompress(byte[] data) {
        short[] buffer = new short[data.length / 2];

        for(int i = 0; i < buffer.length; i ++) {
            buffer[i] = ByteBuffer.wrap(new byte[]{data[i * 2], data[i * 2 + 1]}).getShort();
        }

        return buffer;
    }
}
