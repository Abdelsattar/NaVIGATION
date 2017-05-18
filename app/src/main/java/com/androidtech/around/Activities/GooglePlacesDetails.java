package com.androidtech.around.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidtech.around.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GooglePlacesDetails extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.google_driving)
    ImageView driving;
    @BindView(R.id.google_bicycling)
    ImageView bicycling;
    @BindView(R.id.google_walking)
    ImageView walking;
    @BindView(R.id.place_title)
    TextView mTitle;
    @BindView(R.id.place_rating)
    TextView mRating;
    @BindView(R.id.place_address)
    TextView mAddress;
    @BindView(R.id.place_phone)
    TextView mPhone;
    String tittle, rating, address, phone, lat, lng;
    String [] placeDetails;
    ActionBar actionBar;

    static String DRIVING_TAG = "driving";
    static String WALKING_TAG = "walking";
    static String BICYCLING_TAG = "bicycling";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_places_details);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        if (intent != null) {
            placeDetails = intent.getStringArrayExtra("extra_place");
            tittle = placeDetails[0];
            rating = placeDetails[1];
            address = placeDetails[2];
            phone = placeDetails[3];
            lat = placeDetails[4];
            lng = placeDetails[5];
            adaptViews();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        driving.setOnClickListener(this);
        bicycling.setOnClickListener(this);
        walking.setOnClickListener(this);
    }

    private void adaptViews() {
        mTitle.setText(tittle);
        if (rating!=null){
            mRating.setText(rating);
        }
        mAddress.setText(address);
        if (phone!=null){
            mPhone.setText(phone);
        }
        setTitle(tittle);
    }

    private void launchNavigationIntent(String navMethod){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr="+lat+","+lng+"&mode="+navMethod));
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.google_driving:
                launchNavigationIntent(DRIVING_TAG);
                break;

            case R.id.google_walking:
                launchNavigationIntent(WALKING_TAG);
                break;

            case R.id.google_bicycling:
                launchNavigationIntent(BICYCLING_TAG);
                break;
        }
    }
}
