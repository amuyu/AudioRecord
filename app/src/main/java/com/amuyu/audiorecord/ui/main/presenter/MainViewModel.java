package com.amuyu.audiorecord.ui.main.presenter;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.amuyu.audiorecord.player.IPlayer;
import com.amuyu.audiorecord.player.Player;
import com.amuyu.audiorecord.record.IAudioRecord;
import com.amuyu.audiorecord.record.Recorder;
import com.amuyu.logger.Logger;

public class MainViewModel extends ViewModel {

    IAudioRecord recorder;
    IPlayer player;

    MutableLiveData<Boolean> isRecording = new MutableLiveData<>();
    MutableLiveData<Boolean> isPlaying = new MutableLiveData<>();

    public MainViewModel() {
        isRecording.setValue(false);
        isPlaying.setValue(false);
    }

    public LiveData<Boolean> isRecording() {
        return isRecording;
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public void startRecord() {
        Logger.d("");
        if(recorder != null) recorder.release();
        recorder = new Recorder();
        recorder.start();
        isRecording.setValue(true);
    }

    public void stopRecord() {
        Logger.d("");
        if(recorder != null) recorder.stop();
        isRecording.setValue(false);
    }

    public void startPlayer() {
        Logger.d("");
        if(player != null) player.stop();
        player = new Player();
        player.start();
        isPlaying.setValue(true);
    }

    public void stopPlayer() {
        Logger.d("");
        if(player != null) player.stop();
        isPlaying.setValue(false);
    }

}
