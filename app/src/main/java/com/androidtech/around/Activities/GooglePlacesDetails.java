package com.androidtech.around.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.androidtech.around.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GooglePlacesDetails extends AppCompatActivity {

    @BindView(R.id.place_title)
    TextView mTitle;
    @BindView(R.id.place_rating)
    TextView mRating;
    @BindView(R.id.place_address)
    TextView mAddress;
    @BindView(R.id.place_phone)
    TextView mPhone;
    String tittle, rating, address, phone;
    String [] placeDetails;
    ActionBar actionBar;

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

}
