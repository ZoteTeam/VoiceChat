package com.reider745.voicechat.service.impl.processing;

import com.reider745.voicechat.data.VoiceProcessingContext;
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
    public void process(VoiceProcessingContext context) {
        final float originalGain = context.getGain();

        for (VoiceProcessing processing : globalProcessing) {
            processing.process(context);
        }

        for(VoiceProcessing processing : getProcessing(context.getUsername())) {
            processing.process(context);
        }

        final float gain = context.getGain();

        if(originalGain != context.getGain()) {
            final short[] voice = context.getVoice();

            for(int i = 0; i < context.getLength(); i++) {
                voice[i] = (short) Math.min(Math.max(voice[i] * gain, Short.MIN_VALUE), Short.MAX_VALUE);
            }
        }
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
