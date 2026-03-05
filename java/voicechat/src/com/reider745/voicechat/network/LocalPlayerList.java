package com.reider745.voicechat.network;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import org.json.JSONArray;

import java.util.*;
import java.util.function.Consumer;

public class LocalPlayerList {
    private static final byte[] EMPTY = new byte[0];
    private static final Set<Long> PLAYERS = new HashSet<>();

    private static final List<Consumer<Long>> CONNECTIONS = new ArrayList<>();
    private static final List<Consumer<Long>> DISCONNECTIONS = new ArrayList<>();

    static {
        Network.getSingleton().addClientPacket("voice.player_list.sync", (data, meta, aClass) -> {
            try {
                final JSONArray array = new JSONArray(data.toString());

                PLAYERS.forEach(uid -> DISCONNECTIONS.forEach(func -> func.accept(uid)));
                PLAYERS.clear();

                for (int i = 0; i < array.length(); i++) {
                    long uid = array.getLong(i);
                    CONNECTIONS.forEach(func -> func.accept(uid));
                    PLAYERS.add(uid);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Network.getSingleton().addClientPacket("voice.player_list.add", (data, meta, aClass) -> {
            long uid = Long.parseLong(meta);
            CONNECTIONS.forEach(func -> func.accept(uid));
            PLAYERS.add(uid);
        });

        Network.getSingleton().addClientPacket("voice.player_list.remove", (data, meta, aClass) -> {
            long uid = Long.parseLong(meta);
            DISCONNECTIONS.forEach(func -> func.accept(uid));
            PLAYERS.remove(Long.parseLong(meta));
        });

        Network.getSingleton().getServer().addOnClientConnectedListener(client -> {
            final JSONArray playersObject = new JSONArray();

            Network.getSingleton().getServer().getConnectedPlayers().forEach(playersObject::put);

            client.send("voice.player_list.sync", playersObject.toString());

            Network.getSingleton().getServer().getConnectedClients().forEach(connectedClient -> {
               connectedClient.send("voice.player_list.add#" + client.getPlayerUid(), EMPTY);
            });
        });

        Network.getSingleton().getServer().addOnClientDisconnectedListener((client, message) -> {
            Network.getSingleton().getServer().getConnectedClients().forEach(connectedClient -> {
                connectedClient.send("voice.player_list.remove#" + client.getPlayerUid(), EMPTY);
            });
        });
    }

    public static void init() {}

    public static List<Long> getPlayers() {
        return new ArrayList<>(PLAYERS);
    }

    public static void addConnection(Consumer<Long> connection) {
        CONNECTIONS.add(connection);
    }

    public static void addDisconnection(Consumer<Long> disconnection) {
        DISCONNECTIONS.add(disconnection);
    }
}
