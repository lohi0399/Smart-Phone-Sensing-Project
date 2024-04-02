package com.example.app_3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBottomNavigationViewListener(findViewById(R.id.bottom_navigator));
    }


    private void setBottomNavigationViewListener(BottomNavigationView bottomNaviView) {

        bottomNaviView.setSelectedItemId(R.id.info);


        bottomNaviView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.info) {
                    return true;
                }

                if (item.getItemId() == R.id.debug) {
                    startActivity(new Intent(getApplicationContext(), Debug.class));
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