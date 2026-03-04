interface IClientNetworkService {
    isConnected(): boolean;
}

interface IMicService {
    start(): void;
    stop(): void;
    isRecording(): boolean;
}

interface IVoiceClient {
    getClientNetworkService(): IClientNetworkService;
    getMicService(): IMicService;
}

interface IVoice {
    getClient(): IVoiceClient;
    refreshConfig(config: Config): void;
}

const Voice: IVoice = WRAP_JAVA("com.reider745.voicechat.Voice");
const Color = android.graphics.Color;

Voice.refreshConfig(__config__);