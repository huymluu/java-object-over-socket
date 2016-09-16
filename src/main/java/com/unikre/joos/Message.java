package com.unikre.joos;

import com.google.gson.Gson;

import java.io.Serializable;

public class Message implements Serializable {
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;

    private int code;
    private byte[] body;

    private static final Gson gson = new Gson();

    public Message(int code, Object object) {
        this.setCode(code);
        this.setPayloadObject(object);
    }

    public Message(Object object) {
        this(SUCCESS, object);
    }

    private byte[] compress(String json) {
        // TODO: zip

        return json.getBytes();
    }

    private String decompress(byte[] compressedBytes) {
        // TODO: unzip

        return new String(compressedBytes);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public void setPayloadObject(Object object) {
        body = compress(gson.toJson(object));
    }

    public <T> T getPayloadObject(Class<T> classOfT) {
        return gson.fromJson(decompress(body), classOfT);
    }
}
