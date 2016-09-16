package com.unikre.joos;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InitialTest {
    private final Logger LOG = LoggerFactory.getLogger(InitialTest.class);

    @Test
    public void simpleTest() {
        int port = 9999;

        final String testPayload = "This is test payload";

        MessageProcessor messageProcessor = new MessageProcessor() {
            @Override
            public Message processRequest(Message request) {
                Object receivedObject = request.getPayloadObject(Object.class);
                LOG.info("SERVER: received request: " + receivedObject);

                return new Message(testPayload);
            }
        };

        Server server = new Server(port, messageProcessor);
        server.start();

        Client client = new Client("localhost", port);

        LOG.info("CLIENT: requesting...");
        Message response = client.request(new Message(testPayload));

        String responseObject = response.getPayloadObject(String.class);
        LOG.info("CLIENT: received response from server: " + responseObject);

        Assert.assertEquals(testPayload, responseObject);
    }

}
