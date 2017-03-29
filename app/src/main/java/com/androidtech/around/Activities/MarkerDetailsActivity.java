package com.androidtech.around.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidtech.around.Models.BusinessPlace;
import com.androidtech.around.Models.User;
import com.androidtech.around.R;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.PicassoUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.androidtech.around.Activities.ConversionActivity.USER_EXTRA;

public class MarkerDetailsActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.business_title)
    TextView title;
    @BindView(R.id.business_phone)
    TextView phone;
    @BindView(R.id.business_email)
    TextView email;
    @BindView(R.id.category)
    TextView category;
    @BindView(R.id.subcategory)
    TextView sub_category;
    @BindView(R.id.district)
    TextView district;
    @BindView(R.id.city)
    TextView city;
    @BindView(R.id.label_about_uts)
    TextView label_about;
    @BindView(R.id.business_about)
    TextView about;
    @BindView(R.id.business_image)
    ImageView image;
    @BindView(R.id.driving)
    ImageView driving;
    @BindView(R.id.bicycling)
    ImageView bicycling;
    @BindView(R.id.walking)
    ImageView walking;
    @BindView(R.id.fab_chat)
    FloatingActionButton fab_chat;

    static String DRIVING_TAG = "driving";
    static String WALKING_TAG = "walking";
    static String BICYCLING_TAG = "bicycling";
    public int MY_CALL_PERMISSION = 111;

    BusinessPlace place;
    User user;
    private Dialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addLeftDrawables();

        customDialog();

        driving.setOnClickListener(this);
        bicycling.setOnClickListener(this);
        walking.setOnClickListener(this);

        place = new BusinessPlace();

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            place = extra.getParcelable("extra_place");
            getPlaceOwner(place.getOwner_id());
            updateView();
        }

        if (FirebaseUtil.getCurrentUserId()!=null){
            if (FirebaseUtil.getCurrentUserId().equals(place.getOwner_id()))
                fab_chat.setVisibility(View.INVISIBLE);
        }else
            fab_chat.setVisibility(View.INVISIBLE);


        fab_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null ) {
                    Parcelable wrapped = Parcels.wrap(user);
                    Intent intent= new Intent(MarkerDetailsActivity.this, ConversionActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(USER_EXTRA, wrapped);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });

        FloatingActionButton fab_call = (FloatingActionButton) findViewById(R.id.fab_call_us);
        fab_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (place != null) {
                    if (place.getPhone() != null)
                        lanuchPhoneIntent(place.getPhone());
                } else {
                    Toast.makeText(MarkerDetailsActivity.this, "no phone provide for that business", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void lanuchPhoneIntent(String phone) {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    MY_CALL_PERMISSION);
        }
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 111: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
            }
        }
    }


    private void getPlaceOwner(String owner_id) {
        FirebaseUtil.getUsersRef().child(owner_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    user = dataSnapshot.getValue(User.class);
                }

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });
    }

    private void updateView() {
        if(place.getImage()!=null && !place.getImage().isEmpty())
            PicassoUtil.loadFullImage(place.getImage(),image);

        if(place.getPhone()!=null && !place.getPhone().isEmpty())
            phone.setText(place.getPhone());
        else
            phone.setText(getString(R.string.error_phone));

        title.setText(place.getTitle());
        setTitle(place.getTitle());

        category.setText(place.getCategory());
        sub_category.setText(place.getSpecialization());
        district.setText(place.getDistrict());
        city.setText(place.getCity());
		//email not provided 
		//if place.get mail == nul set "" as text
        if (place.getEmail() == null || place.getEmail().equals("")){
            email.setText(getString(R.string.error_mail));
        }else email.setText(place.getEmail());

        about.setText(place.getAbout());
    }

    private void customDialog() {
        mProgressDialog = new Dialog(MarkerDetailsActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.custom_progress_dialog);
        mProgressDialog.show();
    }

    private void launchNavigationIntent(String navMethod){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr="+place.getLatitude()+","+place.getLongitude()+"&mode="+navMethod));
        startActivity(intent);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.driving:
                launchNavigationIntent(DRIVING_TAG);
                break;

            case R.id.walking:
                launchNavigationIntent(WALKING_TAG);
                break;

            case R.id.bicycling:
                launchNavigationIntent(BICYCLING_TAG);
                break;
        }
    }

    private void addLeftDrawables(){
        //phone drawable
        Drawable drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_phone)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        phone.setCompoundDrawables(drawable, null, null, null );

        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_email)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        email.setCompoundDrawables(drawable, null, null, null );

        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_assignment_ind)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        title.setCompoundDrawables(drawable, null, null, null );

        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_announcement)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        label_about.setCompoundDrawables(drawable, null, null, null );
    }


}
