package com.reider745.voicechat.service.network.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketClientVoice {
    private static final byte[] EMPTY = new byte[0];

    private Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;

    public SocketClientVoice(Socket socket, DataInputStream inputStream, DataOutputStream outputStream) {
        this.socket = socket;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public byte[] handleServer() {
        try {
            int length = inputStream.readInt();
            if(length > 0) {
                byte[] bytes = new byte[length];
                inputStream.readFully(bytes, 0, length);
                return bytes;
            }
            return EMPTY;
        } catch (IOException e) {
            return EMPTY;
        }
    }

    public byte[] handleClient() {
        try {
            if(inputStream.available() == 0) {
                return EMPTY;
            }

            int length = inputStream.readInt();
            if (length > 0){
                byte[] buff = new byte[length];
                inputStream.readFully(buff);
                return buff;
            }

            return EMPTY;
        } catch (IOException e) {
            return EMPTY;
        }
    }

    public void send(byte[] buff, int length) {
        try {
            outputStream.writeInt(length);
            outputStream.write(buff, 0, length);
            outputStream.flush();
        } catch (IOException ignored) {}
    }

    public void send(short[] buff, int length) {
        try {
            if(length <= 0) {
                return;
            }

            outputStream.writeInt(length * 2);
            for(int i = 0; i < length; i++) {
                final short sample = buff[i];

                outputStream.write((byte) (sample & 0xFF));
                outputStream.write((byte) ((sample >> 8) & 0xFF));
            }
            outputStream.flush();
        } catch (IOException ignored) {}
    }

    public void close() {
        try {
            socket.close();
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
