/* declare class DeclServerConfig {

}



declare class DeclSocketServerNetworkServiceImpl {
    constructor();
}

declare class DeclSocketClientNetworkServiceImpl {
    constructor();
}

declare class DeclMicAndroidApiServiceImpl {
    constructor();
}

declare class DeclVoiceClient {
}

declare class DeclVoiceServer {
    constructor(config: DeclServerConfig, server: DeclSocketServerNetworkServiceImpl);
}

interface ServerConfigBuilder {
    rate(rate: number): ServerConfigBuilder
    distance(distance: number): ServerConfigBuilder
    debug(debug: boolean): ServerConfigBuilder;
    host(host: string): ServerConfigBuilder
    port(port: number): ServerConfigBuilder

    build(): DeclServerConfig;
}

const VoiceClient: typeof DeclVoiceClient = new (WRAP_JAVA("com.reider745.voicechat.network.VoiceClient"))();
const VoiceServer: typeof DeclVoiceServer = new (WRAP_JAVA("com.reider745.voicechat.network.VoiceServer"))();

const ServerConfig: {builder(): ServerConfigBuilder} = WRAP_JAVA("com.reider745.voicechat.config.ServerConfig");

Voice.setDistance(__config__.getDouble("distance")); */

WRAP_JAVA("com.reider745.voicechat.Voice").refreshConfig(__config__);