/*namespace Voice {
    let MIC: MicService;
    let SPEAK: SpeakService;
    let NETWORK: NetworkService = new NetworkInnerCoreServiceImpl();

    export function setMic(mic: MicService): void {
        if(MIC != null && MIC.isRecording()) {
            MIC.stop();
            mic.start();
        }

        MIC = mic;

        updateNetwork();
    }

    export function setSpeak(speak: SpeakService): void {
        SPEAK = speak;
    }

    function updateNetwork(): void {
        MIC.setListener((buff, length) => {
            NETWORK.sendToServer(buff, length);
        });

        NETWORK.setServerHandler((client, buff, length) => {
            const clients = Network.getConnectedClients();

            for(const i in clients) {
                const speakClient = clients[i];

                if(speakClient != client && Entity.getDistanceToEntity(speakClient.getPlayerUid(), client.getPlayerUid()) <= __config__.getDouble("distance")) {
                    NETWORK.sendToClient(speakClient, buff, length);
                }
            }
        });

        NETWORK.setClientHandler((buff, length) => {
            SPEAK.play(buff, length);
        });
    }

    export function setNetwork(network: NetworkService): void {
        NETWORK = network;

        updateNetwork();
    }

    setSpeak(new SpeakAndroidApiServiceImpl());
    setMic(new MicAndroidApiServiceImpl());

    export function getMic(): MicService {
        return MIC;
    }

    export function getSpeak(): SpeakService {
        return SPEAK;
    }

    export function getNetwork(): NetworkService {
        return NETWORK;
    }


    Callback.addCallback("LevelDisplayed", () => {
        MIC.start();
    });

    Callback.addCallback("LocalLevelLeft", () => {
        MIC.stop();
    });
}*/

const Voice = new (WRAP_JAVA("com.reider745.voicechat.Voice"))();
Voice.setDistance(__config__.getDouble("distance"));