package com.reider745.voicechat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.reider745.voicechat.service.MicService;
import com.reider745.voicechat.service.NetworkService;
import com.reider745.voicechat.service.SpeakService;
import com.reider745.voicechat.service.impl.mic.MicAndroidApiServiceImpl;
import com.reider745.voicechat.service.impl.network.NetworkInnerCoreServiceImpl;
import com.reider745.voicechat.service.impl.network.NetworkSocketServiceImpl;
import com.reider745.voicechat.service.impl.speak.SpeakAndroidApiServiceImpl;
import com.zhekasmirnov.apparatus.multiplayer.Network;
import com.zhekasmirnov.apparatus.multiplayer.server.ConnectedClient;
import com.zhekasmirnov.horizon.runtime.logger.Logger;
import com.zhekasmirnov.innercore.api.NativeAPI;
import com.zhekasmirnov.innercore.api.mod.adaptedscript.AdaptedScriptAPI;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.Arrays;
import java.util.HashMap;

public class Voice {
    public static String dirMod;

    public static void boot(HashMap<?, ?> options) {
        Callback.invokeAPICallback("VoiceModBoot", options);

        dirMod = (String) options.get("dirMod");

        if(dirMod == null) {
            throw new IllegalArgumentException("Missing required parameter 'dirMod'");
        }

        while (ContextCompat.checkSelfPermission(AdaptedScriptAPI.UI.getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(AdaptedScriptAPI.UI.getContext(), new String[] {Manifest.permission.RECORD_AUDIO}, 200);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private MicService micService;
    private SpeakService speakService;
    private NetworkService networkService;

    private double distance;

    public Voice() {
        this(new MicAndroidApiServiceImpl(), new SpeakAndroidApiServiceImpl(), new NetworkSocketServiceImpl("127.0.0.1", Network.getSingleton().getConfig().getDefaultPort() + 1));
    }

    public Voice(MicService micService, SpeakService speakService, NetworkService networkService) {
        this.networkService = networkService;

        this.setSpeakService(speakService);
        this.setMicService(micService);

        Callback.addCallback("LevelDisplayed", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                micService.start();
                return null;
            }
        }, 0);

        Callback.addCallback("LocalLevelLeft", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                micService.stop();
                networkService.stop();
                return null;
            }
        }, 0);

        Callback.addCallback("ServerLevelPreLoaded", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                networkService.startServer();
                return null;
            }
        }, 0);
    }

    private void updateNetwork() {
        micService.setListener(((buff, length) -> networkService.sendToServer(buff, length)));

        networkService.setServerHandler(((client, buff, length) -> {
            for(ConnectedClient speakClient : Network.getSingleton().getServer().getConnectedClients()) {
                float[] pos1 = new float[3];
                float[] pos2 = new float[3];

                NativeAPI.getPosition(speakClient.getPlayerUid(), pos1);
                NativeAPI.getPosition(client.getPlayerUid(), pos2);

                if(/*speakClient != client && */Math.sqrt(Math.pow(pos1[0] - pos2[0], 2) + Math.pow(pos1[1] - pos2[1], 2) + Math.pow(pos1[2] - pos2[2], 2)) <= this.distance) {
                    networkService.sendToClient(speakClient, buff, length);
                }
            }
        }));

        networkService.setClientHandler(((buff, length) -> speakService.play(buff, length)));
    }

    public void setMicService(MicService micService) {
        if(this.micService != null && this.micService.isRecording()) {
            this.micService.stop();
            micService.start();
        }

        this.micService = micService;

        updateNetwork();
    }

    public void setNetworkService(NetworkService networkService) {
        this.networkService = networkService;

        updateNetwork();
    }

    public void setSpeakService(SpeakService speakService) {
        this.speakService = speakService;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    public MicService getMicService() {
        return micService;
    }

    public SpeakService getSpeakService() {
        return speakService;
    }

    public NetworkService getNetworkService() {
        return networkService;
    }
}
