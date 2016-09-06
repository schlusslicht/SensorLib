package de.fau.teksample;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import de.fau.sensorlib.DsBleSensor;
import de.fau.sensorlib.DsSensor;
import de.fau.sensorlib.DsSensorManager;
import de.fau.sensorlib.KnownSensor;
import de.fau.sensorlib.SensorDataProcessor;
import de.fau.sensorlib.SensorFoundCallback;
import de.fau.sensorlib.dataframe.SensorDataFrame;
import de.fau.sensorlib.sensors.TEK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /**
     * Reference to our connected sensor.
     */
    DsBleSensor mSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // For Android 6+ we have to make sure that we have the BT-LE permissions
        DsSensorManager.checkBtLePermissions(this, true);
    }

    /**
     * The data handler for our sensor which will receive callbacks whenever new data is available
     */
    SensorDataProcessor mDataHandler = new SensorDataProcessor() {
        @Override
        public void onNewData(SensorDataFrame data) {
            // This callback is called every time a new dataframe is available from the sensor.

            // We first cast the dataframe to our TekDataFrame
            TEK.TekDataFrame df = (TEK.TekDataFrame) data;

            // We can now access the elements of the dataframe
            Log.d(TAG, "DataFrame (" + df.getCounter() + "): " + df.toString());
        }
    };

    /**
     * Helper method to retrieve instance pointer in nested classes.
     *
     * @return
     */
    Activity getThis() {
        return this;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This callback is called by Android every time the app resumes control

        // This codeblock allows to connect to a sensor directly.
        /*String tekMac = "52:4D:4B:5F:01:55";
        mSensor = new TEK(this, tekMac, mDataHandler);
        mSensor.useHardwareSensor(DsSensor.HardwareSensor.ACCELEROMETER);
        mSensor.useHardwareSensor(DsSensor.HardwareSensor.LIGHT);
        try {
            mSensor.connect();
            mSensor.startStreaming();
        } catch (Exception e) {
            e.printStackTrace();
        }*/


        // Search for available BT LE devices
        try {
            DsSensorManager.searchBleDevices(this, new SensorFoundCallback() {
                public boolean onKnownSensorFound(KnownSensor sensor) {
                    // This is called whenever a new BLE sensor was found that can be accessed via the SensorLib.
                    Log.d(TAG, "BLE Sensor found: " + sensor.getDeviceName());

                    // Check if it is a TEK sensor
                    if (sensor == KnownSensor.TEK) {
                        // It is a TEK: create the sensor...
                        mSensor = new TEK(getThis(), sensor, mDataHandler);
                        // ...select the desired hardware sensors...
                        mSensor.useHardwareSensor(DsSensor.HardwareSensor.ACCELEROMETER);
                        mSensor.useHardwareSensor(DsSensor.HardwareSensor.LIGHT);
                        try {
                            // ...connect to it...
                            mSensor.connect();
                            // ...and start streaming data.
                            // New data will now appear in the callback above.
                            mSensor.startStreaming();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        // If we lose control we disconnect the sensor.
        if (mSensor != null) {
            mSensor.disconnect();
        }
        super.onPause();
    }
}
