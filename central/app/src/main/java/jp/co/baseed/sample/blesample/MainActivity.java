package jp.co.baseed.sample.blesample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_ENABLE_BT = 10;
    private BLEService bleService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BLEService.LocalBinder)service).getService();
            onBindBLEService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };
    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BLEService.ACTION_GATT_CHANGE_STATE)) {
                onChangeStateBLEService();
            } else if (action.equals(BLEService.ACTION_GATT_DATA_AVAILABLE)) {
                onDataAvailableBLEService();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startTextLogView();

        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bindBLEService();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            onResultEnableBt(resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // registerReceiver
        {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(BLEService.ACTION_GATT_CHANGE_STATE);
            filter.addAction(BLEService.ACTION_GATT_DATA_AVAILABLE);
            registerReceiver(gattUpdateReceiver, filter);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private void onResultEnableBt(int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            System.out.print("Bluetooth disabled.\n");
            return;
        }
        bindBLEService();
    }
    private void bindBLEService() {
        if (bleService != null) {
            System.out.print("bindBLEService: already bind.\n");
            return;
        }
        Intent gattServiceIntent = new Intent(this, BLEService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }
    private void onBindBLEService() {
        System.out.print("bindBLEService: bind.\n");
    }
    private void onChangeStateBLEService() {
        System.out.print("bindBLEService: state > ");
    }
    private void onDataAvailableBLEService(){
//        System.out.print("bindBLEService: bind.\n");
    }

    private void startTextLogView() {
        TextView view = (TextView)findViewById(R.id.textlog);
        System.setOut(new TextViewPrintStream(System.out, view));
        System.setErr(new TextViewPrintStream(System.err, view));
    }
}