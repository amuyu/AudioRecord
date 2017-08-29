package com.amuyu.audiorecord.record;


public class AudioData {
    byte[] data;
    int readLength;

    public AudioData(byte[] data, int readLength) {
        this.data = data;
        this.readLength = readLength;
    }
}
