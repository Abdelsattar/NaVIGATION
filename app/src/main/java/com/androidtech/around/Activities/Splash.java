package com.androidtech.around.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.androidtech.around.R;
import com.androidtech.around.Utils.SharedPrefUtilities;

public class Splash extends AppCompatActivity {

    SharedPrefUtilities mSharedPrefUtilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);

        Thread timer = new Thread() {
            public void run() {

                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    //  Create a new boolean and preference and set it to true
                    boolean isFirstStart = mSharedPrefUtilities.getFirstOpen();

                    if(isFirstStart){
                        mSharedPrefUtilities.setFirstOpen(false);
                        //  Launch app intro
                        Intent intent = new Intent(Splash.this, IntroActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    }else{
                        //  Launch app intro
                        Intent intent = new Intent(Splash.this, MainActivity.class);
                        intent.putExtra("Splash","Splash");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    }

                }
            }

        };
        timer.start();
    }

}
