package io.rala.jugger.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;
import io.rala.jugger.JuggerStonesApp;
import io.rala.jugger.LocaleUtils;
import io.rala.jugger.R;

public class MyPreferenceActivity extends AppCompatActivity {

    public MyPreferenceActivity() {
        LocaleUtils.updateConfig(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);
    }

    protected void changeLanguage(String language) {
        LocaleUtils.setLocale(new Locale(language));
        LocaleUtils.updateConfig(getApplication(), getResources().getConfiguration());
        Intent intent = new Intent(this, MyPreferenceActivity.class);
        if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                JuggerStonesApp.increaseMusicVolume();
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                JuggerStonesApp.decreaseMusicVolume();
                return true;
            case KeyEvent.KEYCODE_BACK:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (getIntent().getExtras() != null) intent.putExtras(getIntent().getExtras());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
                return true;
            default:
                return true; // should be false..?
        }
    }
}