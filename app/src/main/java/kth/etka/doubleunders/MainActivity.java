package kth.etka.doubleunders;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String MOVESENSE = "Movesense";

    public static int REQUEST_ENABLE_BT = 1000;
    public static int REQUEST_ACCESS_LOCATION = 1001;

    public static String SELECTED_DEVICE = "Selected device";

    private static final long SCAN_PERIOD = 5000; // milliseconds

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    private ArrayList<BluetoothDevice> mDeviceList;
    private AdapterBluetooth mBtDeviceAdapter;
    private TextView mScanInfoView;


    private Context context;
    /*private Camera mCamera;
    private CameraPreview mPreview;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create our Preview view and set it as the content of our activity.
        /*
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

         */


        //*** BLUETOOTH CONNECTION ***//
        mDeviceList = new ArrayList<>();
        mHandler = new Handler();
        mScanInfoView = findViewById(R.id.scan_info);

        Button startScanButton = findViewById(R.id.search_devices);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                mDeviceList.clear();
                scanForDevices(true);
            }
        });


        RecyclerView recyclerView = findViewById(R.id.scan_list_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mBtDeviceAdapter = new AdapterBluetooth(mDeviceList,
                new AdapterBluetooth.IOnItemSelectedCallBack() {
                    @Override
                    public void onItemClicked(int position) {
                        onDeviceSelected(position);
                    }
                });
        recyclerView.setAdapter(mBtDeviceAdapter);


    }



    //**** BLUETOOTH ****// - FROM IMU APP
    @SuppressLint("SetTextI18n")
    @Override
    protected void onStart() {
        super.onStart();
        mScanInfoView.setText("No device found");
        initBLE();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onStop() {
        super.onStop();
        // stop scanning
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanForDevices(false);
        }
        mDeviceList.clear();
        mBtDeviceAdapter.notifyDataSetChanged();
    }


    private void initBLE() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            finish();
        } else {

            int hasAccessLocation = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (hasAccessLocation != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ACCESS_LOCATION);

            }
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // turn on BT
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }


    private void onDeviceSelected(int position) {
        BluetoothDevice selectedDevice = mDeviceList.get(position);

        Intent intent = new Intent(MainActivity.this, SensorActivity.class);
        intent.putExtra(SELECTED_DEVICE, selectedDevice);
        startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void scanForDevices(final boolean enable) {
        final BluetoothLeScanner scanner =
                mBluetoothAdapter.getBluetoothLeScanner();
        if (enable) {
            if (!mScanning) {

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mScanning) {
                            mScanning = false;
                            scanner.stopScan(mScanCallback);

                        }
                    }
                }, SCAN_PERIOD);

                mScanning = true;

                scanner.startScan(mScanCallback);
                mScanInfoView.setText("No devices found");

            }
        } else {
            if (mScanning) {
                mScanning = false;
                scanner.stopScan(mScanCallback);

            }
        }
    }


    @SuppressLint("NewApi")
    private ScanCallback mScanCallback = new ScanCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            final BluetoothDevice device = result.getDevice();
            final String name = device.getName();

            mHandler.post(new Runnable() {
                @SuppressLint("RestrictedApi")
                public void run() {
                    if (name != null
                            && name.contains(MOVESENSE)
                            && !mDeviceList.contains(device)) {
                        mDeviceList.add(device);
                        mBtDeviceAdapter.notifyDataSetChanged();
                        String info = "Found " + mDeviceList.size() + " device(s)\n"
                                + "Touch to connect";
                        mScanInfoView.setText(info);
                        Log.i(LOG_TAG, device.toString());
                    }
                }
            });
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.i(LOG_TAG, "onBatchScanResult");
        }

        @SuppressLint("RestrictedApi")
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.i(LOG_TAG, "onScanFailed");
        }
    };



    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_LOCATION) {

            if (grantResults.length == 0
                    || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                this.finish();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if user chooses not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}