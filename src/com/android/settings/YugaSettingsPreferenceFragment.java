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

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class YugaSettingsPreferenceFragment extends SettingsPreferenceFragment {

    public void setYugaBool(boolean enabled, String config_file) {
        setYugaString( (enabled ? "1" : "0"), config_file);
    }

    public boolean getYugaBool(String config_file) {
        int value = 0;

        try {
            Integer.parseInt( getYugaString(config_file) );
        } catch(Exception e) { }

        return (value == 0 ? false : true);
    }

    public void setYugaString(String value, String config_file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(config_file));
            writer.write(value+"\n");
            writer.close();
            Runtime.getRuntime().exec("/system/bin/start yuga_reconf");
        } catch(Exception e) {}
    }

    public String getYugaString(String config_file) {
        String line = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(config_file), 256);
            line = reader.readLine();
            reader.close();
        } catch(Exception e) {} /* file does not exist or is corrupted */
        return line;
   }



}
