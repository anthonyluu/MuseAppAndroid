package com.example.anthonyluu.museapp;

import android.content.Context;
import android.widget.MediaController;

/**
 * Created by tiffanylui on 2015-08-16.
 */
public class MusicController extends MediaController {
    public MusicController(Context context) {
        super(context);
    }

    public void hide(){}

    public void setPrevNextListeners(OnClickListener onClickListener) {
    }
}
