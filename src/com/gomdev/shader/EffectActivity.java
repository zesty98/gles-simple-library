/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.gomdev.shader;

import java.io.File;

import com.gomdev.shader.R;
import com.gomdev.shader.whitehole.WhiteholeConfig;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class EffectActivity extends Activity {
    private static final String CLASS = "EffectActivity";
    private static final String TAG = "gomdev " + CLASS;
    private static final boolean DEBUG = false;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.effect_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

        SharedPreferences pref = this.getSharedPreferences(
                EffectConfig.PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        String effectName = pref.getString(EffectConfig.PREF_EFFECT_NAME,
                WhiteholeConfig.EFFECT_NAME);

        String savedFileName = null;

        switch (item.getItemId()) {
        case R.id.vertex_shader:
            editor.putString(EffectConfig.PREF_SHADER_TYPE,
                    EffectConfig.SHADER_TYPE_VS);

            editor.commit();

            intent = new Intent(this,
                    com.gomdev.shader.ShaderViewActivity.class);
            startActivity(intent);
            return true;
        case R.id.fragment_shader:
            editor.putString(EffectConfig.PREF_SHADER_TYPE,
                    EffectConfig.SHADER_TYPE_FS);

            editor.commit();

            intent = new Intent(this,
                    com.gomdev.shader.ShaderViewActivity.class);
            startActivity(intent);
            return true;
        case R.id.restore_vs:
            savedFileName = EffectUtils.getSavedFilePath(this, effectName,
                    EffectConfig.SHADER_TYPE_VS);
            File file = new File(savedFileName);
            file.delete();
            this.finish();
            return true;
        case R.id.restore_fs:
            savedFileName = EffectUtils.getSavedFilePath(this, effectName,
                    EffectConfig.SHADER_TYPE_FS);
            file = new File(savedFileName);
            file.delete();
            this.finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}