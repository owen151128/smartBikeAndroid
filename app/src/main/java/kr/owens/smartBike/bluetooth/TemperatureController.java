package kr.owens.smartBike.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import kr.owens.smartBike.util.LogWrapper;

public class TemperatureController {
    private static TemperatureController instance = null;

    private static final String NEW_LINE = "\n";
    private static final String GET_SIG = "getTemp" + NEW_LINE;

    private BehaviorSubject<String> tempSubject = null;

    private int byteBufferIndex = 0;
    private byte[] byteBuffer = null;

    private InputStream bluetoothInputStream = null;
    private OutputStream bluetoothOutputStream = null;

    private TemperatureController(InputStream bluetoothInputStream, OutputStream bluetoothOutputStream) {
        this.bluetoothInputStream = bluetoothInputStream;
        this.bluetoothOutputStream = bluetoothOutputStream;
        tempSubject = BehaviorSubject.create();
    }

    public static synchronized TemperatureController getInstance(InputStream bluetoothInputStream, OutputStream bluetoothOutputStream) {
        if (instance == null) {
            instance = new TemperatureController(bluetoothInputStream, bluetoothOutputStream);
        }

        return instance;
    }

    public static synchronized TemperatureController getInstance() {
        if (instance == null) {
            return null;
        }

        return instance;
    }

    public void getTemperature() {
        try {
            byteBuffer = new byte[1024];
            bluetoothOutputStream.write(GET_SIG.getBytes());
            Thread.sleep(500);
            // 데이터 수신 확인
            int byteAvailable = bluetoothInputStream.available();
            // 데이터가 수신 된 경우
            if (byteAvailable > 0) {
                // 입력 스트림에서 바이트 전부 읽어오기
                byte[] bytes = new byte[byteAvailable];
                bluetoothInputStream.read(bytes);
                LogWrapper.printLog(new String(bytes));
                tempSubject.onNext(new String(bytes));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public Observable<String> receiveTempEvent() {
        return tempSubject;
    }

    public void close() {
        tempSubject.onComplete();
    }
}
