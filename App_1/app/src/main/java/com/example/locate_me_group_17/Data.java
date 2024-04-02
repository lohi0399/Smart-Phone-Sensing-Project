package com.example.locate_me_group_17;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.opencsv.CSVWriter;

import org.apache.commons.lang3.ObjectUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Data extends AppCompatActivity implements SensorEventListener {

    private Button buttonRssi;
    private Button StartAcc;
    private Button StopAcc;
    private Switch udpSwitch;

    private EditText cellNumber;

    private BottomNavigationView bottomNavigationView;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private WifiInfo wifiInfo;
    private float aX = 0;
    private float aY = 0;
    private float aZ = 0;
    private TextView currentX, currentY, currentZ, titleAcc, textRssi;
    private boolean isAccActive = false;
    private boolean fileEmpty = true;
    private WifiManager wifiManager;
    private ArrayList<String> uniqueBSSIDs;

    int columnCount = 300;
    int defaultValue = -100;
    int sumSignalStrength = 0;
    int countSignalStrength = 0;

    class NetworkInfo {
        String SSID;
        int level;

        NetworkInfo(String SSID, int level) {
            this.SSID = SSID;
            this.level = level;
        }
    }

    private static void writeToRespectiveFile(String[] dataToWrite, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file, true); // Set the append mode to true
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        csvWriter.writeNext(dataToWrite);
        csvWriter.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        udpSwitch = findViewById(R.id.udp_switch);
        cellNumber = (EditText) findViewById(R.id.cell_Number);

        // Set the wifi manager
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.data);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.data) {
                    return true;
                } else if (item.getItemId() == R.id.sense) {
                    startActivity(new Intent(getApplicationContext(), Sense.class));
                    overridePendingTransition(0, 0);
                    return true;

                } else if (item.getItemId() == R.id.info) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else {
                    return false;
                }

            }
        });
        currentX = (TextView) findViewById(R.id.currentX);
        currentY = (TextView) findViewById(R.id.currentY);
        currentZ = (TextView) findViewById(R.id.currentZ);
        titleAcc = (TextView) findViewById(R.id.titleAcc);
        textRssi = (TextView) findViewById(R.id.textRSSI);

        // Create the button
        buttonRssi = (Button) findViewById(R.id.buttonRSSI);
        StartAcc = (Button) findViewById(R.id.StartAcc);
        StopAcc = (Button) findViewById(R.id.StopAcc);

        // Set the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Initializing the HashMap for getting unique BSSIDs

        HashMap<String, NetworkInfo> uniBSSIDs = new HashMap<>();

        // This ArrayList will keep track of the order of the BSSIDs

        uniqueBSSIDs = new ArrayList<>();

        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    onSuccessAction(timeFormat, dateFormat, uniBSSIDs, uniqueBSSIDs);
                } else {
                    onFailureAction();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);


        // Create a click listener for our wifi button.
        buttonRssi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wifiSuccess = wifiManager.startScan();
                if (!wifiSuccess) {
                    Toast.makeText(getApplicationContext(), "Failed to start scan", Toast.LENGTH_SHORT).show();
                }
            }
        });


        //--- Wifi Button End --- //

        // if the default accelerometer exists
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // set accelerometer
            accelerometer = sensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // register 'this' as a listener that updates values. Each time a sensor value changes,
            // the method 'onSensorChanged()' is called.

        } else {
            // No accelerometer!
        }


        //--- Accelerometer Start Button ---//
        StartAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start or resume the accelerometer sensor
                if (!isAccActive) {
                    sensorManager.registerListener(Data.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    isAccActive = true;
                }
            }
        });
        //--- Accelerometer Start Button End ---//

        //--- Accelerometer Stop Button ---//

        StopAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stop or pause the accelerometer sensor
                if (isAccActive) {
                    sensorManager.unregisterListener(Data.this);
                    isAccActive = false;
                }
            }
        });
        //--- Accelerometer Stop Button End ---//


    }

    private void onFailureAction() {
        if (udpSwitch.isChecked()) {
            new SendUDPDataTask().execute("Waiting");
        }
        Toast.makeText(getApplicationContext(), "Wifi has to Wait", Toast.LENGTH_SHORT).show();
    }

    private void onSuccessAction(SimpleDateFormat timeFormat, SimpleDateFormat dateFormat, HashMap<String, NetworkInfo> uniBSSIDs,ArrayList<String> uniqueBSSIDs) {
        @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();

        // Creating a hashset to only store values of required ssid

        HashSet<String> targetSSIDs = new HashSet<>();
        targetSSIDs.add("eduroam");
        targetSSIDs.add("tudelft-dastud");
        targetSSIDs.add("TUD-facility");
        targetSSIDs.add("Vierambacht 45A_5G");

        for (ScanResult result : scanResults) {
            if (targetSSIDs.contains(result.SSID)) {
                if (!uniBSSIDs.containsKey(result.BSSID) && uniqueBSSIDs.size() < columnCount) {
                    // If the BSSID is not already in our map and we still have space, add it
                    uniBSSIDs.put(result.BSSID, new NetworkInfo(result.SSID, defaultValue));
                    uniqueBSSIDs.add(result.BSSID);
                }
                // Update the signal strength of the BSSID
                uniBSSIDs.get(result.BSSID).level = result.level;
                sumSignalStrength += result.level;
                countSignalStrength++;
            }
        }

// Calculate the mean signal strength
        int meanSignalStrength = countSignalStrength > 0 ? sumSignalStrength / countSignalStrength : defaultValue;

// If we didn't scan enough BSSIDs, fill the rest with default values
        while (uniqueBSSIDs.size() < columnCount) {
            String defaultBSSID = "BSSID_" + (uniqueBSSIDs.size() + 1);
            uniBSSIDs.put(defaultBSSID, new NetworkInfo("defaultSSID", defaultValue));
            uniqueBSSIDs.add(defaultBSSID);
        }
        // Impute missing values with the mean
//        for (String bssid : uniBSSIDs.keySet()) {
//            NetworkInfo info = uniBSSIDs.get(bssid);
//            if (info.level == defaultValue) {
//                info.level = meanSignalStrength;
//            }
//        }


        // Log unique BSSIDs
        for (String bssid : uniqueBSSIDs) {
            Log.d("Unique BSSID", bssid);
        }

        // Write the results to a CSV fil
        StringBuilder dataBuilder0 = new StringBuilder();
        try {
            // Write data
            for (String bssid : uniqueBSSIDs) {
                NetworkInfo info = uniBSSIDs.get(bssid);
                dataBuilder0.append(String.valueOf(info.level));
                dataBuilder0.append(";");
            }
            dataBuilder0.append(timeFormat.format(Calendar.getInstance().getTime()));
            dataBuilder0.append(";");
            dataBuilder0.append(dateFormat.format(Calendar.getInstance().getTime()));
            dataBuilder0.append(";");

            mapToRespectiveFile(Integer.valueOf(cellNumber.getText().toString()), dataBuilder0); // Writing or Appending to the corresponding csv
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Unable to write to File", Toast.LENGTH_SHORT).show();
        }

        // Build the data string with "BSSID, RSSI" columns for each scan
        StringBuilder dataBuilder = new StringBuilder();
        for (String bssid : uniBSSIDs.keySet()) {
            NetworkInfo info = uniBSSIDs.get(bssid);
            dataBuilder.append(bssid).append(",").append(info.level).append(",");
        }

        // Remove the trailing comma
        if (dataBuilder.length() > 0) {
            dataBuilder.setLength(dataBuilder.length() - 1);
        }

        // Build the formatted text for display
        StringBuilder displayBuilder = new StringBuilder();
        for (String bssid : uniBSSIDs.keySet()) {
            NetworkInfo info = uniBSSIDs.get(bssid);
            displayBuilder.append(bssid).append("\t").append(info.level).append("\n");
        }

        displayBuilder.append("Local Time: ").append(timeFormat.format(Calendar.getInstance().getTime())).append("\n");
        displayBuilder.append(";");
        displayBuilder.append("Local Date: ").append(dateFormat.format(Calendar.getInstance().getTime()));
        displayBuilder.append(";");

        // update the text.
        textRssi.setText(displayBuilder.toString());

        // Send the wifi data over UDP for training

        if (udpSwitch.isChecked()) {
            String dataToSend = dataBuilder.toString() + "," + Calendar.getInstance().getTime();
            new SendUDPDataTask().execute(dataToSend);
        }
        // Log unique BSSIDs and count the number that don't start with "BSSID"
        int realBSSIDCount = 0;
        for (String bssid : uniqueBSSIDs) {
            if (bssid.startsWith("BSSID")) {
                continue;
            }
            realBSSIDCount++;
            Log.d("Unique BSSID", bssid);
        }
        Toast.makeText(getApplicationContext(), "SUCCESS! --> # of Unique Access Points: " + realBSSIDCount, Toast.LENGTH_SHORT).show();
    }

    private void mapToRespectiveFile(int cellNumber, StringBuilder dataToWrite) throws IOException {
        String data[] = dataToWrite.toString().split(";");
        switch (cellNumber) {
            case 1:
                File cell1File = new File(MainActivity.PATHTOFILE1);
                writeToRespectiveFile(data, cell1File);
                break;
            case 2:
                File cell2File = new File(MainActivity.PATHTOFILE2);
                writeToRespectiveFile(data, cell2File);
                break;
            case 3:
                File cell3File = new File(MainActivity.PATHTOFILE3);
                writeToRespectiveFile(data, cell3File);
                break;
            case 4:
                File cell4File = new File(MainActivity.PATHTOFILE4);
                writeToRespectiveFile(data, cell4File);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Invalid Cell number. Cannot write", Toast.LENGTH_SHORT).show();
                break;
        }

    }


    public void exportAction(View v){
        if(uniqueBSSIDs == null){
            Toast.makeText(getApplicationContext(), "Null object. Could not export", Toast.LENGTH_SHORT).show();
            return;
        }

        File txtFile;
        FileWriter txtWriter = null;

        try {
//            File txtDir = new File(getApplicationContext().getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyTextFiles");
            File txtDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath());

            if (!txtDir.exists()) {
                txtDir.mkdirs();
            }

            txtFile = new File(txtDir, "UNIQUE_BSSID_NAMES.txt");
            txtWriter = new FileWriter(txtFile);

            // Iterate through the ArrayList
            for (String rowData : uniqueBSSIDs) {
                // Write each String in the ArrayList to the file
                txtWriter.append(rowData);
                txtWriter.append("\n");
            }

            txtWriter.flush();

        } catch (IOException e) {
            // Handle exception
            Toast.makeText(getApplicationContext(), "Error writing to TXT file", Toast.LENGTH_SHORT).show();
        } finally {
            if (txtWriter != null) {
                try {
                    txtWriter.close();
                } catch (IOException e) {
                    // Handle exception
                    Toast.makeText(getApplicationContext(), "Error closing FileWriter", Toast.LENGTH_SHORT).show();
                }
            }
        }
        Toast.makeText(getApplicationContext(), "Successfully_Written_to_TXT", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing.
    }

    // onResume() registers the accelerometer for listening the events

    @Override
    public void onSensorChanged(SensorEvent event) {
        // get the the x,y,z values of the accelerometer
        aX = event.values[0];
        aY = event.values[1];
        aZ = event.values[2];

        // display the current x,y,z accelerometer values
        currentX.setText(Float.toString(aX));
        currentY.setText(Float.toString(aY));
        currentZ.setText(Float.toString(aZ));

        // Sending the accelerometer data via UDP for training
        String dataToSend = aX + "," + aY + "," + aZ;
        if (udpSwitch.isChecked()) {
            new SendUDPDataTask().execute(dataToSend);
        }

        if ((Math.abs(aX) > Math.abs(aY)) && (Math.abs(aX) > Math.abs(aZ))) {
            titleAcc.setTextColor(Color.RED);
        }
        if ((Math.abs(aY) > Math.abs(aX)) && (Math.abs(aY) > Math.abs(aZ))) {
            titleAcc.setTextColor(Color.BLUE);
        }
        if ((Math.abs(aZ) > Math.abs(aY)) && (Math.abs(aZ) > Math.abs(aX))) {
            titleAcc.setTextColor(Color.GREEN);
        }

    }

    private class SendUDPDataTask extends AsyncTask<String, Void, Void> {
        private EditText ipaddress = (EditText) findViewById(R.id.ipaddr);
        private final String DESTINATION_IP = ipaddress.getText().toString(); // Replace with the destination IP address
        private EditText port = (EditText) findViewById(R.id.port);
        private final int DESTINATION_PORT = Integer.valueOf(port.getText().toString()); // Replace with the destination port

        @Override
        protected Void doInBackground(String... params) {
            String data = params[0];

            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] buf = data.getBytes();
                InetAddress destinationAddress = InetAddress.getByName(DESTINATION_IP);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, destinationAddress, DESTINATION_PORT);
                socket.send(packet);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}