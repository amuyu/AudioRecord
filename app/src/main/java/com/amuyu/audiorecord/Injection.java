package com.amuyu.audiorecord;

import com.amuyu.audiorecord.ui.ViewModelFactory;

/**
 * Created by amuyu on 2017. 8. 28..
 */

public class Injection {
    public static ViewModelFactory provideViewModelFactory() {
        return new ViewModelFactory();
    }

}
