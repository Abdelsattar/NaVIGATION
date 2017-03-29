package com.androidtech.around.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidtech.around.Adapters.SpinnerAdapter;
import com.androidtech.around.Fragments.UploadPhotoToFirebaseFragment;
import com.androidtech.around.Models.BusinessPlace;
import com.androidtech.around.Models.Category;
import com.androidtech.around.Models.City;
import com.androidtech.around.Models.Districs;
import com.androidtech.around.Models.Specialization;
import com.androidtech.around.R;
import com.androidtech.around.Utils.DocumentHelper;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gun0912.tedbottompicker.TedBottomPicker;

public class AddBusiness extends AppCompatActivity implements
        UploadPhotoToFirebaseFragment.TaskCallbacks{

    //views section
    @BindColor(R.color.publish_text)
    int mPublishTextColor;
    @BindColor(R.color.secondary_text)
    int mSecondaryTextColor;
    @BindString(R.string.extra_business_id)
    String extra_business_id;
    @BindString(R.string.add_location)
    String mAddLocationStr;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.business_image)
    ImageView mBusinessImage;
    @BindView(R.id.change_photo)
    TextView mChangePhotoText;
    @BindView(R.id.business_title)
    EditText mBusinessTitle;
    @BindView(R.id.business_about)
    EditText mBusinessAbout;
    @BindView(R.id.business_phone)
    EditText mBusinessPhone;
    @BindView(R.id.business_mail)
    EditText mBusinessEmail;
    @BindView(R.id.district_spin)
    AppCompatSpinner mBusinessDistrictSpinner;
    @BindView(R.id.city_spin)
    AppCompatSpinner mBusinessCitySpinner;
    @BindView(R.id.category_spin)
    AppCompatSpinner mBusinessCategorySpinner;
    @BindView(R.id.subcategory_spin)
    AppCompatSpinner mBusinessSpecializationSpinner;
    @BindView(R.id.edit_profile_grid)
    GridLayout mEditProfileGrid;
    @BindView(R.id.clear_location)
    ImageView mClearLocation;
    @BindView(R.id.add_location)
    TextView mAddLocation;
    @BindView(R.id.add_location_linear)
    LinearLayout mAddLocationLinear;

    //variables section
    private static final String TAG = "New Business";
    String userChoosenTask=null;
    private static final int TC_PICK_IMAGE = 101;
    private static final int REQUEST_CAMERA = 800;
    private Dialog mProgressDialog;

    private UploadPhotoToFirebaseFragment mTaskFragment;
    public static final String TAG_TASK_FRAGMENT = "UploadPhotoToFirebaseFragment";

    private static final int STATE_IDLE = 0;
    private static final int STATE_PROGRESS = 1;
    private static final int STATE_RELOAD= 2;
    private int mMenuState = STATE_IDLE;
    private MenuItem mSaveItem;
    private Drawable doneDrawable,updateDrawable;

    private boolean photoUploaded = false;
    private String businessPicture = "";

    private static final int LANGUAGE_ENGLISH =0;
    private static final int LANGUAGE_FRANCE =1;
    private int mChoosingLanguage = LANGUAGE_ENGLISH;

    private ArrayAdapter<String> mDistrictsSpinAdapter;
    private ArrayAdapter<String> mCitySpinAdapter;

    private ArrayAdapter<String> mCategorySpinAdapter;
    private ArrayAdapter<String> mSpecialitySpinAdapter;

    private List<Districs> mDistrictsList ;
    private ArrayList<Category> mCategoryList;
    private boolean mSpinnerListener = true;
    private boolean mCategorySpinnerListener = true;

    private String mCurrentLocationStr ="";
    LatLng mCurrentLatLng;
    private Place mCurrentPlace;
    protected final static int PLACE_PICKER_REQUEST = 9090;
    private SpinnerAdapter mSpinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_business);
        ButterKnife.bind(this);

        mDistrictsList = new ArrayList<>();
        mCategoryList = new ArrayList<>();

        //intaite Custom dialog
        customDialog();

        //change toolbar style
        customizeToolbar();

        //intiate spinner views
        setupDistrictSpinner();

        setupCategorySpinner();

        //add drawable to edit texts
        addLeftDrawable();

        //intiate add location views
        setupAddLocation();

        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        mTaskFragment = (UploadPhotoToFirebaseFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

        // create the fragment and data the first time
        if (mTaskFragment == null) {
            // add the fragment
            mTaskFragment = new UploadPhotoToFirebaseFragment();
            fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
        }
    }

    /**
     * show autocomplete address
     */
    @OnClick(R.id.add_location)
    void addLocation(){

        // Launch the Place Picker so that the user can specify their location, and then
        // return the result to SettingsActivity.
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            this.startActivityForResult(
                    builder.build(this), PLACE_PICKER_REQUEST);

        } catch (GooglePlayServicesNotAvailableException
                | GooglePlayServicesRepairableException e) {
            // What did you do?? This is why we check Google Play services in onResume!!!
            // The difference in these exception types is the difference between pausing
            // for a moment to prompt the user to update/install/enable Play services vs
            // complete and utter failure.
            // If you prefer to manage Google Play services dynamically, then you can do so
            // by responding to these exceptions in the right moment. But I prefer a cleaner
            // user experience, which is why you check all of this when the app resumes,
            // and then disable/enable features based on that availability.
        }
    }

    private void setupAddLocation() {
        // Check to see if Google Play services is available. The Place Picker API is available
        // through Google Play services, so if this is false, we'll just carry on as though this
        // feature does not exist. If it is true, however, we can add a widget to our add location
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            // remove add location linear
            mAddLocationLinear.setVisibility(View.INVISIBLE);
        }

    }

    /**
     * clear choosen location
     */
    @OnClick(R.id.clear_location)
    void clearLocation(){
        //remove current location
        mCurrentPlace = null;
        //reset text view
        mAddLocation.setText(mAddLocationStr);
        mAddLocation.setTextColor(mPublishTextColor);
        //invisible icon
        mClearLocation.setVisibility(View.INVISIBLE);
        mCurrentLocationStr = "";
        mCurrentLatLng = null;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void customDialog() {
        mProgressDialog = new Dialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.custom_progress_dialog);
    }

    private void setupDistrictSpinner() {
        //intiate gender spinner
        String[] emptyArray = new String[0];
        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(emptyArray));


        //intiate district spinner
        mDistrictsSpinAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,lst);
        mDistrictsSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBusinessDistrictSpinner.setAdapter(mDistrictsSpinAdapter);

        mBusinessDistrictSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mSpinnerListener){
                    String district = mBusinessDistrictSpinner.getSelectedItem().toString();
                    if(!district.isEmpty()){
                        //intiate city spinner
                        mCitySpinAdapter  = new ArrayAdapter<String>(AddBusiness.this,android.R.layout.simple_spinner_item,getCityItems(district));
                        mCitySpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mBusinessCitySpinner.setAdapter(mCitySpinAdapter);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mBusinessDistrictSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mSpinnerListener = true; //enable item lisnter for spinners
                return false;
            }
        });
    }

    private void setupCategorySpinner() {
        //intiate gender spinner
        String[] emptyArray = new String[0];
        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(emptyArray));

        //intiate district spinner
