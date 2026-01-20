interface NetworkService {
    sendToServer(buff: number[], length: number): void;

    sendToClient(client: NetworkClient, buff: number[], length: number);

    setClientHandler(handler: HandlerSound): void;

    setServerHandler(handler: NonNullable<(client: NetworkClient, buff: number[], length: number) => void>): void;
}


class NetworkInnerCoreServiceImpl implements NetworkService {
    private clientHandler: HandlerSound = () => {};
    private serverHandler: NonNullable<(client: NetworkClient, buff: number[], length: number) => void> = () => {};

    constructor() {
        Network.addClientPacket("voice.speak", (packet: number[]) => {
            packet = ByteBuffer.wrap(packet).array();
            this.clientHandler(packet, packet.length);
        });

        Network.addServerPacket("voice.mic", (client: NetworkClient, packet: number[]) => {
            this.serverHandler(client, packet, packet.length);
        });
    }

    public sendToServer(buff: number[], length: number): void {
        Network.sendToServer("voice.mic", buff.slice(0, Math.min(buff.length, length)));
    }

    public sendToClient(client: NetworkClient, buff: number[], length: number): void {
        client.send("voice.speak", buff.slice(0, Math.min(buff.length, length)));
    }

    public setClientHandler(handler: HandlerSound): void {
        this.clientHandler = handler;
    }

    public setServerHandler(handler: NonNullable<(client: NetworkClient, buff: number[], length: number) => void>): void {
        this.serverHandler = handler;
    }
}