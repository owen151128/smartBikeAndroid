package kr.owens.smartBike.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DefenseController {
    private static DefenseController instance = null;

    private static final String NEW_LINE = "\n";
    private static final String ON_SIG = "setDefanse:On" + NEW_LINE;
    private static final String SIG_OFF = "setDefanse:Off" + NEW_LINE;

    private InputStream bluetoothInputStream = null;
    private OutputStream bluetoothOutputStream = null;

    private DefenseController(InputStream bluetoothInputStream, OutputStream bluetoothOutputStream) {
        this.bluetoothInputStream = bluetoothInputStream;
        this.bluetoothOutputStream = bluetoothOutputStream;
    }

    public static synchronized DefenseController getInstance(InputStream bluetoothInputStream, OutputStream bluetoothOutputStream) {
        if (instance == null) {
            instance = new DefenseController(bluetoothInputStream, bluetoothOutputStream);
        }

        return instance;
    }

    public static synchronized DefenseController getInstance() {
        if(instance == null) {
            return null;
        }

        return instance;
    }

    public void defenseModeOn() {
        try {
            bluetoothOutputStream.write(ON_SIG.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void defenseModeOff() {
        try {
            bluetoothOutputStream.write(SIG_OFF.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
