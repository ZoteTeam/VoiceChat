package com.reider745.voicechat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.reider745.voicechat.config.ServerConfig;
import com.reider745.voicechat.network.LocalPlayerList;
import com.reider745.voicechat.network.VoiceClient;
import com.reider745.voicechat.network.VoiceServer;
import com.reider745.voicechat.processing.VoiceGainProcessing;
import com.reider745.voicechat.processing.VoiceJammingDistanceProcessing;
import com.reider745.voicechat.processing.VoiceNoiseReductionProcessing;
import com.reider745.voicechat.service.impl.mic.MicAndroidApiServiceImpl;
import com.reider745.voicechat.service.impl.speak.SpeakAndroidApiServiceImpl;
import com.reider745.voicechat.service.network.impl.SocketClientNetworkServiceImpl;
import com.reider745.voicechat.service.network.impl.SocketServerNetworkServiceImpl;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import com.zhekasmirnov.innercore.mod.build.Config;
import lombok.Getter;
import org.json.JSONObject;

import java.util.HashMap;

public class Voice {
    public static String dirMod;

    @Getter
    private static VoiceClient client;
    @Getter
    private static VoiceServer server;

    public static void boot(HashMap<?, ?> options) {
        Callback.invokeAPICallback("VoiceModBoot", options);

        dirMod = (String) options.get("dirMod");

        if (dirMod == null) {
            throw new IllegalArgumentException("Missing required parameter 'dirMod'");
        }

        if (AdaptedScriptAPI.isDedicatedServer())
            return;

        while (ContextCompat.checkSelfPermission(AdaptedScriptAPI.UI.getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AdaptedScriptAPI.UI.getContext(), new String[]{Manifest.permission.RECORD_AUDIO}, 200);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        client = new VoiceClient(new SocketClientNetworkServiceImpl(), new MicAndroidApiServiceImpl(), new SpeakAndroidApiServiceImpl());
        server = new VoiceServer(ServerConfig.builder().build(), new SocketServerNetworkServiceImpl());

        LocalPlayerList.init();
    }

    public static void refreshConfig(Config config) {
        try {
            final JSONObject json = new JSONObject();
            json.put("rate", server.getServerConfig().getRate());
            json.put("distance", server.getServerConfig().getDistance());
            json.put("host",  server.getServerConfig().getHost());
            json.put("port", server.getServerConfig().getPort());
            json.put("volume", 1);

            final JSONObject local = new JSONObject();

            local.put("gain", 1f);
            local.put("noise", true);

            json.put("local", local);

            json.put("players", new JSONObject());

            config.checkAndRestore(json);
        } catch (Exception ignore) {}

        server.setServerConfig(ServerConfig.builder()
                .rate(config.getInteger("rate"))
                .distance(config.getInteger("distance"))
                .host(config.getString("host"))
                .port(config.getInteger("port"))
                .build());

        client.getLocalProcessing().clear();
        client.getServerProcessing().clear();

        float gain = config.getFloat("local.gain");
        if(gain != 1) {
            client.getLocalProcessing().addGlobalProcessing(new VoiceGainProcessing(gain));
        }

        if(config.getBool("local.noise")) {
            client.getLocalProcessing().addGlobalProcessing(new VoiceNoiseReductionProcessing());
        }

        float volume = config.getFloat("volume");
        if(volume != 1) {
            client.getServerProcessing().addGlobalProcessing(new VoiceGainProcessing(volume));
        }

        client.getServerProcessing().addGlobalProcessing(new VoiceJammingDistanceProcessing());

        Object playersObject = config.get("players");
        if(playersObject instanceof Config) {
            Config cfg = (Config) playersObject;
            for(String nickname : cfg.getNames()) {
                float gainPlayer = cfg.getFloat(nickname);
                if(gainPlayer != 1) {
                    client.getServerProcessing().addProcessing(nickname, new VoiceGainProcessing(gainPlayer));
                }
            }
        }

        Callback.invokeAPICallback("VoiceModRefresh", config, client, server);
    }
}
