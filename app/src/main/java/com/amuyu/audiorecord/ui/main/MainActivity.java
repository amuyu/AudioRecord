package com.amuyu.audiorecord.ui.main;

import android.arch.lifecycle.LifecycleActivity;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.amuyu.audiorecord.Injection;
import com.amuyu.audiorecord.R;
import com.amuyu.audiorecord.databinding.ActivityMainBinding;
import com.amuyu.audiorecord.ui.main.presenter.MainViewModel;
import com.amuyu.logger.Logger;

import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends LifecycleActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;

    private final CompositeDisposable mDisposable = new CompositeDisposable();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("");
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        viewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory())
                .get(MainViewModel.class);

        binding.btnRecord.setOnClickListener(view -> {
            if(!viewModel.isRecording().getValue())
                viewModel.startRecord();
            else
                viewModel.stopRecord();
        });

        binding.btnPlay.setOnClickListener(view -> {
            if(!viewModel.isPlaying().getValue())
                viewModel.startPlayer();
            else
                viewModel.stopPlayer();

        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        viewModel.isRecording().observe(this, record -> {
            viewRecordBtn(record);
        });

        viewModel.isPlaying().observe(this, play -> {
            viewPlayBtn(play);
        });
    }

    private void viewRecordBtn(boolean isRecord) {
        binding.btnPlay.setEnabled(!isRecord);
        binding.btnRecord.setText(isRecord?getString(R.string.record_stop):getString(R.string.record_start));
    }

    private void viewPlayBtn(boolean isPlay) {
        binding.btnRecord.setEnabled(!isPlay);
        binding.btnPlay.setText(isPlay?getString(R.string.play_stop):getString(R.string.play_start));
    }
}
