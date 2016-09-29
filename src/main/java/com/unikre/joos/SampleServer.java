package com.unikre.joos;

public class SampleServer {
    public static void main(String[] args) {
        Server server = new Server(7771, new MessageProcessor() {
            @Override
            public Message processRequest(Message request) {

                Message response = new Message(1, "Hello from server");

                return response;
            }
        });

        if (server.start()) {
            while (true) ;
        }
    }
}
