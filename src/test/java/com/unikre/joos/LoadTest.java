package com.unikre.joos;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoadTest {
    private final Logger LOG = LoggerFactory.getLogger(LoadTest.class);

    @Test
    public void repeatTest() throws IOException {
        String host = "TEST_IP";
        int port = 7771;

        while (true) {
            Client client = new Client(host, port);
            client.setSoTimeout(10000);

            LOG.info("Requesting...");
            Message response = client.request(new Message(3, "test"));
            if (response == null) {
                LOG.error("response = null");
                client.disconnect();
                continue;
            }

            if (response.getCode() != 1) {
                LOG.error("response messageId = " + response.getCode());
                client.disconnect();
                continue;
            }

            LOG.info("SUCCESS, payloadSize=" + response.getPayloadSize());
            client.disconnect();
        }

    }

}
