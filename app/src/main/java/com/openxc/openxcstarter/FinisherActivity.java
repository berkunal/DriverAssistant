package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleSpeed;
import com.openxcplatform.openxcstarter.R;

public class FinisherActivity extends Activity {

    private VehicleManager mVehicleManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finisher);

    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            Toast.makeText(getApplicationContext(), "Bound to vehicle manager", Toast.LENGTH_SHORT).show();
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            mVehicleManager.addListener(EngineSpeed.class, mSpeedListener);
            mVehicleManager.addListener(IgnitionStatus.class, mIgnitionListener);
            mVehicleManager.addListener(BrakePedalStatus.class, mBrakePedalListener);
            mVehicleManager.addListener(TransmissionGearPosition.class, mGearModeListener);
            mVehicleManager.addListener(HeadlampStatus.class, mHeadlampListener);
            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.addListener(ParkingBrakeStatus.class, mParkBrakeListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            Toast.makeText(getApplicationContext(), "VehicleManager Service  disconnected unexpectedly", Toast.LENGTH_SHORT).show();
            mVehicleManager = null;
        }
    };
}
