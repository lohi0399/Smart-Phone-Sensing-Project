package com.example.app_2;

import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBottomNavigationViewListener(findViewById(R.id.bottom_navigator));

        initializeTrainingDataCSVFiles();

    }

    private void initializeTrainingDataCSVFiles() {

        File cell1File = FilePaths.PATH_TO_FILE_1;
        File cell2File = FilePaths.PATH_TO_FILE_2;
        File cell3File = FilePaths.PATH_TO_FILE_3;
        File cell4File = FilePaths.PATH_TO_FILE_4;
        File cell5File = FilePaths.PATH_TO_FILE_5;
        File cell6File = FilePaths.PATH_TO_FILE_6;
        File cell7File = FilePaths.PATH_TO_FILE_7;
        File cell8File = FilePaths.PATH_TO_FILE_8;
        File cell9File = FilePaths.PATH_TO_FILE_9;
        File cell10File = FilePaths.PATH_TO_FILE_10;
        File cell11File = FilePaths.PATH_TO_FILE_11;
        File cell12File = FilePaths.PATH_TO_FILE_12;
        File cell13File = FilePaths.PATH_TO_FILE_13;
        File cell14File = FilePaths.PATH_TO_FILE_14;
        File cell15File = FilePaths.PATH_TO_FILE_15;
        File cell16File = FilePaths.PATH_TO_FILE_16;

        writeFile(cell1File);
        writeFile(cell2File);
        writeFile(cell3File);
        writeFile(cell4File);
        writeFile(cell5File);
        writeFile(cell6File);
        writeFile(cell7File);
        writeFile(cell8File);
        writeFile(cell9File);
        writeFile(cell10File);
        writeFile(cell11File);
        writeFile(cell12File);
        writeFile(cell13File);
        writeFile(cell14File);
        writeFile(cell15File);
        writeFile(cell16File);
    }

    private static void writeFile(File file) {
        try {
            File parentFolder = file.getParentFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }

            boolean fileExists = file.exists();

            FileWriter fileWriter = new FileWriter(file, true); // Set the append mode to true

            // Create a CSVWriter object
            CSVWriter csvWriter = new CSVWriter(fileWriter);

            if (!fileExists) {
                String[] headers = {"SSID", "BSSID", "RSSI", "FREQUENCY", "CF0", "CF1", "CHANNEL WIDTH", "TIME", "DATE"};  // Replace with your actual headers
                csvWriter.writeNext(headers);
            }

            csvWriter.close();
        } catch (IOException e) {
            // Handle Exception
        }
    }

    private void setBottomNavigationViewListener(BottomNavigationView bottomNaviView) {

        bottomNaviView.setSelectedItemId(R.id.info);


        bottomNaviView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.info) {
                    return true;
                }

                if (item.getItemId() == R.id.data) {
                    startActivity(new Intent(getApplicationContext(), DataHunting.class));
                    overridePendingTransition(0, 0);
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

}