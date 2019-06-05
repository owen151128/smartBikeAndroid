package kr.owens.smartBike.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.LinearLayout;

import com.cardiomood.android.controls.gauge.SpeedometerGauge;

import kr.owens.smartBike.R;

public class MainActivity extends AppCompatActivity {
    private static final int SPEED_METER_SIZE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpeedometerGauge s = findViewById(R.id.speed_meter);
        s.setLayoutParams(new LinearLayout.LayoutParams(SPEED_METER_SIZE, SPEED_METER_SIZE));
    }
}
