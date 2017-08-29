package com.amuyu.audiorecord.record;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import com.amuyu.logger.Logger;
import com.todoroo.aacenc.AACEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.reactivex.disposables.Disposable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;


public class Recorder implements IAudioRecord, Runnable {

    final static int BIT_RATE = 64000;
    final static int SAMPLE_RATE = 16000;
    final static int MIC_CHANNELS = 1;
    final static int PCM_FORMAT = 16;

    // recorder
    private int bufferSize = 0;
    private static AudioRecord mRecorder = null;

    // encoder
    AACEncoder mEncoder;
    private String filePath;
    private File mEncodeFile;
    private PublishProcessor<AudioData> subject = PublishProcessor.create();
    private Disposable disposable;

    private Thread mThread = null;
    private boolean isRecording = false;

    public Recorder() {
        init();
    }

    private void init() {
        initFile();
        initEncoder();
        initRecoder();
        initThread();
        initStream();
    }

    private void initFile() {
        filePath = Environment.getExternalStorageDirectory() + "/audio.aac";
        mEncodeFile = new File(filePath);
        if(mEncodeFile.exists()) mEncodeFile.delete();
    }

    private void initEncoder() {
        mEncoder = new AACEncoder();
        mEncoder.init(BIT_RATE, MIC_CHANNELS, SAMPLE_RATE, PCM_FORMAT, filePath);
    }

    private void initRecoder() {
        int sampleRate = SAMPLE_RATE;
        int channels = MIC_CHANNELS;
        int pcmFormat = PCM_FORMAT;

        final int[] channelType = {AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.CHANNEL_IN_STEREO};
        final int[] pcmType = {AudioFormat.ENCODING_PCM_8BIT,
                AudioFormat.ENCODING_PCM_16BIT};

        int channelConfig = channelType[channels - 1];
        int audioFormat = pcmType[pcmFormat / 8 - 1];

        bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        bufferSize = 4096; //?

        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                channelConfig, audioFormat, bufferSize);
    }

    private void initThread() {
        mThread = new Thread(this);
        isRecording = false;
    }

    private void initStream() {
        disposable = subject.observeOn(Schedulers.io())
                .subscribe(this::write);
    }

    @Override
    public void start() {
        if(mRecorder == null) {
            throw new NullPointerException("AudioRecord is null!!");
        }

        if(mThread == null) {
            throw new NullPointerException("Thread is null!!");
        }

        isRecording = true;
        mThread.start();
        mRecorder.startRecording();
    }

    @Override
    public void stop() {
        if (mRecorder == null)
            return;

        if (isRecording) {
            isRecording = false;
            mRecorder.stop();
        }

        disposable.dispose();

        try {
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        release();
    }

    @Override
    public void release() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }
        if (mEncoder != null) {
            mEncoder.uninit();
        }
    }

    @Override
    public void run() {
        int dataBufSize = bufferSize / 2;
        byte data[] = new byte[dataBufSize];
        byte[] modifiedData = new byte[dataBufSize];

        int read = 0;
        while(isRecording) {
            read = mRecorder.read(data, 0, dataBufSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                Logger.d("");
                subject.onNext(new AudioData(data, read));
            }
        }
    }

    private void write(AudioData data) {
        Logger.d("");
        byte[] modifiedData = ampDB(data.data, data.readLength);
        byte[] aacData = mEncoder.encode(modifiedData);
        if(aacData != null) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mEncodeFile, true);
                fos.write(aacData);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                try {
                    if(fos != null)
                        fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte[] ampDB(byte[] data, int length) {
        int i = 0;
        int recBufferBytePtr = 0;

        byte[] modifiedData = new byte[data.length];

        while ( i < length ) {
            float sample = (float)( data[recBufferBytePtr+i  ] & 0xFF
                    | data[recBufferBytePtr+i+1] << 8 );

            // THIS is the point were the work is done:
            // Increase level by about 6dB:
            sample *= 2;
            // Or increase level by 20dB:
            // sample *= 10;
            // Or if you prefer any dB value, then calculate the gain factor outside the loop
            // float gainFactor = (float)Math.pow( 10., dB / 20. );    // dB to gain factor
            // sample *= gainFactor;

            // Avoid 16-bit-integer overflow when writing back the manipulated data:
            if ( sample >= 32767f ) {
                data[recBufferBytePtr+i  ] = (byte)0xFF;
                data[recBufferBytePtr+i+1] =       0x7F;
            } else if ( sample <= -32768f ) {
                data[recBufferBytePtr+i  ] =       0x00;
                data[recBufferBytePtr+i+1] = (byte)0x80;
            } else {
                int s = (int)( 0.5f + sample );  // Here, dithering would be more appropriate
                data[recBufferBytePtr+i  ] = (byte)(s & 0xFF);
                data[recBufferBytePtr+i+1] = (byte)(s >> 8 & 0xFF);
            }
            i += 2;
        }
        return data;
    }

}
