package kth.etka.doubleunders;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.UUID;

public class SensorActivity extends AppCompatActivity {

    private BluetoothDevice currentDevice = null;
    private BluetoothGatt bluetoothGatt = null;

    // Movesense 2.0 UUIDs (should be placed in resources file)
    public static final UUID MOVESENSE_2_0_SERVICE =
            UUID.fromString("34802252-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_2_0_COMMAND_CHARACTERISTIC =
            UUID.fromString("34800001-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_2_0_DATA_CHARACTERISTIC =
            UUID.fromString("34800002-7185-4d5d-b431-630e7050e8f0");

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        Intent intent = getIntent();

        currentDevice = intent.getParcelableExtra(MainActivity.SELECTED_DEVICE);
        if (currentDevice == null) {
            deviceNameView.setText("No device");
        } else {
            deviceNameView.setText(currentDevice.getName());
        }
        mHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentDevice != null) {
            bluetoothGatt =
                    currentDevice.connectGatt(this, false, btGattCallback);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            try {
                bluetoothGatt.close();
            } catch (Exception e) {
            }
        }
    }
}