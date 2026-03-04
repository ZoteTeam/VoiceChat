package com.reider745.voicechat.service.impl.processing;

import com.reider745.voicechat.processing.VoiceProcessing;
import com.reider745.voicechat.service.VoiceProcessingService;
import com.zhekasmirnov.apparatus.util.Java8BackComp;

import java.util.*;
import java.util.function.Function;

public class VoiceProcessingServiceImpl implements VoiceProcessingService {
    private final List<VoiceProcessing> globalProcessing = new ArrayList<>();
    private final Map<String, List<VoiceProcessing>> userProcessing = new HashMap<>();

    @Override
    public void addGlobalProcessing(VoiceProcessing processing) {
        globalProcessing.add(processing);
    }

    @Override
    public void addProcessing(String username, VoiceProcessing processing) {
        Java8BackComp.computeIfAbsent(userProcessing, username, (Function<String, List<VoiceProcessing>>) s -> new ArrayList<>())
                .add(processing);
    }

    @Override
    public List<VoiceProcessing> getGlobalProcessing() {
        return Collections.unmodifiableList(globalProcessing);
    }

    @Override
    public List<VoiceProcessing> getProcessing(String username) {
        return Collections.unmodifiableList(userProcessing.getOrDefault(username, Collections.emptyList()));
    }

    @Override
    public List<String> getUsernames() {
        return new ArrayList<>(userProcessing.keySet());
    }

    @Override
    public short[] process(String username, short[] voice) {
        for (VoiceProcessing processing : globalProcessing) {
            voice = processing.process(username, voice);
        }

        for(VoiceProcessing processing : getProcessing(username)) {
            voice = processing.process(username, voice);
        }

        return voice;
    }

    @Override
    public void clearGlobalProcessing() {
        globalProcessing.clear();
    }

    @Override
    public void clearProcessing(String username) {
        Java8BackComp.computeIfAbsent(userProcessing, username, (Function<String, List<VoiceProcessing>>) s -> Collections.emptyList())
                .clear();
    }

    @Override
    public void clear() {
        globalProcessing.clear();
        userProcessing.clear();
    }
}
