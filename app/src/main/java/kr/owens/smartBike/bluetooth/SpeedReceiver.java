package kr.owens.smartBike.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import kr.owens.smartBike.util.LogWrapper;

public class SpeedReceiver {
    private static SpeedReceiver instance = null;

    private static final String NEW_LINE = "\n";
    private static final String GET_SIG = "getSpeed" + NEW_LINE;

    private BehaviorSubject<String> speedSubject = null;

    private int byteBufferIndex = 0;
    private byte[] byteBuffer = null;

    private InputStream bluetoothInputStream = null;
    private OutputStream bluetoothOutputStream = null;

    private SpeedReceiver(InputStream bluetoothInputStream, OutputStream bluetoothOutputStream) {
        this.bluetoothInputStream = bluetoothInputStream;
        this.bluetoothOutputStream = bluetoothOutputStream;
        speedSubject = BehaviorSubject.create();
    }

    public static synchronized SpeedReceiver getInstance(InputStream bluetoothInputStream, OutputStream bluetoothOutputStream) {
        if (instance == null) {
            instance = new SpeedReceiver(bluetoothInputStream, bluetoothOutputStream);
        }

        return instance;
    }

    public static synchronized SpeedReceiver getInstance() {
        if (instance == null) {
            return null;
        }

        return instance;
    }

    public void getSpeed() {
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
                speedSubject.onNext(new String(bytes));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    public Observable<String> receiveSpeedEvent() {
        return speedSubject;
    }

    public void close() {
        speedSubject.onComplete();
    }
}
