package com.unikre.joos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server extends Thread {
    private final Logger LOG = LoggerFactory.getLogger(Server.class);

    private final ServerSocket serverSocket;
    private boolean isRunning = false;
    private final int port;
    private final MessageProcessor messageProcessor;

    public Server(int port, MessageProcessor messageProcessor) throws IOException {
        super();
        this.port = port;
        this.messageProcessor = messageProcessor;
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void run() {
        LOG.info("Server started. Listening on port " + port);
        isRunning = true;

        while (isRunning) {
            try {
                Socket socket = serverSocket.accept();
                CommChannel channel = new CommChannel(socket, messageProcessor);
                channel.start();
            } catch (SocketException ignored) {
            } catch (IOException e) {
                LOG.warn("Error when new client connect", e);
            }
        }
    }

    public boolean stopServer() {
        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.error("Error when close ServerSocket", e);
            return false;
        }

        return true;
    }

}
