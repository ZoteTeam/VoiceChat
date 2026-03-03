package com.reider745.voicechat.service.network.impl;

import androidx.annotation.Nullable;
import com.reider745.voicechat.data.VoiceEntry;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketClientVoice {
    private static final byte[] EMPTY = new byte[0];

    private final Socket socket;
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

    @Nullable
    public VoiceEntry handleClient() {
        try {
            if(inputStream.available() == 0) {
                return null;
            }

            int length = inputStream.readInt();
            String username = inputStream.readUTF();

            // Ограничение на количество байт которые клиент может принять
            if(length > Short.MAX_VALUE) {
                inputStream.skipBytes(Short.MAX_VALUE);
                return null;
            }

            if (length > 0){
                byte[] buff = new byte[length];
                inputStream.readFully(buff);
                return new VoiceEntry(username, buff);
            }

            return null;
        } catch (IOException e) {
            return null;
        }
    }

    public void sendToClient(String username, byte[] buff, int length) {
        try {
            outputStream.writeInt(length);
            outputStream.writeUTF(username);
            outputStream.write(buff, 0, length);
            outputStream.flush();
        } catch (IOException ignored) {}
    }

    public void sendToServer(short[] buff, int length) {
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
