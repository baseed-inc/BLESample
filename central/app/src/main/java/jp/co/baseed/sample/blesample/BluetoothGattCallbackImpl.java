package jp.co.baseed.sample.blesample;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Looper;

public class BluetoothGattCallbackImpl extends BluetoothGattCallback {
    private Handler handler = new Handler(Looper.getMainLooper());
    private IBluetoothCommunicator btObject;

    public BluetoothGattCallbackImpl(IBluetoothCommunicator btObject) {
        super();
        this.btObject = btObject;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    btObject.onConnectGatt();
                }
            });
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    btObject.onDisconnectGatt();
                }
            });
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        final boolean success = status == BluetoothGatt.GATT_SUCCESS;
        handler.post(new Runnable() {
            @Override
            public void run() {
                btObject.onDiscoverService(success);
            }
        });
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            System.out.print("BluetoothGattCallbackImpl: failed CharacteristicRead");
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                btObject.onCharacteristic(characteristic, true);
            }
        });
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                btObject.onCharacteristic(characteristic, false);
            }
        });
    }
//    @Override
//    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
//        super.onPhyUpdate(gatt, txPhy, rxPhy, status);
//    }
//
//    @Override
//    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
//        super.onPhyRead(gatt, txPhy, rxPhy, status);
//    }
//
//    @Override
//    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//        super.onCharacteristicWrite(gatt, characteristic, status);
//    }
//
//    @Override
//    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//        super.onDescriptorRead(gatt, descriptor, status);
//    }
//
//    @Override
//    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
//        super.onDescriptorWrite(gatt, descriptor, status);
//    }
//
//    @Override
//    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
//        super.onReliableWriteCompleted(gatt, status);
//    }
//
//    @Override
//    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
//        super.onReadRemoteRssi(gatt, rssi, status);
//    }
//
//    @Override
//    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
//        super.onMtuChanged(gatt, mtu, status);
//    }
}
