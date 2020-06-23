package jp.co.baseed.sample.blesample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;

/**
 * 他のクラスからのアクセスを明示させる
 */
public interface IBluetoothCommunicator {
    BluetoothManager getBluetoothManager();
    BluetoothAdapter getBluetoothAdapter();
    BluetoothLeScanner getBluetoothLeScanner();

    void onScanResult();
    void onConnectGatt();
    void onDiscoverService(boolean success);
    void onDisconnectGatt();
    void onCharacteristic(BluetoothGattCharacteristic characteristic, boolean read);
}
