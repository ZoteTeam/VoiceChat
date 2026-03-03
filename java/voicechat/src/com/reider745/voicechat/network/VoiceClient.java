package com.reider745.voicechat.network;

import com.reider745.voicechat.service.MicService;
import com.reider745.voicechat.service.SpeakService;
import com.reider745.voicechat.service.network.ClientNetworkService;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import lombok.Getter;
import lombok.Setter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

@Getter
public class VoiceClient {
    private ClientNetworkService clientNetworkService;
    private MicService micService;
    @Setter
    private SpeakService speakService;

    public VoiceClient(ClientNetworkService clientNetworkService, MicService micService, SpeakService speakService) {
        setClientNetworkService(clientNetworkService);
        setMicService(micService);
        setSpeakService(speakService);

        Callback.addCallback("LevelDisplayed", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                getMicService().start();
                return null;
            }
        }, 0);

        Callback.addCallback("LocalLevelLeft", new ScriptableFunctionImpl() {
            @Override
            public Object call(Context context, Scriptable scriptable, Scriptable scriptable1, Object[] objects) {
                getMicService().stop();
                getClientNetworkService().stop();
                return null;
            }
        }, 0);
    }

    public void setClientNetworkService(ClientNetworkService clientNetworkService) {
        clientNetworkService.setHandler((entry) -> {
            getSpeakService().play(entry.getSound(), entry.getSound().length);
        });

        clientNetworkService.setRefreshConfigHandler((config) -> {
            getMicService().refreshConfig(config);
            getSpeakService().refreshConfig(config);
        });

        this.clientNetworkService = clientNetworkService;
    }

    public void setMicService(MicService micService) {
        micService.setListener((buff, length) -> {
            getClientNetworkService().sendToServer(buff, length);
        });

        this.micService = micService;
    }
}
