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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class YugaSettings extends SettingsPreferenceFragment {

    private static final String LOG_TAG = "YugaSettings";
    private static final String PREF_SINGLECORE_MODE = "pabx_singlecore_mode";
    private static final String scmode_config_file = "/data/misc/.pabx_singlecore_mode";

    private CheckBoxPreference mSingleCoreMode;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        addPreferencesFromResource(R.xml.yuga_settings);

        mSingleCoreMode = (CheckBoxPreference) findPreference(PREF_SINGLECORE_MODE);
        mSingleCoreMode.setChecked(getSingleCoreMode());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference == mSingleCoreMode) {
            setSingleCoreMode(mSingleCoreMode.isChecked());
            restartQcFqd();
        }
        return true;
    }

    /* Creates (or removes) the singlecore trigger config file */
    private void setSingleCoreMode(Boolean on) {
        try {
            File confFile = new File(scmode_config_file);
            if(on == true) {
                confFile.createNewFile();
            } else {
                confFile.delete();
            }
        } catch(Exception e) {}
    }

    /* Returns true if singlecore mode is enabled */
    private Boolean getSingleCoreMode() {
        File confFile = new File(scmode_config_file);
        return confFile.exists();
    }

    /* Tells Androids init to restart qc-fqd */
    private void restartQcFqd() {
        try {
            Runtime.getRuntime().exec("/system/bin/stop qcfqd");
            Runtime.getRuntime().exec("/system/bin/start qcfqd");
        } catch(Exception e) {}
    }

}
