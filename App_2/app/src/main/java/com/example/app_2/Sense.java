package com.example.app_2;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sense extends AppCompatActivity {

    private boolean isMultipleModeOn = false;
    private boolean lowConfidenceDetection = false;

    private int maxCount = 3;

    private int maxIteration = 20;
    private int maxIterationForMultisense = 3;
    private int currentIterationForMultiSense = 0;
    private int currentIteration = 0;
    private int currentCount = 1;

    private ArrayList<String> predictionList;

    private Button scanButton;
    private WifiManager wifiManager;

    private TextView cellNoView;

    private TextView c1;
    private TextView c2;
    private TextView c3;
    private TextView c456;
    private TextView c7;
    private TextView c8;
    private TextView c9;
    private TextView c10;
    private TextView c11;
    private TextView c12;
    private TextView c13;
    private TextView c14;
    private TextView c15;
    private TextView c16;


    private GaussianPrediction gaussianPrediction;

    public String findMostFrequentString(ArrayList<String> strings) {
        // Create a frequency map to store string occurrences
        Map<String, Integer> frequencyMap = new HashMap<>();

        // Count occurrences of each string
        for (String str : strings) {
            frequencyMap.put(str, frequencyMap.getOrDefault(str, 0) + 1);
        }

        // Find the string with the highest occurrence
        String mostFrequentString = null;
        int maxOccurrences = 0;

        for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > maxOccurrences) {
                mostFrequentString = entry.getKey();
                maxOccurrences = entry.getValue();
            }
        }

        return mostFrequentString;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sense);

        setBottomNavigationViewListener(findViewById(R.id.bottom_navigator));

        initializeInteractables();
        initializeWifiBroadCaster();
    }

    private void initializeWifiBroadCaster() {
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
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
        Toast.makeText(getApplicationContext(), "No Updated Info received. Scanning again", Toast.LENGTH_SHORT).show();
        locateMeButtonHandler(scanButton);
    }

    private void successActionForWifiBroadCast() {
        // Initiate Prediction based on data read.
        @SuppressLint("MissingPermission") List<ScanResult> scanResults = wifiManager.getScanResults();

        Map<String, Double> bssidRssi = new HashMap<>();
        for (ScanResult scanResult : scanResults) {
            bssidRssi.put(scanResult.BSSID, (double) scanResult.level);
        }

        if (isMultipleModeOn) {

            if (predictionList == null) {
                predictionList = new ArrayList<>();
            }
            if (currentCount >= maxCount) {
                String mostFrequentCell = findMostFrequentString(predictionList);
                highlightDetectedCellNo(mostFrequentCell.substring(5).trim());
                if (lowConfidenceDetection) {
                    lowConfidenceDetection = false;
                    Toast.makeText(getApplicationContext(), "Low confidence Guess", Toast.LENGTH_SHORT).show();
                }
                currentCount = 1;
                predictionList = null;
                scanButton.setText("Locate Me");
                scanButton.setEnabled(true);
                return;
            }
            if (currentIterationForMultiSense < maxIterationForMultisense) {
                String detectedCell = gaussianPrediction.inferCell(bssidRssi, false);
                if (detectedCell != null) {
                    predictionList.add(detectedCell);
                    currentCount++;
                } else {
                    Toast.makeText(getApplicationContext(), "Not enough confidence.Scanning again", Toast.LENGTH_SHORT).show();
                    currentIterationForMultiSense++;
                }
            } else {
                String detectedCell = gaussianPrediction.inferCell(bssidRssi, true);
                predictionList.add(detectedCell);
                currentCount++;
                currentIterationForMultiSense = 0;
                lowConfidenceDetection = true;
            }

            scanButton.setText("Locating..." + currentCount + " of " + maxCount);
            locateMeButtonHandler(scanButton);

        } else {
            String getDetectedCell = gaussianPrediction.inferCell(bssidRssi, false);
            if (getDetectedCell == null) {

                if (currentIteration < maxIteration) {
                    currentIteration++;
                    Toast.makeText(getApplicationContext(), "Not enough confidence.Scanning again", Toast.LENGTH_SHORT).show();
                    locateMeButtonHandler(scanButton);
                } else {
                    Toast.makeText(getApplicationContext(), "Low confidence Guess", Toast.LENGTH_SHORT).show();
                    scanButton.setEnabled(true);
                    scanButton.setText("Locate Me");
                    currentIteration = 0;
                    String getWCDetectedCell = gaussianPrediction.inferCell(bssidRssi, true);
                    highlightDetectedCellNo(getWCDetectedCell.substring(5).trim());
                    currentIteration = 0;

                }
                return;
            }
            highlightDetectedCellNo(getDetectedCell.substring(5).trim());
            scanButton.setText("Locate Me");
            scanButton.setEnabled(true);
            currentIteration = 0;
        }

    }

    private void initializeInteractables() {

        gaussianPrediction = new GaussianPrediction(this);
        gaussianPrediction.loadCellData();
        if (!gaussianPrediction.isGaussianPredictionReady()) {
            Toast.makeText(getApplicationContext(), "Error in reading data", Toast.LENGTH_SHORT).show();
        }

        Toast.makeText(getApplicationContext(), "Data Successfully Loaded", Toast.LENGTH_SHORT).show();

        cellNoView = findViewById(R.id.show_cell_no);
        scanButton = findViewById(R.id.scanButton);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        c1 = findViewById(R.id.c1_view);
        c2 = findViewById(R.id.c2_view);
        c3 = findViewById(R.id.c3_view);
        c456 = findViewById(R.id.c456_view);
        c7 = findViewById(R.id.c7_view);
        c8 = findViewById(R.id.c8_view);
        c9 = findViewById(R.id.c9_view);
        c10 = findViewById(R.id.c10_view);
        c11 = findViewById(R.id.c11_view);
        c12 = findViewById(R.id.c12_view);
        c13 = findViewById(R.id.c13_view);
        c14 = findViewById(R.id.c14_view);
        c15 = findViewById(R.id.c15_view);
        c16 = findViewById(R.id.c16_view);
    }

    public void locateMeButtonHandler(View v) {

        Button scanButton = (Button) v;
        clearAllHighlights();
        cellNoView.setText(" ");

        boolean wifiSuccess = wifiManager.startScan();
        if (!wifiSuccess) {
            Toast.makeText(getApplicationContext(), "Failed to start scan", Toast.LENGTH_SHORT).show();
        } else {
            if (isMultipleModeOn) {
                scanButton.setText("Locating..." + currentCount + " of " + maxCount);
            } else {
                scanButton.setText("Locating...");
            }
            scanButton.setEnabled(false);
        }
    }

    public void multipleModeHandler(View v) {
        Switch multipleSwitch = (Switch) v;
        if (multipleSwitch.isChecked()) {
            isMultipleModeOn = true;
        } else {
            isMultipleModeOn = false;
        }
    }


    private void highlightDetectedCellNo(String cellNo) {
        clearAllHighlights();
        cellNoView.setText(cellNo);
//        Toast.makeText(getApplicationContext(), "Detected Cell " + cellNo, Toast.LENGTH_SHORT).show();
        switch (cellNo) {
            case "1":
                c1.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "2":
                c2.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "3":
                c3.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "4":
            case "5":
            case "6":
                c456.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "7":
                c7.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "8":
                c8.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "9":
                c9.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "10":
                c10.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "11":
                c11.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "12":
                c12.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "13":
                c13.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "14":
                c14.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "15":
                c15.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            case "16":
                c16.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                break;
            default:
                clearAllHighlights();
                break;


        }


    }

    private void clearAllHighlights() {
        c1.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c2.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c3.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c456.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c7.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c8.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c9.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c10.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c11.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c12.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c13.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c14.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c15.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        c16.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void setBottomNavigationViewListener(BottomNavigationView bottomNaviView) {

        bottomNaviView.setSelectedItemId(R.id.sense);


        bottomNaviView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.info) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                if (item.getItemId() == R.id.data) {
                    startActivity(new Intent(getApplicationContext(), DataHunting.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                if (item.getItemId() == R.id.sense) {
                    return true;
                }

                return false;

            }
        });

    }
}
