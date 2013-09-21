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

public class PowersaveBiasPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener {
    
    private SeekBar mSeekBar;
    private TextView mSliderValue;
    private static final int SEEK_BAR_RANGE = 700;
    private static final String config_file = "/data/misc/.pabx_settings_powersave_bias";
    private int mCurrentValue;

    public PowersaveBiasPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.preference_dialog_powersave_bias);
        setDialogIcon(R.drawable.ic_settings_yuga);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        String userText = "disabled";
        mCurrentValue = progress;
        if(mCurrentValue > 0) {
            userText = String.format("%.1f%%", mCurrentValue/10f);
        }
        mSliderValue.setText(userText);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        
        mSliderValue = (TextView) view.findViewById(R.id.slider_value);
        mSeekBar = getSeekBar(view);
        mSeekBar.setMax(SEEK_BAR_RANGE);
        mSeekBar.setProgress(getPrefValue());
        mSeekBar.setEnabled(true);
        mSeekBar.setOnSeekBarChangeListener(this);
        /* Init help text */
        onProgressChanged(mSeekBar, getPrefValue(), false);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if(positiveResult) {
            setPrefValue(mCurrentValue);
        }
    }


    private void setPrefValue(int val) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(config_file));
            writer.write(val+"\n");
            writer.close();
            /* Try to restart qc-fqd (this is pretty ugly) */
            Runtime.getRuntime().exec("/system/bin/stop qcfqd");
            Runtime.getRuntime().exec("/system/bin/start qcfqd");
        } catch(Exception e) {}
    }

    private int getPrefValue() {
        int value = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(config_file), 256);
            String line = reader.readLine();
            reader.close();
            value = Integer.parseInt(line);
        } catch(Exception e) {} /* file does not exist or is corrupted */
        return value;
    }

}

