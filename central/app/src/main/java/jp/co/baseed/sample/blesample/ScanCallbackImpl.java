package jp.co.baseed.sample.blesample;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;


/**
 * 指定した1つのデバイスのみを取得する
 */
public class ScanCallbackImpl  extends ScanCallback {
    private Handler handler = new Handler(Looper.getMainLooper());
    private IBluetoothCommunicator btObject;

    private BluetoothDevice targetDevice;
    private ScanResult targetScanResult;

//        // Fails to start scan as BLE scan with the same settings is already started by the app.
//        public static final int SCAN_FAILED_ALREADY_STARTED = 1;
//        // Fails to start scan as app cannot be registered.
//        public static final int SCAN_FAILED_APPLICATION_REGISTRATION_FAILED = 2;
//        // Fails to start scan due an internal error
//        public static final int SCAN_FAILED_INTERNAL_ERROR = 3;
//        // Fails to start power optimized scan as this feature is not supported.
//        public static final int SCAN_FAILED_FEATURE_UNSUPPORTED = 4;
//        // Fails to start scan as it is out of hardware resources.
//        public static final int SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES = 5;
//        Fails to start scan as application tries to scan too frequently.
//        public static final int SCAN_FAILED_SCANNING_TOO_FREQUENTLY = 6;
//        static final int NO_ERROR = 0;
    private int lastErrorCode = 0; //NO_ERROR;

    public BluetoothDevice getTargetDevice() {
        return targetDevice;
    }
    public ScanResult getScanResult() {
        return targetScanResult;
    }
    public boolean hasError() {
        return lastErrorCode != 0;
    }
    public int getError() {
        return lastErrorCode;
    }

    public ScanCallbackImpl(IBluetoothCommunicator btObject) {
        super();
        this.btObject = btObject;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        // 最初に見つかったデバイスのみを使用する
        if (targetDevice != null) {
            return;
        }
        targetDevice = result.getDevice();
        targetScanResult = result;
        triggerScanResult();
    }

//unsupported.
//    @Override
//    public void onBatchScanResults(List<ScanResult> results) {
//        super.onBatchScanResults(results);
//        btObject.getBluetoothLeScanner().stopScan(this);
//    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        lastErrorCode = errorCode;
        triggerScanResult();
    }

    private void triggerScanResult() {
        btObject.getBluetoothLeScanner().stopScan(this);
        handler.post(new Runnable() {
            @Override
            public void run() {
                btObject.onScanResult();
            }
        });
    }
}
