package kth.etka.doubleunders;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//Inspired by https://gits-15.sys.kth.se/anderslm/Ble-Gatt-Movesense -2.0

public class AdapterBluetooth extends RecyclerView.Adapter<AdapterBluetooth.ViewHolder> {
    private List<BluetoothDevice> sensorList;

    public interface IOnItemSelectedCallBack {
        void onItemClicked(int position);
    }
    private IOnItemSelectedCallBack mOnItemSelectedCallback;

    public AdapterBluetooth(List<BluetoothDevice> sensors,
                            IOnItemSelectedCallBack onItemSelectedCallback) {
        super();
        sensorList = sensors;
        mOnItemSelectedCallback = onItemSelectedCallback;
    }

    // Represents the the item view, and its internal views
    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView sensorIdView;
        TextView deviceInfoView;

        private IOnItemSelectedCallBack mOnItemSelectedCallback;

        ViewHolder(View itemView, IOnItemSelectedCallBack onItemSelectedCallback) {
            super(itemView);
            itemView.setOnClickListener(this);
            mOnItemSelectedCallback = onItemSelectedCallback;
        }

        // Handles the item (row) being being clicked
        @Override
        public void onClick(View view) {
            int position = getAdapterPosition(); // gets item (row) position
            mOnItemSelectedCallback.onItemClicked(position);
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new item view
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_list_layout, parent, false);
        final ViewHolder vh = new ViewHolder(itemView, mOnItemSelectedCallback);
        vh.sensorIdView = itemView.findViewById(R.id.sensor_id);
        //vh.deviceInfoView = itemView.findViewById(R.id.device_info);

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        BluetoothDevice device = sensorList.get(position);
        String name = device.getName();
        vh.sensorIdView.setText(name == null ? "Unknown" : name);
        //vh.deviceInfoView.setText(device.getBluetoothClass() + ", " + device.getAddress());
        Log.i("ScanActivity", "onBindViewHolder");
    }

    @Override
    public int getItemCount() {
        return sensorList.size();
    }
}
