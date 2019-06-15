package kr.owens.smartBike.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kr.owens.smartBike.R;
import kr.owens.smartBike.bluetooth.DefenseController;
import kr.owens.smartBike.bluetooth.SpeedReceiver;
import kr.owens.smartBike.bluetooth.TemperatureController;
import kr.owens.smartBike.databinding.ActivitySplashBinding;
import kr.owens.smartBike.util.ToastUtil;

public class SplashActivity extends RxAppCompatActivity {
    private ActivitySplashBinding splashBinding = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> devices = null;
    private BluetoothDevice bluetoothDevice = null;
    private BluetoothSocket bluetoothSocket = null;
    private SpeedReceiver speedReceiver = null;
    private DefenseController defenseController = null;
    private TemperatureController temperatureController = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private String temp = null;

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_GPS = 1002;
    private static final int EXIT = 0;

    private static final String BLANK = " ";
    private static final String KEY_TEMP = "TEMP";

    private static final String BT_UUID = "00001101-0000-1000-8000-00805f9b34fb";

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash);
        InvisibleHandler invisibleHandler = new InvisibleHandler(this);
        IntentHandler intentHandler = new IntentHandler(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);

        int gpsPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (gpsPermission != PackageManager.PERMISSION_GRANTED) {   //  GPS 권한이 승인되어 있지 않을때
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {    //  최초 GPS 권한 물어볼때 거절 당했을때
                ToastUtil.show(getString(R.string.gps_permission_request_message));
                ActivityCompat.requestPermissions(this, //  계속 GPS 권한 물어봄
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_GPS);
            } else {
                ActivityCompat.requestPermissions(this, //  최초 실행시 GPS 권한 물어봄
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_GPS);
                ToastUtil.show(getString(R.string.gps_permission_request_message));
            }
        } else {
//            initializeMapView();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {  //  블루투스 권한 승인시 실행
                    initializeApplication();
                } else {    //  블루투스 권한 거부시 실행
                    ToastUtil.show(getString(R.string.bluetooth_permission_request_message));
                    System.exit(EXIT);
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {  // GPS 권한 승인 됬을때
//                    initializeMapView();
        } else {    // GPS 권한 거부 됬을때
            ToastUtil.show(getString(R.string.gps_permission_request_message));
            System.exit(EXIT);
        }
    }

    private void initializeApplication() {
        devices = bluetoothAdapter.getBondedDevices();
        int pairedCount = devices.size();

        if (pairedCount < 1) {
            System.exit(EXIT);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.select_dialog_title));
        ArrayList<String> deviceList = new ArrayList<>();

        for (BluetoothDevice bd : devices) {
            deviceList.add(bd.getName());
        }

        int listSize = devices.size();

        CharSequence[] charSequences = deviceList.toArray(new CharSequence[listSize]);

        builder.setItems(charSequences, (d, w) -> getBluetoothStream(charSequences[w].toString()));

        builder.setCancelable(false);
        builder.create().show();
    }

    @SuppressLint("CheckResult")
    private void getBluetoothStream(String deviceName) {
        for (BluetoothDevice bd : devices) {
            if (deviceName.equals(bd.getName())) {
                bluetoothDevice = bd;
                break;
            }
        }

        UUID uuid = UUID.fromString(BT_UUID);    //  임베디드와 통신하기 위한 블루투스 UUID

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();

            speedReceiver = SpeedReceiver.getInstance(inputStream, outputStream);
            defenseController = DefenseController.getInstance(inputStream, outputStream);
            temperatureController = TemperatureController.getInstance(inputStream, outputStream);
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
                        temp = s.split(":")[1] + getString(R.string.temperature);
                        invisibleUI();
                        loadMainActivity();
                    });

            temperatureController.getTemperature();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void invisibleUI() {
        splashBinding.loadProgress.setVisibility(View.INVISIBLE);
        splashBinding.loadText.setVisibility(View.INVISIBLE);
    }

    private void loadMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.putExtra(KEY_TEMP, temp);
        startActivity(intent);
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
