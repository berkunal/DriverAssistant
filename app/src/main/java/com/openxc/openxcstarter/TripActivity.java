package com.openxc.openxcstarter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.VehicleSpeed;
import com.openxcplatform.openxcstarter.R;

public class TripActivity extends Activity {
    private static final String TAG = "TripActivity";

    private TextView mVehicleSpeedView;
    private VehicleManager mVehicleManager;
    private double vSpeed;
    private boolean parkBrake, headLamp;
    private int accPedalPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        new safetyWarning().execute(null, null, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");
            mVehicleManager.removeListener(VehicleSpeed.class,
                    mVehicleSpeedListener);
            mVehicleManager.removeListener(ParkingBrakeStatus.class,
                    mParkBrakeListener);
            mVehicleManager.removeListener(HeadlampStatus.class,
                    mHeadlampListener);
            mVehicleManager.removeListener(AcceleratorPedalPosition.class,
                    mAcceleratorPedalListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    private class safetyWarning extends AsyncTask<Void, Integer, Void> {
        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this);
        AlertDialog handbrakeDialog, headlampDialog;
        int flag = 1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.setMessage(R.string.handbrake_warning)
                    .setTitle(R.string.safety_warning);
            handbrakeDialog = builder.create();
            builder.setMessage(R.string.headlamp_warning)
                    .setTitle(R.string.safety_warning);
            headlampDialog = builder.create();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                // flag == 1 -> no warning
                // Handbrake during cruse. 0 -> handbrake warning
                if (( ( vSpeed > 0 || accPedalPosition > 3 ) && parkBrake ) && flag == 1) {
                    flag = 0;
                    publishProgress(flag);
                } else if ((( (vSpeed > 0 || accPedalPosition > 3) && !parkBrake) || (vSpeed == 0 && accPedalPosition < 3)) && flag == 0) {
                    flag = 1;
                    publishProgress(flag);
                }

                // Closed headlamps during cruse. 2 -> headlamp warnings
                if (!headLamp && flag == 1) {
                    flag = 2;
                    publishProgress(flag);
                } else if (headLamp && flag == 2) {
                    flag = 3;
                    publishProgress(flag);
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 0) {
                handbrakeDialog.show();
            } else if (values[0] == 1) {
                handbrakeDialog.dismiss();
            } else if (values[0] == 2) {
                headlampDialog.show();
            } else if (values[0] == 3) {
                headlampDialog.dismiss();
                flag = 1;
            }
        }
    }

    VehicleSpeed.Listener mVehicleSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed vehicleSpeed = (VehicleSpeed) measurement;
            TripActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    vSpeed = vehicleSpeed.getValue().doubleValue();
                    mVehicleSpeedView.setText(getString(R.string.vehicle_speed, vSpeed));
                }
            });
        }
    };

    ParkingBrakeStatus.Listener mParkBrakeListener = new ParkingBrakeStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final ParkingBrakeStatus parkingBrakeStatus = (ParkingBrakeStatus) measurement;
            TripActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    parkBrake = parkingBrakeStatus.getValue().booleanValue();
                }
            });
        }
    };

    HeadlampStatus.Listener mHeadlampListener = new HeadlampStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final HeadlampStatus headlampStatus = (HeadlampStatus) measurement;
            TripActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    headLamp = headlampStatus.getValue().booleanValue();
                }
            });
        }
    };

    VehicleSpeed.Listener mAcceleratorPedalListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final AcceleratorPedalPosition acceleratorPedalPosition = (AcceleratorPedalPosition) measurement;
            TripActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    accPedalPosition = acceleratorPedalPosition.getValue().intValue();
                    Log.i(TAG, "acc pedal pos: " + accPedalPosition);
                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            Toast.makeText(getApplicationContext(), "Bound to vehicle manager", Toast.LENGTH_SHORT).show();
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.addListener(ParkingBrakeStatus.class, mParkBrakeListener);
            mVehicleManager.addListener(HeadlampStatus.class, mHeadlampListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAcceleratorPedalListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            Toast.makeText(getApplicationContext(), "VehicleManager Service  disconnected unexpectedly", Toast.LENGTH_SHORT).show();
            mVehicleManager = null;
        }
    };
}
