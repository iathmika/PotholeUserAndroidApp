package com.example.potholeuserandroidapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.example.potholeuserandroidapp.Activities.CaptchaActivity;
import com.example.potholeuserandroidapp.Activities.HomeActivity;
import com.example.potholeuserandroidapp.Activities.LoginActivity;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetClient;

import static com.example.potholeuserandroidapp.R.layout.activity_main;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("PREFERENCES",MODE_PRIVATE);

        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn",false);

        if(isLoggedIn){
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }else{
            startActivity(new Intent(MainActivity.this, CaptchaActivity.class));
            finish();
        }
    }



}
