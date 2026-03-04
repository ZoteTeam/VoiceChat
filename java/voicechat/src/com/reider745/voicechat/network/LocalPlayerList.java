package com.reider745.voicechat.network;

import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import org.json.JSONArray;

import java.util.*;

public class LocalPlayerList {
    private static final byte[] EMPTY = new byte[0];
    private static final Set<Long> PLAYERS = new HashSet<>();

    static {
        Network.getSingleton().addClientPacket("voice.player_list.sync", (data, meta, aClass) -> {
            try {
                final JSONArray array = new JSONArray(data.toString());

                PLAYERS.clear();

                for (int i = 0; i < array.length(); i++) {
                    PLAYERS.add(array.getLong(i));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Network.getSingleton().addClientPacket("voice.player_list.add", (data, meta, aClass) -> {
            PLAYERS.add(Long.parseLong(meta));
        });

        Network.getSingleton().addClientPacket("voice.player_list.remove", (data, meta, aClass) -> {
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
}
