package kr.owens.smartBike.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.view.View;
import android.widget.LinearLayout;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kr.owens.smartBike.R;
import kr.owens.smartBike.bluetooth.DefenseController;
import kr.owens.smartBike.bluetooth.SpeedReceiver;
import kr.owens.smartBike.bluetooth.TemperatureController;
import kr.owens.smartBike.databinding.ActivityMainBinding;
import kr.owens.smartBike.util.LogWrapper;
import kr.owens.smartBike.util.ToastUtil;

public class MainActivity extends RxAppCompatActivity {
    private static final int SPEED_METER_SIZE = 1350;
    private static final int MAX_SPEED = 40;

    private static final String KEY_TEMP = "Temp";

    private SpeedReceiver speedReceiver = null;
    private TemperatureController temperatureController = null;
    private DefenseController defenseController = null;

    private ActivityMainBinding mainBinding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBinding.setMain(this);

        SpeedometerGauge s = findViewById(R.id.speed_meter);
        s.setMaxSpeed(MAX_SPEED);
        s.setLabelConverter((d, m) -> String.valueOf((int) Math.round(d)));
        s.setMajorTickStep(10);
        s.setMinorTicks(1);
        s.addColoredRange(0, 10, Color.GREEN);
        s.addColoredRange(10, 30, Color.YELLOW);
        s.addColoredRange(30, 40, Color.RED);
        s.setLabelTextSize(60);
        s.setLayoutParams(new LinearLayout.LayoutParams(SPEED_METER_SIZE, SPEED_METER_SIZE));

        speedReceiver = SpeedReceiver.getInstance();
        temperatureController = TemperatureController.getInstance();
        defenseController = DefenseController.getInstance();

        onRefreshButtonClicked(null);

        @SuppressLint("CheckResult") Thread speedObserverThread = new Thread(() -> {
            speedReceiver.receiveSpeedEvent()
                    .subscribeOn(Schedulers.io())
                    .compose(this.bindToLifecycle())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(speed -> s.setSpeed(Integer.parseInt(speed.replace("speed:", "").trim())));
            while (true) {
                speedReceiver.getSpeed();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        speedObserverThread.start();
    }

    @SuppressLint("CheckResult")
    public void onRefreshButtonClicked(View v) {
//            Observable.fromCallable(() -> {
//                defenseController.defenseModeOff();
//                return true;
//            }).subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//            .subscribe((t) -> Log.e("DEBUG", "defense Send Complete!"));

        temperatureController.receiveTempEvent()
                .subscribeOn(Schedulers.io())
                .compose(this.bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {
                    s = s.replace("\n", "");
                    String temp = s.split(":")[1] + getString(R.string.temperature);
                    ToastUtil.show(getString(R.string.weather_refresh_message));
                    mainBinding.weatherText.setText(temp);
                });

        temperatureController.getTemperature();
    }

    @SuppressLint("CheckResult")
    public void onDefenseButtonClicked(View v) {
        Drawable imageId = mainBinding.defenseButton.getDrawable();
        Drawable off = getDrawable(R.drawable.guard_off_icon);
        Bitmap currentImage = ((BitmapDrawable) imageId).getBitmap();
        assert off != null;
        Bitmap offImage = ((BitmapDrawable) off).getBitmap();

        if (currentImage.equals(offImage)) {
            Observable.fromCallable(() -> {
                defenseController.defenseModeOn();

                return true;
            })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe((t) -> mainBinding.defenseButton.setImageDrawable(getDrawable(R.drawable.guard_on_icon)));
        } else {
            Observable.fromCallable(() -> {
                defenseController.defenseModeOff();

                return true;
            })
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe((t) -> mainBinding.defenseButton.setImageDrawable(getDrawable(R.drawable.guard_off_icon)));
        }
    }

    public void onGpsButtonClicked(View v) {
        startActivity(new Intent(MainActivity.this, MapActivity.class));
    }
}
