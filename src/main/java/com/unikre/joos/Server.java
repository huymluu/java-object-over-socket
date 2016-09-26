package com.unikre.joos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Server {
    private final Logger LOG = LoggerFactory.getLogger(Server.class);

    private ServerSocket serverSocket;
    private ThreadWaitClient threadWaitClient;
    private boolean isRunning = false;
    private final int port;
    private final MessageProcessor messageProcessor;

    public Server(int port, MessageProcessor messageProcessor) {
        this.port = port;
        this.messageProcessor = messageProcessor;
    }

    public boolean start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("Error when new ServerSocket at port " + port, e);
            return false;
        }

        threadWaitClient = new ThreadWaitClient();
        isRunning = true;
        threadWaitClient.start();
        LOG.info("Success start Server at port " + port);
        return true;
    }

    public void joinThread() throws InterruptedException {
        this.threadWaitClient.join();
    }

    public boolean stop() {

        isRunning = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            LOG.error("Error when close ServerSocket", e);
            return false;
        }

        return true;
    }

    private synchronized void onNewClientConnected(Socket socket) throws IOException {
        LOG.info("onNewClientConnected()");
        ClientHandler clientHandler = new ClientHandler(socket);
        clientHandler.start();
    }

    private synchronized void onClientDisconnected(ClientHandler clientHandler) {
        LOG.info("onClientDisconnected()");
    }

    private class ThreadWaitClient extends Thread {
        @Override
        public void run() {
            while (isRunning) {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                    onNewClientConnected(socket);
                } catch (SocketException ignored) {
                } catch (IOException e) {
                    LOG.warn("Error when accept()", e);
                }
            }
        }
    }

    private class ClientHandler extends Thread {
        private Socket socket;
        private ObjectInputStream is;
        private ObjectOutputStream os;

        ClientHandler(Socket socket) throws IOException {
            super();
            this.socket = socket;
            this.os = new ObjectOutputStream(socket.getOutputStream());
            this.is = new ObjectInputStream(socket.getInputStream());
        }

        @Override
        public void run() {
            while (true) {

                Message request;
                try {
                    request = (Message) is.readObject();
                } catch (EOFException e) {
                    LOG.info("ClientHandler: client close connection");
                    break;
                } catch (ClassNotFoundException e) {
                    LOG.warn("ClientHandler: ClassNotFoundException incorrect object type from client");
                    continue;
                } catch (IOException e) {
                    LOG.error("ClientHandler: Error when readObject()", e);
                    break;
                }

                Message response = messageProcessor.processRequest(request);
                try {
                    os.writeObject(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                is.close();
            } catch (IOException ignored) {
            }

            try {
                os.close();
            } catch (IOException ignored) {
            }

            try {
                socket.close();
            } catch (IOException ignored) {
            }

            onClientDisconnected(this);
        }
    }
}
