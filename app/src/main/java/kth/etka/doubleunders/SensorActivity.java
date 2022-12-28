package kth.etka.doubleunders;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import java.util.List;
import java.util.UUID;

public class SensorActivity extends AppCompatActivity {

    private BluetoothDevice currentDevice = null;
    private BluetoothGatt bluetoothGatt = null;

    private final String IMU_COMMAND = "Meas/IMU6/13"; // see documentation
    private final byte MOVESENSE_REQUEST = 1, MOVESENSE_RESPONSE = 2, REQUEST_ID = 99;

    Handler mHandler;

    TextView deviceNameView;

    // Movesense 2.0 UUIDs (should be placed in resources file)
    public static final UUID MOVESENSE_2_0_SERVICE =
            UUID.fromString("34802252-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_2_0_COMMAND_CHARACTERISTIC =
            UUID.fromString("34800001-7185-4d5d-b431-630e7050e8f0");
    public static final UUID MOVESENSE_2_0_DATA_CHARACTERISTIC =
            UUID.fromString("34800002-7185-4d5d-b431-630e7050e8f0");

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG =
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");



    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        Intent intent = getIntent();

        deviceNameView = findViewById(R.id.device_name);

        currentDevice = intent.getParcelableExtra(MainActivity.SELECTED_DEVICE);
        if (currentDevice == null) {
            deviceNameView.setText("No device");
        } else {
            deviceNameView.setText(currentDevice.getName());
        }
        mHandler = new Handler();
    }
    public static byte[] stringToAsciiArray(byte id, String command) {
        if (id > 127) throw new IllegalArgumentException("id= " + id);
        char[] chars = command.trim().toCharArray();
        byte[] ascii = new byte[chars.length + 2];
        ascii[0] = 1;
        ascii[1] = id;
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] > 127) throw new IllegalArgumentException("ascii val= " + (int) chars[i]);
            ascii[i + 2] = (byte) chars[i];
        }
        return ascii;
    }
    @SuppressLint("NewApi")
    private final BluetoothGattCallback btGattCallback = new BluetoothGattCallback() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                bluetoothGatt = gatt;
                /*mHandler.post(new Runnable() {
                    public void run() {
                    }
                });
                 */

                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Close connection and display info in ui
                bluetoothGatt = null;
                mHandler.post(new Runnable() {
                    public void run() {
                        deviceNameView.setText("Disconnected");
                    }
                });
            }
        }

        @SuppressLint("RestrictedApi")
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Debug: list discovered services
                List<BluetoothGattService> services = gatt.getServices();
                for (BluetoothGattService service : services) {
                    Log.i(LOG_TAG, service.getUuid().toString());
                }


                BluetoothGattService movesenseService = gatt.getService(MOVESENSE_2_0_SERVICE);
                if (movesenseService != null) {

                    List<BluetoothGattCharacteristic> characteristics =
                            movesenseService.getCharacteristics();
                    for (BluetoothGattCharacteristic chara : characteristics) {
                        Log.i(LOG_TAG, chara.getUuid().toString());
                    }


                    BluetoothGattCharacteristic commandCharAcc =
                            movesenseService.getCharacteristic(
                                    MOVESENSE_2_0_COMMAND_CHARACTERISTIC);

                    byte[] commandAcc =
                            stringToAsciiArray(REQUEST_ID, IMU_COMMAND);
                    commandCharAcc.setValue(commandAcc);

                    boolean wasSuccessAcc = bluetoothGatt.writeCharacteristic(commandCharAcc);

                    Log.i("writeCharacteristic", "was success=" + wasSuccessAcc);

                } else {
                    mHandler.post(new Runnable() {
                        public void run() {

                        }
                    });
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (currentDevice != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                bluetoothGatt =
                        currentDevice.connectGatt(this, false, btGattCallback);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
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