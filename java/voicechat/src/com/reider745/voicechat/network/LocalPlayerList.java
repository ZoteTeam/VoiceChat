package com.reider745.voicechat.network;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.innercore.api.NativeAPI;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LocalPlayerList {
    private static final byte[] EMPTY = new byte[0];

    private static final Map<Long, String> PLAYERS = new ConcurrentHashMap<>();

    private static final List<Consumer<Long>> CONNECTIONS = new ArrayList<>();
    private static final List<Consumer<Long>> DISCONNECTIONS = new ArrayList<>();

    static {
        Network.getSingleton().addClientPacket("voice.player_list.sync", (data, meta, aClass) -> {
            try {
                final JSONArray array = new JSONArray(data.toString());

                PLAYERS.forEach((uid, username) -> DISCONNECTIONS.forEach(func -> func.accept(uid)));
                PLAYERS.clear();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject player = array.getJSONObject(i);
                    long playerUid = player.getLong("uid");

                    CONNECTIONS.forEach(func -> func.accept(playerUid));
                    PLAYERS.put(playerUid, player.getString("username"));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Network.getSingleton().addClientPacket("voice.player_list.add", (data, meta, aClass) -> {
            long uid = Long.parseLong(meta);
            CONNECTIONS.forEach(func -> func.accept(uid));
            PLAYERS.put(uid, data.toString());
        });

        Network.getSingleton().addClientPacket("voice.player_list.remove", (data, meta, aClass) -> {
            long uid = Long.parseLong(meta);
            DISCONNECTIONS.forEach(func -> func.accept(uid));
            PLAYERS.remove(Long.parseLong(meta));
        });

        Network.getSingleton().getServer().addOnClientConnectedListener(client -> {
            final JSONArray playersObject = new JSONArray();

            Network.getSingleton().getServer().getConnectedPlayers().forEach(uId -> {
                try {
                    final JSONObject playerObject = new JSONObject();

                    playerObject.put("uid", uId);
                    playerObject.put("username", NativeAPI.getNameTag(uId));

                    playersObject.put(playerObject);
                } catch (Exception ignore) {}
            });

            client.send("voice.player_list.sync", playersObject.toString());

            Network.getSingleton().getServer().getConnectedClients().forEach(connectedClient -> {
               connectedClient.send("voice.player_list.add#" + client.getPlayerUid(), NativeAPI.getNameTag(client.getPlayerUid()));
            });
        });

        Network.getSingleton().getServer().addOnClientDisconnectedListener((client, message) -> {
            Network.getSingleton().getServer().getConnectedClients().forEach(connectedClient -> {
                connectedClient.send("voice.player_list.remove#" + client.getPlayerUid(), EMPTY);
            });
        });
    }

    public static void init() {}

    public static Map<Long, String> getPlayers() {
        return new HashMap<>(PLAYERS);
    }

    public static long getPlayerUid(String username) {
        for(Map.Entry<Long, String> entry : PLAYERS.entrySet()) {
            if(entry.getValue().equals(username)) {
                return entry.getKey();
            }
        }

        return 0;
    }

    public static void addConnection(Consumer<Long> connection) {
        CONNECTIONS.add(connection);
    }

    public static void addDisconnection(Consumer<Long> disconnection) {
        DISCONNECTIONS.add(disconnection);
    }
}
