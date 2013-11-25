package com.duethealth.test.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.duethealth.lib.component.DhBaseAdapter;
import com.google.android.gms.internal.p;

import java.util.ArrayList;

import butterknife.InjectView;
import butterknife.Views;

public class MainActivity extends Activity {

    private static final int REQUEST_ENABLE_BT = 100;

    @InjectView(R.id.container)
    View mContainer;
    @InjectView(R.id.tv_ble_on)
    TextView mTvBleOn;
    @InjectView(R.id.list)
    ListView mList;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBtManager;

    private BtArrayAdapter mListAdapter;

    private Handler mHandler;

    private boolean mScanning;
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Views.inject(this);

        mBtManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBtManager.getAdapter();

        if (!checkEnabled()) {
            return;
        }

        mHandler = new Handler();
        scaneBle(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mListAdapter = new BtArrayAdapter(this);
        mList.setAdapter(mListAdapter);
    }

    private void scaneBle(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private boolean checkEnabled() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mListAdapter.addItem(device);
                    mListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private class BtArrayAdapter extends DhBaseAdapter<BluetoothDevice> {
        public BtArrayAdapter(Context context) {
            super(context);
            items = new ArrayList<BluetoothDevice>();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            TextView tv = null;
            if (convertView == null) {
                tv = new TextView(MainActivity.this);
                tv.setPadding(8, 8, 8 , 8);
            } else {
                tv = (TextView) convertView;
            }

            BluetoothDevice device = items.get(position);
            tv.setText(device.getName());

            return tv;
        }
    }

}
