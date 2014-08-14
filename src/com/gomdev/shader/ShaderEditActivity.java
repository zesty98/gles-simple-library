package com.gomdev.shader;

import com.gomdev.shader.R;
import com.gomdev.gles.GLESFileUtils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class ShaderEditActivity extends Activity {

    private EditText mEditView = null;

    private String mShaderSource = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.shader_edit);

        mEditView = (EditText) findViewById(R.id.shader_edit);

        mShaderSource = EffectUtils.getShaderSource(this);

        mEditView.setText(mShaderSource);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.shader_edit_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.save:
            SharedPreferences pref = this.getSharedPreferences(
                    EffectConfig.PREF_NAME, MODE_PRIVATE);

            if (GLESFileUtils.isExternalStorageWriable() == false) {
                Toast.makeText(this, "SDCard is not available",
                        Toast.LENGTH_SHORT).show();

                return false;
            }

            String savedFileName = pref.getString(EffectConfig.PREF_SAVED_FILE_NAME, "");

            GLESFileUtils.write(savedFileName, mEditView.getText().toString());

            Toast.makeText(this, savedFileName + " Saved", Toast.LENGTH_SHORT).show();

            this.finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
