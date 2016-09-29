package com.unikre.joos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class CommChannel extends Thread {
    private final Logger LOG = LoggerFactory.getLogger(CommChannel.class);

    private Socket socket;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private final MessageProcessor messageProcessor;

    CommChannel(Socket socket, MessageProcessor messageProcessor) {
        super();
        this.socket = socket;

        this.messageProcessor = messageProcessor;
    }

    @Override
    public void run() {
        try {
            LOG.trace("Preparing IO streams...");
            this.os = new ObjectOutputStream(socket.getOutputStream());
            this.is = new ObjectInputStream(socket.getInputStream());

            LOG.trace("IO streams ready. Start loop waiting for Message");
            while (true) {
                Message request;
                try {
                    LOG.trace("Reading object from client...");
                    request = (Message) is.readObject();
                } catch (EOFException e) {
                    LOG.trace("Client close connection");
                    break;
                }

                LOG.trace("Processing request...");
                Message response = messageProcessor.processRequest(request);

                LOG.trace("Sending response to client...");
                os.writeObject(response);
            }
        } catch (Exception e) {
            LOG.error("Exception in CommChannel", e);
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

        LOG.trace("CommChannel ended");
    }
}