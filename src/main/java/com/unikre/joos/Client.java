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
    private int socketTimeout = 0;

    public Client(String address, int port) {
        this.serverAddress = address;
        this.serverPort = port;
    }

    public void setSoTimeout(int timeout) {
        this.socketTimeout = timeout;
    }

    private boolean tryConnectIfNot() {
        if (isConnected)
            return true;

        // Disconnect first
        disconnect();

        // Connect
        try {
            socket = new Socket(serverAddress, serverPort);
            socket.setSoTimeout(socketTimeout);
            os = new ObjectOutputStream(socket.getOutputStream());
            is = new ObjectInputStream(socket.getInputStream());
            isConnected = true;
            LOG.trace("Connected to " + serverAddress + ":" + serverPort);
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
            LOG.error("IOException - writeObject()", e);
            return null;
        }

        // Response
        try {
            return (Message) is.readObject();
        } catch (Exception e) {
            disconnect();
            LOG.error("IOException - readObject()", e);
            return null;
        }
    }

}
