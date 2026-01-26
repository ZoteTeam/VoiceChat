package com.reider745.voicechat.service.impl.network;

import com.reider745.voicechat.data.HandlerSound;
import com.reider745.voicechat.data.HandlerSoundServer;
import com.reider745.voicechat.service.NetworkService;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.NetworkJsAdapter;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.innercore.api.NativeCallback;
import com.zhekasmirnov.innercore.api.log.ICLog;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkSocketServiceImpl implements NetworkService {
    private static class ClientVoice {
        private static final byte[] EMPTY = new byte[0];

        private Socket socket;
        private final DataInputStream inputStream;
        private final DataOutputStream outputStream;

        public ClientVoice(Socket socket, DataInputStream inputStream, DataOutputStream outputStream) {
            this.socket = socket;
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        public byte[] handle() {
            try {
                int length = inputStream.available();
                if(length > 0) {
                    byte[] bytes = new byte[length];
                    inputStream.readFully(bytes, 0, length);
                    return bytes;
                }
                return EMPTY;
            } catch (IOException e) {
                return EMPTY;
            }
        }

        public void send(byte[] buff, int length) {
            try {
                outputStream.write(buff, 0, Math.min(buff.length, length));
                outputStream.flush();
            } catch (IOException ignored) {}
        }
    }

    private static final NetworkJsAdapter ADAPTER = AdaptedScriptAPI.MCSystem.getNetwork();
    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    private final Map<ConnectedClient, ClientVoice> connectedClients = new ConcurrentHashMap<>();
    private volatile ServerSocket socketServer;
    private volatile ClientVoice clientVoice;
    private final String host;
    private final int port;

    private HandlerSound clientHandler;
    private HandlerSoundServer serverHandler;

    public NetworkSocketServiceImpl(String host, int port) {
        this.host = host;
        this.port = port;

        Network.getSingleton().getServer().addOnClientDisconnectedListener((client, disconnected) -> {
            ClientVoice clientVoice = connectedClients.remove(client);
            if(clientVoice != null) {
                try {
                    clientVoice.socket.close();
                } catch (IOException ignored) {}
            }
        });

        Network.getSingleton().getServer().addOnClientConnectedListener((client) -> {
            client.send("voice.request#" + port, host);
        });

        ADAPTER.addClientPacket("voice.request", (data, meta, type) -> {
            stop();

            if(data instanceof CharSequence) {
                String remoteHost = data.toString();
                if(remoteHost.equals("127.0.0.1")) {
                    remoteHost = NativeCallback.getStringParam("host");

                    if(remoteHost.isEmpty()) {
                        remoteHost = "0.0.0.0";
                    }
                }

                AdaptedScriptAPI.Logger.debug("VoiceMod", "New socket request connection " + remoteHost + ":" + meta);

                try {
                    final Socket socket = new Socket(remoteHost, Integer.parseInt(meta));
                    final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    final DataInputStream in = new DataInputStream(socket.getInputStream());

                    out.writeLong(Network.getSingleton().getClient().getPlayerUid());
                    out.flush();

                    this.clientVoice = new ClientVoice(socket, in, out);

                    final Thread thread = new Thread(() -> {
                        while(clientVoice != null) {
                            try {
                                int length = in.available();
                                if (length > 0){
                                    if(length % 2 != 0) {
                                        in.skip(1);
                                        length--;
                                    }
                                    short[] buff = new short[length / 2];
                                    for(int i = 0; i < buff.length; i ++) {
                                        buff[i] = in.readShort();
                                    }
                                    clientHandler.apply(buff, buff.length);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                Thread.sleep(1L);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    thread.setName("voice-speak");
                    thread.start();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void sendToServer(short[] buff, int length) {
        if(socketServer != null || clientVoice == null) return;

        try {
            for (short value : buff) {
                clientVoice.outputStream.writeShort(value);
            }
            clientVoice.outputStream.flush();
        } catch (IOException ignored) {}
    }

    @Override
    public void sendToClient(ConnectedClient client, byte[] buff, int length) {
        if(socketServer == null || clientVoice != null) return;

        final ClientVoice clientVoice = connectedClients.get(client);
        if(clientVoice != null) {
            clientVoice.send(buff, length);
        }
    }

    @Override
    public void startServer() {
        try {
            this.socketServer = new ServerSocket(port);

            Thread thread = new Thread(() -> {
                while (this.socketServer != null) {
                    try {
                        final Socket socket = this.socketServer.accept();

                        if(socket != null) {
                            final DataInputStream is = new DataInputStream(socket.getInputStream());
                            final DataOutputStream io = new DataOutputStream(socket.getOutputStream());

                            executor.submit(() -> {
                                try {
                                    while(is.available() > 0) {Thread.yield();}

                                    ConnectedClient client = ADAPTER.getClientForPlayer(is.readLong());

                                    if(client != null) {
                                        this.connectedClients.put(client, new ClientVoice(socket, is, io));
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
                while (this.socketServer != null) {
                    connectedClients.forEach((client, voice) -> {
                        byte[] bytes = voice.handle();
                        if(bytes.length > 0) {
                            serverHandler.apply(client, bytes, bytes.length);
                        }
                    });

                    try {
                        Thread.sleep(1L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
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
        if(this.socketServer != null) {
            try {
                this.socketServer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.socketServer = null;
        }
        if(this.clientVoice != null) {
            try {
                this.clientVoice.socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.clientVoice = null;
        }
    }

    @Override
    public void setClientHandler(HandlerSound listener) {
        this.clientHandler = listener;
    }

    @Override
    public void setServerHandler(HandlerSoundServer listener) {
        this.serverHandler = listener;
    }
}
