package com.openxc.openxcstarter;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.IgnitionStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxcplatform.openxcstarter.R;

public class FinisherActivity extends Activity {
    private static final String TAG = "StarterActivity";
    private VehicleManager mVehicleManager;
    private boolean parkBrake, headLamp;
    private String ignStatus;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finisher);
        //mHeadlampView = (TextView) findViewById(R.id.headlamp);
        //mHandBrakeView = (TextView) findViewById(R.id.park_brake);
        //mGearModeView = (TextView) findViewById(R.id.transmission);
        new finishingMoves().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

            mVehicleManager.removeListener(HeadlampStatus.class,
                    mHeadlampListener);
            mVehicleManager.removeListener(ParkingBrakeStatus.class,
                    mParkBrakeListener);
            mVehicleManager.removeListener(IgnitionStatus.class,
                    mIgnitionListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    private class finishingMoves extends AsyncTask<Void, Integer, Void> {
        int stage = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            View c = findViewById(R.id.park_brake);
            c.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (stage != 8){
                if (stage == 0){ // Park brake
                    if (parkBrake) {
                        publishProgress(stage);
                    }
                } else if (stage == 1) { // Headlamp
                    if (!headLamp) {
                        publishProgress(stage);
                    }
                } else if (stage == 2) { // Windows
                    Button windowsButton = (Button) findViewById(R.id.button_windows);
                    windowsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            publishProgress(stage);
                        }
                    });
                } else if (stage == 3) { // Infotainment
                    Button windowsButton = (Button) findViewById(R.id.button_infotainment);
                    windowsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            publishProgress(stage);
                        }
                    });
                } else if (stage == 4) { // Ignition
                    if (ignStatus.equals("off")) {
                        publishProgress(stage);
                    }
                } else if (stage == 5) { // Stuff to pick up (wallet)
                    Button windowsButton = (Button) findViewById(R.id.button_stuff);
                    windowsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            publishProgress(stage);
                        }
                    });
                } else if (stage == 6) { // Lock your doors :)
                    Button windowsButton = (Button) findViewById(R.id.button_door_lock);
                    windowsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            publishProgress(stage);
                        }
                    });
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (values[0] == 0) {
                View c = findViewById(R.id.park_brake);
                c.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                c = findViewById(R.id.headlamp_reminder);
                c.setVisibility(View.VISIBLE);
                stage++;
            } else if (values[0] == 1) {
                View c = findViewById(R.id.headlamp_reminder);
                c.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                c = findViewById(R.id.button_windows);
                c.setVisibility(View.VISIBLE);
                stage++;
            } else if (values[0] == 2) {
                View c = findViewById(R.id.button_windows);
                c.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                c = findViewById(R.id.button_infotainment);
                c.setVisibility(View.VISIBLE);
                stage++;
            } else if (values[0] == 3) {
                View c = findViewById(R.id.button_infotainment);
                c.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                c = findViewById(R.id.ignition_reminder);
                c.setVisibility(View.VISIBLE);
                stage++;
            } else if (values[0] == 4) {
                View c = findViewById(R.id.ignition_reminder);
                c.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                c = findViewById(R.id.button_stuff);
                c.setVisibility(View.VISIBLE);
                stage++;
            } else if (values[0] == 5) {
                View c = findViewById(R.id.button_stuff);
                c.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                c = findViewById(R.id.button_door_lock);
                c.setVisibility(View.VISIBLE);
                stage++;
            } else if (values[0] == 6) {
                mp.release();
                finish();
            }
        }

    }

    ParkingBrakeStatus.Listener mParkBrakeListener = new ParkingBrakeStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final ParkingBrakeStatus parkingBrakeStatus = (ParkingBrakeStatus) measurement;
            FinisherActivity.this.runOnUiThread(new Runnable() {
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
            FinisherActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    headLamp = headlampStatus.getValue().booleanValue();
                }
            });
        }
    };

    IgnitionStatus.Listener mIgnitionListener = new IgnitionStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final IgnitionStatus ignitionStatus = (IgnitionStatus) measurement;
            FinisherActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ignStatus = ignitionStatus.getValue().getSerializedValue();
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

            mVehicleManager.addListener(HeadlampStatus.class, mHeadlampListener);
            mVehicleManager.addListener(ParkingBrakeStatus.class, mParkBrakeListener);
            mVehicleManager.addListener(IgnitionStatus.class, mIgnitionListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            Toast.makeText(getApplicationContext(), "VehicleManager Service  disconnected unexpectedly", Toast.LENGTH_SHORT).show();
            mVehicleManager = null;
        }
    };
}
