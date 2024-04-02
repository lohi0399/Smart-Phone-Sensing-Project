package com.example.app_2;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DataHunting extends AppCompatActivity {
    private Switch udpSwitch;
    private SendUDPDataTask sendUDPDataTask;
    private EditText ipAddressEditText;
    private EditText portEditText;
    public Spinner cellIdSpinner;

    private WifiManager wifiManager;

    private TextView debugDisplayScreen;
    private String latestDate;
    private String latestTime;

    private SimpleDateFormat timeFormat;
    private SimpleDateFormat dateFormat;

    private boolean isMultipleEnabled = false;

    private int maxCount = 10;

    private int currentCount = 1;

    private Button wifiScanButton;

    private static void writeToRespectiveFile(String[] dataToWrite, File file) throws
            IOException {
        FileWriter fileWriter = new FileWriter(file, true); // Set the append mode to true
        CSVWriter csvWriter = new CSVWriter(fileWriter);
        csvWriter.writeNext(dataToWrite);
        csvWriter.close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_hunting);

        setBottomNavigationViewListener(findViewById(R.id.bottom_navigator));

        initializeInteractabels();

        timeFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    }

    private void initializeInteractabels() {

        wifiScanButton = findViewById(R.id.wifi_scan_button);

        debugDisplayScreen = (TextView) findViewById(R.id.data_debug_screen);
        debugDisplayScreen.setMovementMethod(new ScrollingMovementMethod());

        udpSwitch = findViewById(R.id.udp_switch);
        ipAddressEditText = findViewById(R.id.ipaddr);
        portEditText = findViewById(R.id.port);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        initializeWifiBroadCaster();

        initializeCellSpinner();
    }

    private void initializeWifiBroadCaster() {
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    successActionForWifiBroadCast();
                } else {
                    failureActionForWifiBroadCast();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiScanReceiver, intentFilter);
    }


    private void failureActionForWifiBroadCast() {
        Toast.makeText(getApplicationContext(), "No New Wifi Info Updated", Toast.LENGTH_SHORT).show();
        if (isMultipleEnabled) {
            recordMultipleHandler(findViewById(R.id.record_multiple));
        }
    }

    private void successActionForWifiBroadCast() {
//        Toast.makeText(getApplicationContext(), "SuccessfullyReceivedData", Toast.LENGTH_SHORT).show();
        String cellNumber = cellIdSpinner.getSelectedItem().toString();
        debugDisplayScreen.append("________________________NEW DATA RECEIVED__________\n");
        @SuppressLint("MissingPermission")
        List<ScanResult> scanResults = wifiManager.getScanResults();
        for (ScanResult scanResult : scanResults) {

            StringBuilder populatedResults = populateRequiredData(scanResult);
            debugDisplayScreen.append(populatedResults);
            debugDisplayScreen.append("\n");

            try {
                mapFileToWrite(cellNumber, populatedResults);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "Unable to write to File", Toast.LENGTH_SHORT).show();
            }
        }

        try {
            mapFileToWrite(cellNumber, new StringBuilder(" "));
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Unable to write to File", Toast.LENGTH_SHORT).show();
        }

        if (isMultipleEnabled) {
            currentCount = currentCount + 1;
            if (currentCount > maxCount) {
                currentCount = 1;
                isMultipleEnabled = false;
                Button multipleButton = findViewById(R.id.record_multiple);
                multipleButton.setEnabled(true);
                multipleButton.setText("Record 10 scans");
                return;
            }

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                recordMultipleHandler(findViewById(R.id.record_multiple));
            }, 500);


        } else {
            wifiScanButton.setEnabled(true);
            wifiScanButton.setText("Record Once");
        }


    }

    private StringBuilder populateRequiredData(ScanResult scanResult) {
        StringBuilder dataBuilder = new StringBuilder();
        dataBuilder.append(scanResult.SSID);
        dataBuilder.append(";");
        dataBuilder.append(scanResult.BSSID);
        dataBuilder.append(";");
        dataBuilder.append(scanResult.level);
        dataBuilder.append(";");
        dataBuilder.append(scanResult.frequency);
        dataBuilder.append(";");
        dataBuilder.append(scanResult.centerFreq0);
        dataBuilder.append(";");
        dataBuilder.append(scanResult.centerFreq1);
        dataBuilder.append(";");
        dataBuilder.append(scanResult.channelWidth);
        dataBuilder.append(";");
        dataBuilder.append(getLatestTime());
        dataBuilder.append(";");
        dataBuilder.append(getLatestDate());
        dataBuilder.append(";");

        return dataBuilder;
    }

    private void mapFileToWrite(String cellNumber, StringBuilder dataToWrite) throws IOException {
        String data[] = dataToWrite.toString().split(";");
        switch (cellNumber) {
            case "1":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_1);
                break;
            case "2":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_2);
                break;
            case "3":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_3);
                break;
            case "4":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_4);
                break;

            case "5":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_5);
                break;
            case "6":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_6);
                break;
            case "7":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_7);
                break;
            case "8":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_8);
                break;
            case "9":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_9);
                break;
            case "10":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_10);
                break;
            case "11":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_11);
                break;
            case "12":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_12);
                break;
            case "13":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_13);
                break;
            case "14":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_14);
                break;
            case "15":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_15);
                break;
            case "16":
                writeToRespectiveFile(data, FilePaths.PATH_TO_FILE_16);
                break;
            default:
                Toast.makeText(getApplicationContext(), "Invalid File Selection", Toast.LENGTH_SHORT).show();
                break;

        }
    }

    public void recordWifiDataButtonHandler(View v) {
        Button scanButton = (Button) v;
        boolean wifiSuccess = wifiManager.startScan();
        if (!wifiSuccess) {
            Toast.makeText(getApplicationContext(), "Failed to start scan", Toast.LENGTH_SHORT).show();
            sendDataViaUDP("Failed to start scan");
        } else {
//            Toast.makeText(getApplicationContext(), "Requested New Wifi Scan", Toast.LENGTH_SHORT).show();
            setLatestTime(timeFormat.format(Calendar.getInstance().getTime()));
            setLatestDate(dateFormat.format(Calendar.getInstance().getTime()));
            scanButton.setEnabled(false);
            scanButton.setText("Scan in progress...");
        }
    }


    public void recordMultipleHandler(View v) {
        isMultipleEnabled = true;
        Button scanButton = (Button) v;
        boolean wifiSuccess = wifiManager.startScan();
        if (!wifiSuccess) {
            Toast.makeText(getApplicationContext(), "Failed to start scan", Toast.LENGTH_SHORT).show();
            sendDataViaUDP("Failed to start scan");
            recordMultipleHandler(findViewById(R.id.record_multiple));
        } else {
//            Toast.makeText(getApplicationContext(), "Requested New Wifi Scan", Toast.LENGTH_SHORT).show();
            setLatestTime(timeFormat.format(Calendar.getInstance().getTime()));
            setLatestDate(dateFormat.format(Calendar.getInstance().getTime()));
            scanButton.setEnabled(false);
            scanButton.setText("Scan No " + currentCount + " in progress");
        }
    }


    public void sendDataViaUDP(String data) {
        String ipAddress = ipAddressEditText.getText().toString();
        int port = Integer.valueOf(portEditText.getText().toString());
        sendUDPDataTask = new SendUDPDataTask(ipAddress, port);

        if (udpSwitch.isChecked()) {
            sendUDPDataTask.execute(data);
        }
    }

    private void initializeCellSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = 1; i <= 16; i++) {
            adapter.add(String.valueOf(i));
        }
        cellIdSpinner = findViewById(R.id.cell_id_spinner);
        cellIdSpinner.setAdapter(adapter);
        cellIdSpinner.setSelection(0);
    }

    private void setBottomNavigationViewListener(BottomNavigationView bottomNaviView) {

        bottomNaviView.setSelectedItemId(R.id.data);
        bottomNaviView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.info) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                if (item.getItemId() == R.id.data) {
                    return true;
                }

                if (item.getItemId() == R.id.sense) {
                    startActivity(new Intent(getApplicationContext(), Sense.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;

            }
        });
    }

    public void clearDebugScreenButtonAction(View v) {
        debugDisplayScreen.setText("");
    }

    public String getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(String latestTime) {
        this.latestTime = latestTime;
    }

    public String getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(String latestDate) {
        this.latestDate = latestDate;
    }


    private class SendUDPDataTask extends AsyncTask<String, Void, Void> {
        private String destinationIP;
        private int destinationPort;

        public SendUDPDataTask(String destinationIP, int destinationPort) {
            this.destinationIP = destinationIP;
            this.destinationPort = destinationPort;
        }

        @Override
        protected Void doInBackground(String... params) {
            String data = params[0];

            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] buf = data.getBytes();
                InetAddress destinationAddress = InetAddress.getByName(destinationIP);
                DatagramPacket packet = new DatagramPacket(buf, buf.length, destinationAddress, destinationPort);
                socket.send(packet);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
