package com.unikre.joos;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class Message implements Serializable {
    private static final long serialVersionUID = -4032660901766903668L;
    private int code;
    private byte[] body;

    private static final Gson gson = new Gson();

    public Message(int code, Object object) {
        this.setCode(code);
        this.setPayloadObject(object);
    }

    private byte[] compress(String json) {
        return toGZIPBytes(json);
    }

    private String decompress(byte[] compressedBytes) {
        return fromGZIPBytes(compressedBytes);
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

    public int getPayloadSize() {
        return (body == null ? 0 : body.length);
    }

    public <T> T getPayloadObject(Type typeOfT) {
        return gson.fromJson(decompress(body), typeOfT);
    }

    private byte[] toGZIPBytes(String inputString) {

        if (inputString == null || inputString.length() == 0) {
            return null;
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream(inputString.length());
             GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
            gzos.write(inputString.getBytes());
            gzos.close();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    private String fromGZIPBytes(byte[] inputBytes) {

        if (inputBytes == null || inputBytes.length == 0) {
            return "";
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
             GZIPInputStream gzis = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = gzis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            return new String(baos.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
