package com.reider745.voicechat.service.network.impl;

import com.reider745.voicechat.data.HandlerSoundServer;
import com.reider745.voicechat.service.network.ServerNetworkService;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServerNetworkServiceImpl implements ServerNetworkService {
    private static final NetworkJsAdapter ADAPTER = AdaptedScriptAPI.MCSystem.getNetwork();
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    private final String host;
    private final int port;
    private final Map<ConnectedClient, SocketClientVoice> connectedClients = new ConcurrentHashMap<>();

    private ServerSocket serverSocket;
    private HandlerSoundServer handler = (client, buff, length) -> {};

    public SocketServerNetworkServiceImpl(String host, int port) {
        this.host = host;
        this.port = port;

        Network.getSingleton().getServer().addOnClientDisconnectedListener((client, disconnected) -> {
            SocketClientVoice clientVoice = connectedClients.remove(client);
            if(clientVoice != null) {
                clientVoice.close();
            }
        });

        Network.getSingleton().getServer().addOnClientConnectedListener((client) -> {
            client.send("voice.request#" + port, host);
        });
    }

    @Override
    public void sendToClient(ConnectedClient client, byte[] buff, int length) {
        if(serverSocket == null) return;

        final SocketClientVoice clientVoice = connectedClients.get(client);
        if(clientVoice != null) {
            clientVoice.send(buff, length);
        }
    }

    @Override
    public void setHandler(HandlerSoundServer listener) {
        this.handler = listener;
    }

    @Override
    public void start() {
        try {
            this.serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));

            Thread thread = new Thread(() -> {
                while (this.serverSocket != null) {
                    try {
                        final Socket socket = this.serverSocket.accept();

                        if(socket != null) {
                            final DataInputStream is = new DataInputStream(socket.getInputStream());
                            final DataOutputStream io = new DataOutputStream(socket.getOutputStream());

                            executor.submit(() -> {
                                try {
                                    while(is.available() <= 0) {Thread.yield();}

                                    long playerUid = is.readLong();
                                    ConnectedClient client = ADAPTER.getClientForPlayer(playerUid);

                                    if(client != null) {
                                        ICLog.i("VoiceMod", "new connected client: " + client.getPlayerUid());
                                        this.connectedClients.put(client, new SocketClientVoice(socket, is, io));
                                    } else {
                                        ICLog.i("VoiceMod", "new error connected  client: " + playerUid);
                                    }
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } catch (IOException e) {
                        ICLog.e("VoiceMod", "connection voice error", e);
                    }
                }
            });
            thread.setName("handle-voice-connection");
            thread.start();

            thread = new Thread(() -> {
                while (this.serverSocket != null) {
                    connectedClients.forEach((client, voice) -> {
                        byte[] bytes = voice.handleServer();
                        if(bytes.length > 0) {
                            handler.apply(client, bytes, bytes.length);
                        }
                    });

                    Thread.yield();
                }

            });
            thread.setName("handle-voices");
            thread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        if(this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.serverSocket = null;
        }
    }
}
