package com.example.locate_me_group_17;

import static com.example.locate_me_group_17.KNNClassifier.readSamplesFromCsv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Sense extends AppCompatActivity implements SensorEventListener {
    BottomNavigationView bottomNavigationView;
    private KNNClassifier classifier,classifier1;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView activityLabel;
    private TextView cellLabel;

    private Switch senseSwitch;

    private WifiManager wifiManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);

        List<KNNClassifier.Sample> samples = readSamplesFromCsv(this, "dataset.csv");
        List<KNNClassifier.Sample> samples1 = readSamplesFromCsv(this, "dataset1.csv");
        senseSwitch = findViewById(R.id.sensing_switch);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.sense);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.data) {
                    startActivity(new Intent(getApplicationContext(), Data.class));

                    overridePendingTransition(0, 0);
                    return true;
                }
                if (item.getItemId() == R.id.sense) {
                    return true;

                }
                if (item.getItemId() == R.id.info) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;

            }
        });

        activityLabel = findViewById(R.id.action);
        cellLabel = findViewById(R.id.cell4);
        // Initialize the sensor manager and accelerometer sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        classifier = new KNNClassifier(4, samples);
        classifier1 = new KNNClassifier(4, samples1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (senseSwitch.isChecked()) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                double[] features = {event.values[0], event.values[1], event.values[2]};
                // Classify activity
                String activity = classifier.classify(features);
                activityLabel.setText(activity);

                // Change the image based on the activity
                ImageView cellImage = findViewById(R.id.change);

                switch (activity) {
                    case "running":
                        cellImage.setImageResource(R.drawable.baseline_directions_run_24);
                        break;
                    case "walking":
                        cellImage.setImageResource(R.drawable.baseline_directions_walk_24);
                        break;
                    case "standing":
                        cellImage.setImageResource(R.drawable.baseline_boy_24);
                        break;

                }
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public void locateMeHandler(View v) {
//    Button locateMe = (Button) v;
        boolean wifiSuccess = wifiManager.startScan();
        if (wifiSuccess) {
            List<String> bssidList = new ArrayList<>();

            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "UNIQUE_BSSID_NAMES.txt");
                FileInputStream fis = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                String line;

                while ((line = br.readLine()) != null) {
                    bssidList.add(line.trim());
                }

                br.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();

            Map<String, Integer> bssidRssiMap = new HashMap<>();
            for (ScanResult scanResult : scanResults) {
                bssidRssiMap.put(scanResult.BSSID, scanResult.level);
            }
            double[] features = new double[bssidList.size()];
            for (int i = 0; i < bssidList.size(); i++) {
                String bssid = bssidList.get(i);
                if (bssidRssiMap.containsKey(bssid)) {
                    features[i] = bssidRssiMap.get(bssid);
                } else {
                    // Here, we handle the case when a BSSID from your file was not found in the scan results.
                    // This could be due to the Wi-Fi network being out of range, or not existing anymore.
                    // A common approach is to use a default value such as -100 (since -100 dBm is a very weak signal).
                    features[i] = -100;
                }
            }
            String cell = classifier1.classify(features);
            cellLabel.setText(cell);

            Toast.makeText(getApplicationContext(), "SUCCESS!", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "Still Old wifi data. Wait", Toast.LENGTH_SHORT).show();
        }
        }

}