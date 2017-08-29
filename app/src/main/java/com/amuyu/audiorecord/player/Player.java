package com.amuyu.audiorecord.player;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;

import java.io.IOException;


public class Player implements IPlayer {



    private MediaPlayer mMediaPlayer; // 미디어 플레이어
    private String filePath;


    public Player() {
        filePath = Environment.getExternalStorageDirectory() + "/audio.aac";
        initPlayer();
    }

    private void initPlayer() {
        mMediaPlayer = new MediaPlayer();
        try {

            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepare();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        if(mMediaPlayer == null) throw new NullPointerException("MediaPlayer is null!!");
        mMediaPlayer.start();
    }

    @Override
    public void stop() {
        if(mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void pause() {
        if(mMediaPlayer == null) throw new NullPointerException("MediaPlayer is null!!");
        mMediaPlayer.pause();
    }
}
