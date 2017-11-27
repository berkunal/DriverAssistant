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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.openxc.VehicleManager;
import com.openxc.measurements.AcceleratorPedalPosition;
import com.openxc.measurements.BrakePedalStatus;
import com.openxc.measurements.HeadlampStatus;
import com.openxc.measurements.Measurement;
import com.openxc.measurements.ParkingBrakeStatus;
import com.openxc.measurements.VehicleDoorStatus;
import com.openxc.measurements.VehicleSpeed;
import com.openxcplatform.openxcstarter.R;

public class TripActivity extends Activity {
    private static final String TAG = "TripActivity";

    private TextView mVehicleSpeedView;
    private VehicleManager mVehicleManager;
    private double vSpeed;
    private boolean parkBrake, headLamp, brakePedal;
    private int accPedalPosition;
    private String doorAjar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);
        Button endButton = (Button) findViewById(R.id.end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( vSpeed > 1 ) {
                    Toast.makeText(getApplicationContext(), "Stop your car first!", Toast.LENGTH_LONG).show();
                } else {
                    Intent i = new Intent(getApplicationContext(), FinisherActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        });
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
            mVehicleManager.removeListener(BrakePedalStatus.class,
                    mBrakePedalListener);
            mVehicleManager.removeListener(VehicleDoorStatus.class,
                    mDoorAjarListener);
            unbindService(mConnection);
            mVehicleManager = null;
        }
    }

    private class safetyWarning extends AsyncTask<Void, Integer, Void> {
        AlertDialog.Builder builder = new AlertDialog.Builder(TripActivity.this);
        AlertDialog handbrakeDialog, headlampDialog, brakeGasDialog, aggressiveAccSafetyDialog, aggressiveAccEmissionDialog, safetyPlusEmissionDialog;
        int flag = 1, i = 0;
        double acc, vSpeedPrev;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            vSpeedPrev = vSpeed;
            builder.setMessage(R.string.handbrake_warning)
                    .setTitle(R.string.safety_warning);
            handbrakeDialog = builder.create();
            builder.setMessage(R.string.headlamp_warning)
                    .setTitle(R.string.safety_warning);
            headlampDialog = builder.create();
            builder.setMessage(R.string.brake_acc_warning)
                    .setTitle(R.string.emission_warning);
            brakeGasDialog = builder.create();
            builder.setMessage(R.string.aggressive_acc_warning)
                    .setTitle(R.string.safety_warning);
            aggressiveAccSafetyDialog = builder.create();
            builder.setMessage(R.string.aggressive_acc_warning)
                    .setTitle(R.string.emission_warning);
            aggressiveAccEmissionDialog = builder.create();
            builder.setMessage(R.string.aggressive_acc_warning)
                    .setTitle(R.string.safety_and_emission_warning);
            safetyPlusEmissionDialog = builder.create();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (true) {
                // Calculate acceleration
                acc = (vSpeed - vSpeedPrev)*2;
                vSpeedPrev = vSpeed;

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

                // Brake + gas pedal warning 4
                if ((brakePedal && accPedalPosition > 3) && flag == 1) {
                    flag = 4;
                    publishProgress(flag);
                } else if ( (!brakePedal || accPedalPosition < 3) && flag == 4 ) {
                    flag = 5;
                    publishProgress(flag);
                }

                // Aggressive acceleration safety warning -> 6
                if (vSpeed >= 0 && vSpeed < 20 && flag == 1){
                    if (acc >= 2.6) {
                        // safety + emission warning
                        flag = 10;
                        publishProgress(flag);
                    } else if (acc >= 2.16) {//safety
                        flag = 6;
                        publishProgress(flag);
                    }
                } else if (vSpeed >= 20 && vSpeed < 30 && flag == 1) {
                    if (acc >= 2.06) {
                        // safety + emission warning
                        flag = 10;
                        publishProgress(flag);
                    } else if (acc >= 1.73) {//emission
                        flag = 8;
                        publishProgress(flag);
                    }
                } else if (vSpeed >= 30 && vSpeed < 40 && flag == 1) {
                    if (acc >= 1.96) {
                        // safety + emission warning
                        flag = 10;
                        publishProgress(flag);
                    } else if (acc >= 1.3) {//emission
                        flag = 8;
                        publishProgress(flag);
                    }
                } else if (vSpeed >= 40 && vSpeed < 50 && flag == 1) {
                    if (acc >= 1.86) {
                        // safety + emission warning
                        flag = 10;
                        publishProgress(flag);
                    } else if (acc >= 1.1) {//emission
                        flag = 8;
                        publishProgress(flag);
                    }
                } else if (vSpeed >= 50 && vSpeed < 70 && flag == 1) {
                    if (acc >= 1.47) {
                        // safety + emission warning
                        flag = 10;
                        publishProgress(flag);
                    } else if (acc >= 0.83) {//emission
                        flag = 8;
                        publishProgress(flag);
                    }
                } else if (vSpeed >= 70 && vSpeed < 80 && flag == 1) {
                    if (acc >= 1.37) {
                        // safety + emission warning
                        flag = 10;
                        publishProgress(flag);
                    } else if (acc >= 0.60) {//emission
                        flag = 8;
                        publishProgress(flag);
                    }
                } else if (vSpeed >= 80 && flag == 1) {
                    if (acc >= 1.27) {
                        // safety + emission warning
                        flag = 10;
                        publishProgress(flag);
                    } else if (acc >= 0.55) {//emission
                        flag = 8;
                        publishProgress(flag);
                    }
                }

                if (flag == 6) {
                    i++;
                    if (i == 4){
                        flag = 7;
                        i = 0;
                        publishProgress(flag);
                    }
                } else if (flag == 8) {
                    i++;
                    if (i == 4){
                        flag = 9;
                        i = 0;
                        publishProgress(flag);
                    }
                } else if (flag == 10) {
                    i++;
                    if (i == 4){
                        flag = 11;
                        i = 0;
                        publishProgress(flag);
                    }
                }

                // Kapılar bozuk :(
                // Door open warning 6
                // Log.i(TAG, "door: " + doorAjar);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
            } else if (values[0] == 4) {
                brakeGasDialog.show();
            } else if (values[0] == 5) {
                brakeGasDialog.dismiss();
                flag = 1;
            } else if (values[0] == 6) {
                aggressiveAccSafetyDialog.show();
            } else if (values[0] == 7) {
                aggressiveAccSafetyDialog.dismiss();
                flag = 1;
            } else if (values[0] == 8) {
                aggressiveAccEmissionDialog.show();
            } else if (values[0] == 9) {
                aggressiveAccEmissionDialog.dismiss();
                flag = 1;
            } else if (values[0] == 10) {
                safetyPlusEmissionDialog.show();
            } else if (values[0] == 11) {
                safetyPlusEmissionDialog.dismiss();
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
                }
            });
        }
    };

    BrakePedalStatus.Listener mBrakePedalListener = new BrakePedalStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final BrakePedalStatus brakePedalStatus = (BrakePedalStatus) measurement;
            TripActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    brakePedal = brakePedalStatus.getValue().booleanValue();
                }
            });
        }
    };

    VehicleDoorStatus.Listener mDoorAjarListener = new VehicleDoorStatus.Listener() {
        @Override
        public void receive(Measurement measurement) {
            final VehicleDoorStatus vehicleDoorStatus = (VehicleDoorStatus) measurement;
            TripActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    doorAjar = vehicleDoorStatus.getValue().getSerializedValue();
                }
            });
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.i(TAG, "Bound to VehicleManager");
            mVehicleManager = ((VehicleManager.VehicleBinder) service)
                    .getService();

            mVehicleManager.addListener(VehicleSpeed.class, mVehicleSpeedListener);
            mVehicleManager.addListener(ParkingBrakeStatus.class, mParkBrakeListener);
            mVehicleManager.addListener(HeadlampStatus.class, mHeadlampListener);
            mVehicleManager.addListener(AcceleratorPedalPosition.class, mAcceleratorPedalListener);
            mVehicleManager.addListener(BrakePedalStatus.class, mBrakePedalListener);
            mVehicleManager.addListener(VehicleDoorStatus.class, mDoorAjarListener);
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.w(TAG, "VehicleManager Service  disconnected unexpectedly");
            Toast.makeText(getApplicationContext(), "VehicleManager Service  disconnected unexpectedly", Toast.LENGTH_SHORT).show();
            mVehicleManager = null;
        }
    };
}
