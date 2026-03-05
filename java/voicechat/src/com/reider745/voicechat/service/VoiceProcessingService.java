package com.reider745.voicechat.service;

import com.reider745.voicechat.data.VoiceProcessingContext;
import com.reider745.voicechat.processing.VoiceProcessing;

import java.util.List;

public interface VoiceProcessingService {
    void addGlobalProcessing(VoiceProcessing processing);

    void addProcessing(String username, VoiceProcessing processing);

    List<VoiceProcessing> getGlobalProcessing();

    List<VoiceProcessing> getProcessing(String username);

    List<String> getUsernames();

    void process(VoiceProcessingContext context);

    void clearGlobalProcessing();

    void clearProcessing(String username);

    void clear();
}
