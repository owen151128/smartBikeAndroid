package kr.owens.smartBike.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.lang.ref.WeakReference;

import kr.owens.smartBike.R;
import kr.owens.smartBike.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {
    private ActivitySplashBinding splashBinding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        InvisibleHandler invisibleHandler = new InvisibleHandler(this);
        IntentHandler intentHandler = new IntentHandler(this);

        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            invisibleHandler.sendMessage(new Message());

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            intentHandler.sendMessage(new Message());
        });

        thread.start();
    }

    private void invisibleUI() {
        splashBinding.loadProgress.setVisibility(View.INVISIBLE);
        splashBinding.loadText.setVisibility(View.INVISIBLE);
    }

    private void loadMainActivity() {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    private static class InvisibleHandler extends Handler {
        private final WeakReference<SplashActivity> activity;

        InvisibleHandler(SplashActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SplashActivity splashActivity = activity.get();

            if (splashActivity != null) {
                splashActivity.invisibleUI();
            }
        }
    }

    private static class IntentHandler extends Handler {
        private final WeakReference<SplashActivity> activity;

        IntentHandler(SplashActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SplashActivity splashActivity = activity.get();

            if (splashActivity != null) {
                splashActivity.loadMainActivity();
            }
        }
    }
}
