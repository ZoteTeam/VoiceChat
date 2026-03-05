package com.reider745.voicechat.network;

import com.reider745.voicechat.config.ClientConfig;
import com.reider745.voicechat.data.VoiceProcessingContext;
import com.reider745.voicechat.service.MicService;
import com.reider745.voicechat.service.SpeakService;
import com.reider745.voicechat.service.VoiceProcessingService;
import com.reider745.voicechat.service.impl.processing.VoiceProcessingServiceImpl;
import com.reider745.voicechat.service.network.ClientNetworkService;
import com.zhekasmirnov.innercore.api.mod.util.ScriptableFunctionImpl;
import com.zhekasmirnov.innercore.api.runtime.Callback;
import lombok.Getter;
import lombok.Setter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Getter
public class VoiceClient {
    private final Map<Long, SpeakService> speakServices = new ConcurrentHashMap<>();

    private ClientNetworkService clientNetworkService;
    private MicService micService;
    @Setter
    private VoiceProcessingService localProcessing = new VoiceProcessingServiceImpl(), serverProcessing = new VoiceProcessingServiceImpl();

    private ClientConfig clientConfig;

    public VoiceClient(ClientNetworkService clientNetworkService, MicService micService, Function<Long, SpeakService> speakService) {
        setClientNetworkService(clientNetworkService);
        setMicService(micService);

        LocalPlayerList.addConnection((uid) -> {
            speakServices.put(uid, speakService.apply(uid));
        });

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
            final VoiceProcessingContext context = new VoiceProcessingContext(entry.getUsername(), entry.getSound());
            serverProcessing.process(context);

            final SpeakService speakService = this.speakServices.get(context.getPlayerUid());
            if (speakService != null) {
                speakService.play(context.getVoice(), context.getLength(), context.getLeftGain(), context.getRightGain());
            }
        });

        clientNetworkService.setRefreshConfigHandler((config) -> {
            clientConfig = config;

            getMicService().refreshConfig(config);
            speakServices.forEach((uid, speakService) -> {
                speakService.refreshConfig(config);
            });
        });

        this.clientNetworkService = clientNetworkService;
    }

    public void setMicService(MicService micService) {
        micService.setListener((buff, length) -> {
            final VoiceProcessingContext context = new VoiceProcessingContext(null, buff, length);
            localProcessing.process(context);
            if(context.isValid()) {
                getClientNetworkService().sendToServer(context.getVoice(), context.getLength());
            }
        });

        this.micService = micService;
    }
}
