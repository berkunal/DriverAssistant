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
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.TransmissionGearPosition;
import com.openxc.measurements.VehicleSpeed;
import com.openxcplatform.openxcstarter.R;
import com.openxc.VehicleManager;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.EngineSpeed;
import com.openxc.measurements.IgnitionStatus;

public class StarterActivity extends Activity {
    private static final String TAG = "StarterActivity";

    private VehicleManager mVehicleManager;
    private TextView mEngineSpeedView, mIgnitionView, mBrakePedalView, mHandBrakeView, mGearModeView, mHeadlampView, mVehicleSpeedView;
    private double eSpeed, vSpeed;
    private String ignStatus, gearPosition;
    private boolean brakePedal, parkBrake, headLamp;
    private MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        // grab a reference to the engine speed text object in the UI, so we can
        // manipulate its value later from Java code
        mEngineSpeedView = (TextView) findViewById(R.id.engine_speed);
        mIgnitionView = (TextView) findViewById(R.id.ignition_status);
        mBrakePedalView = (TextView) findViewById(R.id.brake_pedal);
        mHandBrakeView = (TextView) findViewById(R.id.hand_brake);
        mGearModeView = (TextView) findViewById(R.id.gear_mode);
        mHeadlampView = (TextView) findViewById(R.id.headlamp);
        mVehicleSpeedView = (TextView) findViewById(R.id.vehicle_speed);
        Button startButton = (Button) findViewById(R.id.main_button);
        Button seatButton = (Button) findViewById(R.id.button_seat);
        Button mirrorButton = (Button) findViewById(R.id.button_mirror);
        Button seatBeltButton = (Button) findViewById(R.id.button_seatbelt);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View b = findViewById(R.id.main_button);
                b.setVisibility(View.GONE);
                View c = findViewById(R.id.button_seat);
                c.setVisibility(View.VISIBLE);
            }
        });
        seatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View b = findViewById(R.id.button_seat);
                b.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                View c = findViewById(R.id.button_mirror);
                c.setVisibility(View.VISIBLE);
            }
        });
        mirrorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View b = findViewById(R.id.button_mirror);
                b.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                View c = findViewById(R.id.button_seatbelt);
                c.setVisibility(View.VISIBLE);
            }
        });
        seatBeltButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View b = findViewById(R.id.button_seatbelt);
                b.setVisibility(View.GONE);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
                mp.start();
                View c = findViewById(R.id.lamp);
                c.setVisibility(View.VISIBLE);
                new headlampCheck().execute(null, null, null);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mVehicleManager != null) {
            Log.i(TAG, "Unbinding from Vehicle Manager");

            mVehicleManager.removeListener(EngineSpeed.class,
                    mSpeedListener);
            mVehicleManager.removeListener(IgnitionStatus.class,
                    mIgnitionListener);
            mVehicleManager.removeListener(BrakePedalStatus.class,
                    mBrakePedalListener);
            mVehicleManager.removeListener(TransmissionGearPosition.class,
                    mGearModeListener);
            mVehicleManager.removeListener(HeadlampStatus.class,
                    mHeadlampListener);
            mVehicleManager.removeListener(VehicleSpeed.class,
                    mVehicleSpeedListener);
            mVehicleManager.removeListener(ParkingBrakeStatus.class,
                    mParkBrakeListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mVehicleManager == null) {
            Intent intent = new Intent(this, VehicleManager.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    EngineSpeed.Listener mSpeedListener = new EngineSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final EngineSpeed speed = (EngineSpeed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mEngineSpeedView.setText(getString(R.string.engine_speed, speed.getValue().doubleValue()));
                    eSpeed = speed.getValue().doubleValue();
                }
            });
        }
    };

    IgnitionStatus.Listener mIgnitionListener = new IgnitionStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final IgnitionStatus ignitionStatus = (IgnitionStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mIgnitionView.setText(getString(R.string.ignition_status, ignitionStatus.getValue().getSerializedValue()));
                    ignStatus = ignitionStatus.getValue().getSerializedValue();
                }
            });
        }
    };

    BrakePedalStatus.Listener mBrakePedalListener = new BrakePedalStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final BrakePedalStatus brakePedalStatus = (BrakePedalStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mBrakePedalView.setText(getString(R.string.brake_pedal, brakePedalStatus.getValue().booleanValue()));
                    brakePedal = brakePedalStatus.getValue().booleanValue();
                }
            });
        }
    };

    ParkingBrakeStatus.Listener mParkBrakeListener = new ParkingBrakeStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final ParkingBrakeStatus parkingBrakeStatus = (ParkingBrakeStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mHandBrakeView.setText(getString(R.string.hand_brake, parkingBrakeStatus.getValue().booleanValue()));
                    parkBrake = parkingBrakeStatus.getValue().booleanValue();
                }
            });
        }
    };

    TransmissionGearPosition.Listener mGearModeListener = new TransmissionGearPosition.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final TransmissionGearPosition gearMode = (TransmissionGearPosition) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mGearModeView.setText(getString(R.string.gear_mode, gearMode.getValue().getSerializedValue()));
                    gearPosition = gearMode.getValue().getSerializedValue();
                }
            });
        }
    };

    HeadlampStatus.Listener mHeadlampListener = new HeadlampStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final HeadlampStatus headlampStatus = (HeadlampStatus) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mHeadlampView.setText(getString(R.string.headlamp, headlampStatus.getValue().booleanValue()));
                    headLamp = headlampStatus.getValue().booleanValue();
                }
            });
        }
    };

    VehicleSpeed.Listener mVehicleSpeedListener = new VehicleSpeed.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleSpeed vehicleSpeed = (VehicleSpeed) measurement;
            StarterActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mVehicleSpeedView.setText(getString(R.string.vehicle_speed_info, vehicleSpeed.getValue().doubleValue()));
                    vSpeed = vehicleSpeed.getValue().doubleValue();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.starter, menu);
        return true;
    }

    private class headlampCheck extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (!headLamp){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            View b = findViewById(R.id.lamp);
            b.setVisibility(View.GONE);
            mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
            mp.start();
            View c = findViewById(R.id.brake);
            c.setVisibility(View.VISIBLE);
            new brakeCheck().execute(null, null, null);
        }
    }

    private class brakeCheck extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (!brakePedal){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            View b = findViewById(R.id.brake);
            b.setVisibility(View.GONE);
            mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
            mp.start();
            View c = findViewById(R.id.handbrake);
            c.setVisibility(View.VISIBLE);
            new parkBrakeCheck().execute(null, null, null);
        }
    }

    private class parkBrakeCheck extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (parkBrake){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            View b = findViewById(R.id.handbrake);
            b.setVisibility(View.GONE);
            mp = MediaPlayer.create(getApplicationContext(), R.raw.ping);
            mp.start();
            View c = findViewById(R.id.ready);
            c.setVisibility(View.VISIBLE);
            new vSpeedCheck().execute(null, null, null);
        }
    }

    private class vSpeedCheck extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (vSpeed < 2) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            View b = findViewById(R.id.ready);
            b.setVisibility(View.GONE);
            mp.release();
            Intent i = new Intent(getApplicationContext(), TripActivity.class);
            startActivity(i);
            finish();
        }
    }
}
