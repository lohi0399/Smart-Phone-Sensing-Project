package com.example.locate_me_group_17;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    public static final String CELL_1_DATA_CSV = "Cell_1_Data.csv";
    public static final String PATHTOFILE1 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + File.separator + CELL_1_DATA_CSV;
    public static final String CELL_2_DATA_CSV = "Cell_2_Data.csv";
    public static final String PATHTOFILE2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + File.separator + CELL_2_DATA_CSV;
    public static final String CELL_3_DATA_CSV = "Cell_3_Data.csv";
    public static final String PATHTOFILE3 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + File.separator + CELL_3_DATA_CSV;
    public static final String CELL_4_DATA_CSV = "Cell_4_Data.csv";
    public static final String PATHTOFILE4 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + File.separator + CELL_4_DATA_CSV;

    BottomNavigationView bottomNavigationView;

    private static void writeFile(File file) {
        try {
            boolean fileExists = file.exists();

            FileWriter fileWriter = new FileWriter(file, true); // Set the append mode to true

            // Create a CSVWriter object
            CSVWriter csvWriter = new CSVWriter(fileWriter);
            csvWriter.close();
        } catch (IOException e) {
            // Handle Exception
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.info);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.info) {
                    return true;
                }
                if (item.getItemId() == R.id.sense) {
                    startActivity(new Intent(getApplicationContext(), Sense.class));

                    overridePendingTransition(0, 0);
                    return true;

                }
                if (item.getItemId() == R.id.data) {
                    startActivity(new Intent(getApplicationContext(), Data.class));

                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;

            }
        });


        createNecessaryExcel_Files();

    }

    private void createNecessaryExcel_Files() {
        File cell1File = new File(PATHTOFILE1);
        File cell2File = new File(PATHTOFILE2);
        File cell3File = new File(PATHTOFILE3);
        File cell4File = new File(PATHTOFILE4);

        writeFile(cell1File);
        writeFile(cell2File);
        writeFile(cell3File);
        writeFile(cell4File);

    }



}