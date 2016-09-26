package com.unikre.joos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private final Logger LOG = LoggerFactory.getLogger(Client.class);

    private final String serverAddress;
    private final int serverPort;

    private Socket socket;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private boolean isConnected = false;

    public Client(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    private boolean tryConnectIfNot() {
        if (isConnected)
            return true;

        // Disconnect first
        disconnect();

        // Connect
        try {
            socket = new Socket(serverAddress, serverPort);
            socket.setSoTimeout(5000);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            LOG.info("Connected to " + serverAddress + ":" + serverPort);
            return true;
        } catch (IOException e) {
            LOG.error("IOException when connect to " + serverAddress + ":" + serverPort, e);
            disconnect();
            return false;
        }
    }

    public void disconnect() {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ignored) {
            } finally {
                is = null;
            }
        }

        if (os != null) {
            try {
                os.close();
            } catch (IOException ignored) {
            } finally {
                os = null;
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            } finally {
                socket = null;
            }
        }

        isConnected = false;
    }

    public Message request(Message request) {
        if (!tryConnectIfNot())
            return null;

        // Request
        try {
            os.writeObject(request);
        } catch (IOException e) {
            disconnect();
            LOG.error("FAIL can not write to server " + e.toString());
            return null;
        }

        // Response
        try {
            return (Message) is.readObject();
        } catch (Exception e) {
            disconnect();
            LOG.error("FAIL can not read from server " + e.toString());
            return null;
        }
    }

}
