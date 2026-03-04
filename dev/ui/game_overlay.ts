enum VoiceStatus {
    DISCONNECT,
    ON,
    OFF
}

namespace GameOverlay {
    const BUTTON_SIZE = 50;

    function getStatus(): VoiceStatus {
        if(Voice.getClient().getClientNetworkService().isConnected()) {
            return VoiceStatus.DISCONNECT;
        }

        if(Voice.getClient().getMicService().isRecording())
            return VoiceStatus.ON;

        return VoiceStatus.OFF;
    }

    function getUi(status: VoiceStatus): UI.Window {
        let texture;

        switch(status) {
            case VoiceStatus.DISCONNECT: texture = "voice_disconnected";
            break;

            case VoiceStatus.ON: texture = "voice_microphone";
            break;

            case VoiceStatus.OFF: texture = "voice_microphone_off";
            break;
        }

        let content: UI.WindowContent = {
            location: {
                x: 1000 - BUTTON_SIZE,
                y: 0,
                width: BUTTON_SIZE,
                height: BUTTON_SIZE
            },

            drawing: [
                {type: "background", color: Color.argb(0, 0, 0, 0)}
            ],

            elements: {
                button: {
                    x: 0,
                    y: 0,
                    bitmap: texture,

                    scale: 1000 / 16,

                    type: "button",

                    clicker: {
                        onClick() {
                            if(status == VoiceStatus.ON) {
                                Voice.getClient().getMicService().stop();
                            }

                            if(status == VoiceStatus.OFF) {
                                Voice.getClient().getMicService().start();
                            }
                        }   
                    }
                }
            }
        };

        let window = new UI.Window(content);
        window.setAsGameOverlay(true);
        window.setBlockingBackground(false);
        window.setTouchable(true);
        return window;
    }

    let ui: Nullable<UI.Window>;

    Callback.addCallback("LevelDisplayed", () => {
        let lastStatus = getStatus();
        ui = getUi(lastStatus); 

        ui.open();

        Updatable.addLocalUpdatable({
            update() {
                if(ui && ui.isOpened()) {
                    let newStatus = getStatus();

                    if(lastStatus != newStatus) {
                        lastStatus = newStatus;

                        ui.close();
                        ui = getUi(newStatus);
                        ui.open();
                    }
                }
            }
        });
    });

    Callback.addCallback("NativeGuiChanged", (name) => {
        if(!ui) return;

        if(name == "in_game_play_screen") 
            ui.open();
        else 
            ui.close();
    });
}