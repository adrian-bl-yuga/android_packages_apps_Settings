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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class YugaSettings extends YugaSettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String LOG_TAG = "YugaSettings";
    private static final String PREF_CPU_GOVERNOR = "pabx_settings_governor";
    private static final String PREF_DOUBLE_TAP_TO_WAKE = "pabx_settings_double_tap";

    private static final String CF_DOUBLE_TAP_TO_WAKE = "/data/.pabx/"+PREF_DOUBLE_TAP_TO_WAKE;
    private static final String CF_CPU_GOVERNOR = "/data/.pabx/"+PREF_CPU_GOVERNOR;

    private SwitchPreference mDoubleTapToWake;
    private SwitchPreference mVolumeRocker;
    private ListPreference mCpuGovernor;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        addPreferencesFromResource(R.xml.yuga_settings);
        mDoubleTapToWake = (SwitchPreference) findPreference(PREF_DOUBLE_TAP_TO_WAKE);
        mDoubleTapToWake.setChecked(getYugaBool(CF_DOUBLE_TAP_TO_WAKE));

        mVolumeRocker = (SwitchPreference) findPreference(Settings.System.PABX_VOLUME_ROCKER);
        mVolumeRocker.setPersistent(false); // we handle saving on our own
        boolean bVolumeRockerState = 
                (Settings.System.getInt(getContentResolver(), Settings.System.PABX_VOLUME_ROCKER, Settings.System.PABX_VOLUME_ROCKER_DEFAULT) 
                    != Settings.System.PABX_VOLUME_ROCKER_DEFAULT);
        mVolumeRocker.setChecked(bVolumeRockerState);

        mCpuGovernor = (ListPreference) findPreference(PREF_CPU_GOVERNOR);
        mCpuGovernor.setOnPreferenceChangeListener(this);
        mCpuGovernor.setValue(getYugaString(CF_CPU_GOVERNOR));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if(preference == mDoubleTapToWake) {
            setYugaBool(mDoubleTapToWake.isChecked(), CF_DOUBLE_TAP_TO_WAKE);
            if(mDoubleTapToWake.isChecked()) {
                displayD2wWarning();
            }
        }
        if(preference == mVolumeRocker) {
            Settings.System.putInt(getContentResolver(), Settings.System.PABX_VOLUME_ROCKER,
                    mVolumeRocker.isChecked() ? 1 : 0);
        }
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (PREF_CPU_GOVERNOR.equals(key)) {
            setYugaString((String) newValue, CF_CPU_GOVERNOR);
        }
        return true;
    }

    private void displayD2wWarning() {
        AlertDialog mWarningDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.yuga_doubletap_warning_title)
                .setMessage(R.string.yuga_doubletap_warning_text)
                .setPositiveButton(R.string.dlg_ok, null)
                .create();
        mWarningDialog.show();
    }

}
