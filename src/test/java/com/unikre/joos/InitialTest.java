package com.unikre.joos;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class InitialTest {
    private final Logger LOG = LoggerFactory.getLogger(InitialTest.class);

    public static int sizeof(Object obj) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteOutputStream.toByteArray().length;
    }

    public static String readFile(String pathname) throws IOException {
        File file = new File(pathname);
        StringBuilder fileContents = new StringBuilder((int) file.length());
        String lineSeparator = System.getProperty("line.separator");
        FileReader fr = new FileReader(pathname);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            fileContents.append(line + lineSeparator);
        }
        br.close();

        return fileContents.toString();
    }

    @Test
    public void simpleTest() throws IOException {
        int port = 9999;
        String inputFile = "sample_file.txt";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(inputFile).getFile());

        final String testPayload = readFile(file.getAbsolutePath());

        MessageProcessor messageProcessor = new MessageProcessor() {
            @Override
            public Message processRequest(Message request) {
                Object receivedObject = request.getPayloadObject(Object.class);
                LOG.info("SERVER: received request: " + receivedObject);

                return new Message(0, testPayload);
            }
        };

        Server server = new Server(port, messageProcessor);
        server.start();

        Client client = new Client("localhost", port);

        LOG.info("CLIENT: requesting...");
        Message request = new Message(0, testPayload);
        LOG.info("CLIENT: sizeOf request: " + sizeof(request));
        Message response = client.request(request);
        LOG.info("CLIENT: sizeOf response: " + sizeof(response));

        String responseObject = response.getPayloadObject(String.class);
        LOG.info("CLIENT: received response from server: " + responseObject);

        Assert.assertEquals(testPayload, responseObject);
    }

}
