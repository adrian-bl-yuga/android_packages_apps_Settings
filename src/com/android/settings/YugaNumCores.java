/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.SeekBarDialogPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class YugaNumCores extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener {
    
    private SeekBar mSeekBar_MAX;
    private SeekBar mSeekBar_MIN;
    private TextView mSliderValue;
    private int value_sb_min;
    private int value_sb_max;

    private static String CF_MIN = "/data/misc/.pabx_settings_min_cores";
    private static String CF_MAX = "/data/misc/.pabx_settings_max_cores";
    private static final int SEEK_BAR_RANGE = 3; /* 0-3 */

    public YugaNumCores(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_yuga_dual_seekbar);
        setDialogIcon(R.drawable.ic_settings_yuga);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        if(seekBar.getId() == R.id.seekbar_max) {
            value_sb_max = progress+1;
        } else {
            value_sb_min = progress+1;
        }
        String userText = "Max="+value_sb_max+", Min="+value_sb_min;
        mSliderValue.setText(userText);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mSliderValue = (TextView) view.findViewById(R.id.slider_value);

        value_sb_max = getPrefValue(CF_MAX, SEEK_BAR_RANGE+1);
        mSeekBar_MAX = (SeekBar) view.findViewById(R.id.seekbar_max);
        mSeekBar_MAX.setMax(SEEK_BAR_RANGE);
        mSeekBar_MAX.setOnSeekBarChangeListener(this);

        value_sb_min = getPrefValue(CF_MIN, 1);
        mSeekBar_MIN = (SeekBar) view.findViewById(R.id.seekbar_min);
        mSeekBar_MIN.setMax(SEEK_BAR_RANGE);
        mSeekBar_MIN.setOnSeekBarChangeListener(this);

        // init defaults
        onProgressChanged(mSeekBar_MAX, value_sb_max-1, false);
        mSeekBar_MIN.setProgress(value_sb_min-1);
        mSeekBar_MAX.setProgress(value_sb_max-1);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
       super.onDialogClosed(positiveResult);
       if(positiveResult) {
           if(value_sb_min > value_sb_max) {
               value_sb_min = value_sb_max;
           }
           setPrefValue(CF_MAX, value_sb_max);
           setPrefValue(CF_MIN, value_sb_min);
           // Now kick qcfqd to re-read its config
           try {
               Runtime.getRuntime().exec("/system/bin/stop qcfqd");
               Runtime.getRuntime().exec("/system/bin/start qcfqd");
           } catch(Exception e) {}
       }
    }

    /* Attempts to store 'val' in 'cfile' */
    private void setPrefValue(String cfile, int val) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(cfile));
            writer.write(val+"\n");
            writer.close();
        } catch(Exception e) {}
    }

    /* Attempts to read 'cfile', returns the default value on error */
    private int getPrefValue(String cfile, int dflt) {
        int value = dflt;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(cfile), 256);
            String line = reader.readLine();
            reader.close();
            value = Integer.parseInt(line);
        } catch(Exception e) {}
        return value;
    }
}

