package jp.co.baseed.sample.blesample;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * 特定のデバイスに接続してデータの受信を行う
 */
public class BLEService extends Service implements IBluetoothCommunicator {
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID BLE_TARGET_SERVICE_UUID = UUID.fromString("BFFD2CDD-C6D4-4F53-AE05-F2A0BE431EDE");

    public final static String ACTION_GATT_CHANGE_STATE = "jp.co.baseed.sample.blesample.ACTION_GATT_CHANGE_STATE";
    public final static String ACTION_GATT_DATA_AVAILABLE = "jp.co.baseed.sample.blesample.ACTION_GATT_DATA_AVAILABLE";

    // スキャンコールバック
    private ScanCallbackImpl scan = new ScanCallbackImpl(this );

    // GATT コールバック
    private BluetoothGattCallbackImpl gatt = new BluetoothGattCallbackImpl(this);

    // Bluetooh オブジェクトキャッシュ
    private BluetoothManager cachedBluetoothManager = null;
    private BluetoothAdapter cachedBluetoothAdapter = null;
    private BluetoothLeScanner cachedScanner = null;
    public BluetoothManager getBluetoothManager() {
        if (cachedBluetoothManager==null) {
            cachedBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        }
        return cachedBluetoothManager;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        if (cachedBluetoothAdapter==null) {
            cachedBluetoothAdapter = getBluetoothManager().getAdapter();
        }
        return cachedBluetoothAdapter;
    }
    public BluetoothLeScanner getBluetoothLeScanner() {
        if (cachedScanner==null) {
            cachedScanner = getBluetoothAdapter().getBluetoothLeScanner();
        }
        return cachedScanner;
    }

    // ステータス&タイマー
    static final int TIMER_INTERVAL = 5000;
    private final Timer timer = new Timer();
    public static final int BLESTATE_BLUETOOTH_UNAVAILABLE = 1;
    public static final int BLESTATE_START_SCANNING = 2;
    public static final int BLESTATE_FAILED_SCANNING = 3;
    public static final int BLESTATE_START_CONNECTING = 4;
    public static final int BLESTATE_DISCOVERING_SERVICE = 5;
    public static final int BLESTATE_WAIT_CHARACTERISTIC = 6;
    public static final int BLESTATE_DISCONNECTED = 7;

    private int bleState = BLESTATE_BLUETOOTH_UNAVAILABLE;
    private long changedBleStateTime = System.currentTimeMillis();
    private int failedScanCount = 0;
    private int failedConnectCount = 0;

    private boolean isBound = false;
    private BluetoothGatt gattProfile;

    public class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.this;
        }
    }
    private final IBinder binder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        startTimer();
        return binder;
    }
    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        return super.onUnbind(intent);
    }

    private void startTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                onTimer();
            }
        }, 0, TIMER_INTERVAL);
    }
    private void changeBleState(int newState) {
        changedBleStateTime = System.currentTimeMillis();
        bleState = newState;
    }
    private long measureBleStateInterval() {
        return System.currentTimeMillis() - changedBleStateTime;
    }

    private void startScan() {
        ScanSettings setting = new ScanSettings.Builder()
        // CALLBACK_TYPE_FIRST_MATCH と CALLBACK_TYPE_MATCH_LOST はハードウェア依存で動作しない場合が多い
        //                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH)
                .build();
        ScanFilter filter = new ScanFilter.Builder()
                // filter指定の場合、バックグラウンドでのスキャンが可能
                .setServiceUuid(new ParcelUuid(BLE_TARGET_SERVICE_UUID))
                .build();
        changeBleState(BLESTATE_START_SCANNING);
        printLog("BLEService: start scanning");
        getBluetoothLeScanner().startScan(Arrays.asList(filter), setting, scan);
    }

    @Override
    public void onScanResult() {
        if (scan.hasError()) {
            changeBleState(BLESTATE_FAILED_SCANNING);
            return;
        }
        printLog("BLEService: success scan");
        connectGatt();
    }
    private void connectGatt() {
        BluetoothDevice device = scan.getTargetDevice();
        changeBleState(BLESTATE_START_CONNECTING);
        printLog("BLEService: start connecting");
        gattProfile = device.connectGatt(this, true, gatt);
    }
    @Override
    public void onConnectGatt() {
        printLog("BLEService: connected");
        gattProfile.discoverServices();
        changeBleState(BLESTATE_DISCOVERING_SERVICE);
    }
    @Override
    public void onDiscoverService(boolean success) {
        //TODO: failed
        printLog("BLEService: discover service > " + success);
        BluetoothGattService service = gattProfile.getService(BLE_TARGET_SERVICE_UUID);
        List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
        for (BluetoothGattCharacteristic characteristic:characteristics) {
            // notify 形式の Characteristic を想定
            boolean result = gattProfile.setCharacteristicNotification(characteristic, true);
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gattProfile.writeDescriptor(descriptor);

//            gattProfile.readCharacteristic(characteristic);
        }
        changeBleState(BLESTATE_WAIT_CHARACTERISTIC);
    }

    @Override
    public void onDisconnectGatt() {
        printLog("BLEService: disconnected");
        changeBleState(BLESTATE_DISCONNECTED);
        ++failedConnectCount;
    }
    @Override
    public void onCharacteristic(BluetoothGattCharacteristic characteristic, boolean read) {
        String value = characteristic.getStringValue(0);
        if (value==null) {
            printLog("BLEService: characteristic > null");
        }else{
            printLog("BLEService: characteristic > " + value);
        }
    }
    private void onTimer() {
        Log.i("BLE", "BLEService: onTimer bleStat> " + bleState);
        if (bleState == BLESTATE_BLUETOOTH_UNAVAILABLE) {
            if (checkBluetoothAvailable()) {
                startScan();
                return;
            }
        }
        else {
            if (!checkBluetoothAvailable()) {
                //TODO: 通知
                // 一旦サービス終了
                printLog("BLEService: Bluetooth unavailable. stop service");
                stop();
                return;
            }
        }
        long bleStateInterval = measureBleStateInterval();
        if (bleState == BLESTATE_START_SCANNING) {
            // スキャンに時間がかかっているようならサービスを一旦停止する
            if (10*1000 < bleStateInterval) {
                getBluetoothLeScanner().stopScan(scan);
                printLog("BLEService: scan timeout. stop service");
                stop();
            }
            return;
        }
        if (bleState == BLESTATE_START_CONNECTING) {
            // 接続に時間がかかっているようならサービスを一旦停止する
            if (10*1000 < bleStateInterval) {
                printLog("BLEService: connect timeout. stop service");
                stop();
            }
            return;
        }
    }
    private boolean checkBluetoothAvailable() {
        BluetoothAdapter adapter = getBluetoothAdapter();
        if (adapter == null || !adapter.isEnabled()) {
            //TODO: 通知処理
            return false;
        }
        return true;
    }
    void stop()
    {
        timer.cancel();
        stopSelf();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public BLEService() {
        super();
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
//    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    void printLog(String s) {
        Log.i("BLE", s);
        System.out.print(s+"\n");
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//    }
//
//    @Override
//    public void onTrimMemory(int level) {
//        super.onTrimMemory(level);
//    }
//
//    @Override
//    public void onRebind(Intent intent) {
//        super.onRebind(intent);
//    }
//
//    @Override
//    public void onTaskRemoved(Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//    }
//
//    @Override
//    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
//        super.dump(fd, writer, args);
//    }

}