//        mCategorySpinAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,lst);
//        mCategorySpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        mBusinessCategorySpinner.setAdapter(mCategorySpinAdapter);

        ArrayList<Category>categories = new ArrayList<>();
        mSpinnerAdapter = new SpinnerAdapter(this,R.layout.item_spinner,categories);
        mBusinessCategorySpinner.setAdapter(mSpinnerAdapter);

        mBusinessCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mCategorySpinnerListener){
                    Category category = (Category) mBusinessCategorySpinner.getAdapter().getItem(i);
                    if(!category.getFr_name().isEmpty()){
                        //intiate city spinner
                        mSpecialitySpinAdapter  = new ArrayAdapter<String>(AddBusiness.this,android.R.layout.simple_spinner_item,getSpecializationItems(category.getFr_name()));
                        mSpecialitySpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mBusinessSpecializationSpinner.setAdapter(mSpecialitySpinAdapter);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mBusinessCategorySpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mCategorySpinnerListener = true; //enable item lisnter for spinners
                return false;
            }
        });
    }


    /**
     * update ui data from firebase
     */
    private void updateUI() {
        //change menu state to progress
        mMenuState = STATE_PROGRESS;
        mSaveItem.setActionView(R.layout.action_bar_progress);
        mSaveItem.expandActionView();

        //check internet first
        if(Utils.checkInternet(this)){

            //load data from firebase of category's
            FirebaseUtil.getCategoryRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Category category = postSnapshot.getValue(Category.class); // get the category
                            category.setFr_name(category.getFr_name());
                            mCategoryList.add(category);
                        }
                        //add data to spinners
                        mSpinnerAdapter.clear();
                        mSpinnerAdapter.addAll(mCategoryList);
                        mSpinnerAdapter.notifyDataSetChanged();

                        if (!mDistrictsList.isEmpty()){
                            mEditProfileGrid.setVisibility(View.VISIBLE);
                            //change menu state to done
                            mMenuState = STATE_IDLE;
                            mSaveItem.setActionView(null);
                            mSaveItem.setIcon(doneDrawable);
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    //don't show view and reload data

                    //change menu state to reload
                    mMenuState = STATE_RELOAD;
                    mSaveItem.setActionView(null);
                    mSaveItem.setIcon(updateDrawable);

                    mEditProfileGrid.setVisibility(View.INVISIBLE);
                }
            });


            //load data from firebase of districts
            FirebaseUtil.getDistrictsRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            Districs district = postSnapshot.getValue(Districs.class); // get the district
                            mDistrictsList.add(district);
                        }

                        //add data to spinners
                        mDistrictsSpinAdapter.clear();
                        mDistrictsSpinAdapter.addAll(getDistrictsItems());
                        mDistrictsSpinAdapter.notifyDataSetChanged();


                        if (!mCategoryList.isEmpty()){
                            mEditProfileGrid.setVisibility(View.VISIBLE);
                            //change menu state to done
                            mMenuState = STATE_IDLE;
                            mSaveItem.setActionView(null);
                            mSaveItem.setIcon(doneDrawable);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    //don't show view and reload data

                    //change menu state to reload
                    mMenuState = STATE_RELOAD;
                    mSaveItem.setActionView(null);
                    mSaveItem.setIcon(updateDrawable);

                    mEditProfileGrid.setVisibility(View.INVISIBLE);
                }
            });
        }else{
            //change menu state to reload
            mMenuState = STATE_RELOAD;
            mSaveItem.setActionView(null);
            mSaveItem.setIcon(updateDrawable);

            mEditProfileGrid.setVisibility(View.INVISIBLE);
        }

    }

    private ArrayList<String> getDistrictsItems() {
        List<String>districts = new ArrayList<>();
        switch (mChoosingLanguage){
            case LANGUAGE_ENGLISH:
                for (Districs districs :mDistrictsList){
                    districts.add(districs.en_name);
                }
                break;
        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(districts.toArray(new String[0])));
        return lst;
    }

    private ArrayList<String> getCityItems(String districtStr) {
        List<String>cities = new ArrayList<>();
        List<City>cityList = getCitiesOfDistrict(districtStr);
        switch (mChoosingLanguage){
            case LANGUAGE_ENGLISH:
                for (City city :cityList){
                    cities.add(city.en_name);
                }
                break;
        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(cities.toArray(new String[0])));
        return lst;
    }

    List<City> getCitiesOfDistrict(String districtStr){
        for (Districs districs :mDistrictsList){
            if(districs.en_name.equals(districtStr))
                return districs.cities;
        }
        return new ArrayList<>();
    }

    private ArrayList<String> getCategoryItems() {
        List<String>categorys = new ArrayList<>();
        switch (mChoosingLanguage){
            case LANGUAGE_ENGLISH:
                for (Category category :mCategoryList){
                    categorys.add(category.getFr_name());
                }
                break;

        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(categorys.toArray(new String[0])));
        return lst;
    }

    private ArrayList<String> getSpecializationItems(String categoryStr) {
        List<String>sub_category = new ArrayList<>();
        List<Specialization>specializationList = getSubOfCategory(categoryStr);
        switch (mChoosingLanguage){
            case LANGUAGE_ENGLISH:
                try {
                    for (Specialization specialization :specializationList){
                        specialization.setfr_name(specialization.getfr_name());
                        sub_category.add(specialization.getfr_name());
                        Log.d("Spec",specialization.getfr_name());
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
            case LANGUAGE_FRANCE:
                try {
                    for (Specialization specialization :specializationList){
                        specialization.setfr_name(specialization.getfr_name());
                        sub_category.add(specialization.getfr_name());
                        Log.d("Spec",specialization.getfr_name());
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
                break;
        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(sub_category.toArray(new String[0])));
        return lst;
    }

    List<Specialization> getSubOfCategory(String categoryStr){
        for (Category category :mCategoryList){
            if(category.getFr_name().equals(categoryStr))
                return category.getmSpecializations();
        }
        return new ArrayList<>();
    }


    /**
     * add left drawables to edit texts
     */
    private void addLeftDrawable() {
        //full name drawable
        Drawable drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_assignment_ind)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mBusinessTitle.setCompoundDrawables(drawable, null, null, null );


        //phone drawable
        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_phone)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mBusinessPhone.setCompoundDrawables(drawable, null, null, null );

        //email drawable
        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_email)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mBusinessEmail.setCompoundDrawables(drawable, null, null, null );

        //change menu item save changes icon
        doneDrawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_done)
                .color(getResources().getColor(R.color.editProfileAccent))
                .sizeDp(20);

        //change menu item reload icon
        updateDrawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_update)
                .color(getResources().getColor(R.color.editProfileAccent))
                .sizeDp(20);

        //add location drawable
        updateDrawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_location_on)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mAddLocation.setCompoundDrawables(updateDrawable, null, null, null );

        //add clear location
        updateDrawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_clear)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(14);
        mClearLocation.setBackground(updateDrawable);
    }

    /**
     * change toolbar style
     */
    private void customizeToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable clear = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_clear)
                .color(getResources().getColor(R.color.white))
                .sizeDp(16);
        getSupportActionBar().setHomeAsUpIndicator(clear);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_profile_menu, menu);
        mSaveItem = menu.getItem(0);

        mSaveItem.setIcon(doneDrawable);

        updateUI();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_save_changes:
                switch (mMenuState){
                    case STATE_IDLE:
                        saveNewBusiness();
                        break;
                    case STATE_PROGRESS:
                        break;
                }
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.change_photo)
    void showImagePicker() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                TedBottomPicker bottomSheetDialogFragment = new TedBottomPicker.Builder(AddBusiness.this)
                        .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                            @Override
                            public void onImageSelected(Uri uri) {
                                Log.d("ted", "uri: " + uri);
                                Log.d("ted", "uri.getPath(): " + uri.getPath());

                                String filePath = DocumentHelper.getPath(AddBusiness.this, uri);
                                //Safety check to prevent null pointer exception
                                if (filePath == null || filePath.isEmpty()) {
                                    showToast(R.string.error_occurred);
                                    return;
                                }
                                mProgressDialog.show();
                                uploadImageFirebase(filePath);
                                Picasso.with(AddBusiness.this).load(uri).into(mBusinessImage);
                            }
                        })
                        .setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                        .create();

                bottomSheetDialogFragment.show(getSupportFragmentManager());

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(AddBusiness.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };

        new TedPermission(AddBusiness.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                mCurrentPlace = PlacePicker.getPlace(data, this);
                Log.i(TAG, "Place: " + mCurrentPlace.getName());
                //update add location address
                updateLocationUI();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    /**
     * update place address on location text
     */
    private void updateLocationUI() {
        mCurrentLocationStr = mCurrentPlace.getAddress().toString();
        mCurrentLatLng = mCurrentPlace.getLatLng();
        if(mCurrentLocationStr.isEmpty())
            mCurrentLocationStr = String.format("(%.2f, %.2f)",mCurrentLatLng.latitude, mCurrentLatLng.longitude);

        mAddLocation.setText(mCurrentLocationStr);
        mAddLocation.setTextColor(mSecondaryTextColor);
        mClearLocation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPhotoProfileUploaded(final String imageUrl) {
        if(mProgressDialog.isShowing())
            mProgressDialog.dismiss();
        if(imageUrl!=null){
        //  PicassoUtil.loadProfileIcon(imageUrl, mBusinessImage);
            photoUploaded = true;
           businessPicture = imageUrl;
        } else{
            mBusinessImage.setImageResource(R.drawable.profile_image);
            showToast(R.string.error_image);
        }
    }

    /**
     * compress image and upload it to firebase
     * @param filePath
     */
    private void uploadImageFirebase(String filePath) {
        //compress image here before upload
        //TODO make compression inside task fragment
        String compressFilePath = compressImage(filePath);
        if (compressFilePath == null || compressFilePath.isEmpty()){
            showToast(R.string.error_occurred);
            return;
        }
        File chosenFile = new File(compressFilePath);

        Uri file = Uri.fromFile(chosenFile);
        mTaskFragment.uploadPhotoProfile(file);
    }

    public String compressImage(String imageUri) {

        String filePath = imageUri;
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    /**
     * add new user to DB
     */
    private void saveNewBusiness() {
        if(Utils.checkInternet(this)){
            //show dialog
            mProgressDialog.show();
            //get data from views
            final String businessTitle = mBusinessTitle.getText().toString();

            final String businessPhone = mBusinessPhone.getText().toString();
            final String businessEmail = mBusinessEmail.getText().toString();
            final String businessDistrict = mBusinessDistrictSpinner.getSelectedItem().toString();
            final String businessCity = mBusinessCitySpinner.getSelectedItem().toString();
            final Category businessCategory = (Category) mBusinessCategorySpinner.getSelectedItem();
            final String businessSpecialization = mBusinessSpecializationSpinner.getSelectedItem().toString();
            String businessAbout = mBusinessAbout.getText().toString();

            if (businessTitle.isEmpty()) {
                mBusinessTitle.setError(getResources().getString(R.string.required_field));
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                return;
            }
            else if( mCurrentLocationStr.equals("")){
                showToast(R.string.required_location);
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                return;
            }
            else if(businessDistrict.equals("Choose District")){
                showToast(R.string.required_city);
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                return;
            }
            else if (businessPhone.isEmpty()) {
                mBusinessPhone.setError(getResources().getString(R.string.required_field));
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                return;
            }
            else if (!businessPhone.isEmpty() && businessPhone.length() < 9) {
                mBusinessPhone.setError(getResources().getString(R.string.phone_short));
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                showToast(R.string.phone_short);
                return;
            }else if (!businessEmail.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(businessEmail).matches()) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                showToast(R.string.wrong_email);
                return;
            }

            if(businessAbout.isEmpty()){
                businessAbout = getString(R.string.business_about_empty).toString();
            }

            final BusinessPlace mBusinessPlace = new BusinessPlace();
            mBusinessPlace.setOwner_id(FirebaseUtil.getCurrentUserId());
            mBusinessPlace.setTitle(businessTitle);
            mBusinessPlace.setImage(businessPicture);
            mBusinessPlace.setPhone(businessPhone);
            mBusinessPlace.setEmail(businessEmail);
            mBusinessPlace.setCategory(businessCategory.getFr_name());
            mBusinessPlace.setSpecialization(businessSpecialization);
            mBusinessPlace.setCity(businessCity);
            mBusinessPlace.setDistrict(businessDistrict);
            mBusinessPlace.setAbout(businessAbout);
            mBusinessPlace.setLocation(mCurrentLocationStr);
            mBusinessPlace.setLatitude(mCurrentLatLng.latitude);
            mBusinessPlace.setLongitude(mCurrentLatLng.longitude);
            mBusinessPlace.setCreated_at(String.valueOf(System.currentTimeMillis()));
            mBusinessPlace.setUpdated_at(String.valueOf(System.currentTimeMillis()));
            mBusinessPlace.setIcon(businessCategory.getIcon());

            //add business place to firebase
            final String businessPlaceKey = FirebaseUtil.getBusinessRef().push().getKey();

            FirebaseUtil.getBusinessRef().child(businessPlaceKey).setValue(mBusinessPlace, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError==null){
                        FirebaseUtil.getUserBusinessRef().push().setValue(businessPlaceKey, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError==null){
                                    showToast(R.string.success_add_place);
                                    if (mProgressDialog.isShowing())
                                        mProgressDialog.dismiss();;
                                    IntentToMainActivity(mBusinessPlace);
                                }else {
                                    if (mProgressDialog.isShowing())
                                        mProgressDialog.dismiss();;
                                    showToast(R.string.error_add_place);
                                }

                            }
                        });
                    }else {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();;
                        showToast(R.string.error_add_place);
                    }

                }
            });
        }
        else
        {
            Utils.internetErrorDialog(this);
        }

    }

    @MainThread
    private void showToast(@StringRes int errorMessageRes) {
        Toast.makeText(this, errorMessageRes, Toast.LENGTH_LONG).show();
    }

    private void IntentToMainActivity(BusinessPlace mBusinessPlace){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Parcelable wrapped = Parcels.wrap(mBusinessPlace);
        Bundle bundle = new Bundle();
        bundle.putParcelable(extra_business_id, wrapped);
        intent.putExtras(bundle);
        startActivity(intent);
        finish(); // Call once you redirect to another activity
    }

    @Override
    public void onImageResized(String resizedImage, boolean isFull) {

    }

    @Override
    public void onPhotoUploaded(String fullUrl, String thumbnailUrl) {

    }

}
