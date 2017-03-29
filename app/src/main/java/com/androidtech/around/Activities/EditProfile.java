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
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.androidtech.around.Fragments.UploadPhotoToFirebaseFragment;
import com.androidtech.around.Models.City;
import com.androidtech.around.Models.Districs;
import com.androidtech.around.Models.User;
import com.androidtech.around.R;
import com.androidtech.around.Utils.DocumentHelper;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.PicassoUtil;
import com.androidtech.around.Utils.SharedPrefUtilities;
import com.androidtech.around.Utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gun0912.tedbottompicker.TedBottomPicker;

public class EditProfile extends AppCompatActivity implements
        UploadPhotoToFirebaseFragment.TaskCallbacks{


    //region views section
    @BindColor(R.color.publish_text)
    int mPublishTextColor;
    @BindColor(R.color.secondary_text)
    int mSecondaryTextColor;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.user_image)
    ImageView mUserImage;
    @BindView(R.id.change_photo)
    TextView mChangePhotoText;
    @BindView(R.id.full_name)
    EditText mFullNameEdit;
    @BindView(R.id.email_verify)
    TextView EmailVerifyText;
    @BindView(R.id.user_email)
    EditText mUserEmailEdit;
    @BindView(R.id.new_password)
    EditText mUserNewPasswordEdit;
    @BindView(R.id.new_password_line)
    View mNewPasswordViewLine;
    @BindView(R.id.confirm_password)
    EditText mUserConfirmPasswordEdit;
    @BindView(R.id.confirm_password_line)
    View mConfirmPasswordViewLine;
    @BindView(R.id.user_phone)
    EditText mUserPhoneEdit;
    @BindView(R.id.district_spin)
    AppCompatSpinner mUSerDistrictSpinner;
    @BindView(R.id.city_spin)
    AppCompatSpinner mUSerCitySpinner;
    @BindView(R.id.edit_profile_grid)
    GridLayout mEditProfileGrid;

    //endregion

    //region variables section
    private static final String TAG = "EditProfile";
    private FirebaseUser mCurrentUser;
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
    String userChoosenTask=null;
    private static final int TC_PICK_IMAGE = 101;
    private static final int REQUEST_CAMERA = 800;
    private static final int LANGUAGE_ENGLISH =0;
    private static final int LANGUAGE_FRANCE =1;
    private int mChoosingLanguage = LANGUAGE_ENGLISH;

    private ArrayAdapter<String> mDistrictsSpinAdapter;
    private ArrayAdapter<String> mCitySpinAdapter;

    private SharedPrefUtilities mSharedPrefUtilities;
    private List<Districs> mDistrictsList ;
    private FirebaseAuth mAuth;
    private boolean mSpinnerListener = true;
    private FirebaseAuth.AuthStateListener mAuthListener;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);

        mDistrictsList = new ArrayList<>();

        //get current user
        mCurrentUser = FirebaseUtil.getCurrentUser();
        if (mCurrentUser!=null)
            mCurrentUser.reload();

        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);

        //intiate firebase auth
        firebaseAuthIntalize();

        //intaite Custom dialog
        customDialog();

        //change toolbar style
        customizeToolbar();

        //intiate spinner views
        setupSpinner();

        //add drawable to edit texts
        addLeftDrawable();

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
     * intiate firebase auth nd add listner for auth changes
     */
    private void firebaseAuthIntalize() {
        mAuth = FirebaseAuth.getInstance();

        //linten to auth changes
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.isEmailVerified());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void customDialog() {
        mProgressDialog = new Dialog(EditProfile.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.custom_progress_dialog);
    }

    private void setupSpinner() {
        //intiate gender spinner
        String[] emptyArray = new String[0];
        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(emptyArray));


        //intiate district spinner
        mDistrictsSpinAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,lst);
        mDistrictsSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUSerDistrictSpinner.setAdapter(mDistrictsSpinAdapter);

        mUSerDistrictSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(mSpinnerListener){
                    String district = mUSerDistrictSpinner.getSelectedItem().toString();
                    if(!district.isEmpty()){
                        //intiate city spinner
                        mCitySpinAdapter  = new ArrayAdapter<String>(EditProfile.this,android.R.layout.simple_spinner_item,getCityItems(district));
                        mCitySpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mUSerCitySpinner.setAdapter(mCitySpinAdapter);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mUSerDistrictSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mSpinnerListener = true; //enable item lisnter for spinners
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

                        //show menu item progress bar
                        FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot!=null){
                                    User user = dataSnapshot.getValue(User.class); // get the user
                                    if(user !=null){
                                        bindData(user);
                                    }
                                    mEditProfileGrid.setVisibility(View.VISIBLE);
                                    //change menu state to done
                                    mMenuState = STATE_IDLE;
                                    mSaveItem.setActionView(null);
                                    mSaveItem.setIcon(doneDrawable);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                //don't show view and reload data

                                //change menu state to reload
                                mMenuState = STATE_RELOAD;
                                mSaveItem.setActionView(null);
                                mSaveItem.setIcon(updateDrawable);

                                mEditProfileGrid.setVisibility(View.INVISIBLE);
                            }
                        });
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

    /**
     * publish data on ui
     * @param user
     */
    private void bindData(User user) {
        if(user.getFull_name()!=null){
            mFullNameEdit.setText(user.getFull_name());
            mSharedPrefUtilities.setFullName(user.getFull_name());
        }

        if(user.getProfile_picture_url()!=null){
            photoUploaded = true;
            PicassoUtil.loadProfileIcon(user.getProfile_picture_url(),mUserImage);
            mSharedPrefUtilities.setUserImageFull(user.getProfile_picture_url());
        }

        if(user.getEmail()!=null){
            mUserEmailEdit.setText(user.getEmail());
            mUserEmailEdit.setEnabled(false);
//            if(!mCurrentUser.isEmailVerified())
//                EmailVerifyText.setVisibility(View.VISIBLE);
        }

        if(user.getPhone_number()!=null)
            mUserPhoneEdit.setText(user.getPhone_number());


        if(user.getDistrict()!=null){
            mSpinnerListener = false;
            mUSerDistrictSpinner.setSelection(StringIndex(user.getDistrict(),getDistrictsItems().toArray(new String[0])));
            //intiate city spinner
            mCitySpinAdapter  = new ArrayAdapter<String>(EditProfile.this,android.R.layout.simple_spinner_item,getCityItems(user.getDistrict()));
            mCitySpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mUSerCitySpinner.setAdapter(mCitySpinAdapter);
        }

        if(user.getCity()!=null){
            mUSerCitySpinner.setSelection(StringIndex(user.getCity(),getCityItems(user.getDistrict()).toArray(new String[0])));
        }

        if(!mCurrentUser.isAnonymous()){

            mUserNewPasswordEdit.setVisibility(View.GONE);
            mNewPasswordViewLine.setVisibility(View.GONE);

            mConfirmPasswordViewLine.setVisibility(View.GONE);
            mUserConfirmPasswordEdit.setVisibility(View.GONE);
        }
    }

    /**
     * retrive index of string in array
     * @return
     */
    int StringIndex(String str , String []StringArray){
        int index = -1;
        for (int i=0;i<StringArray.length;i++) {
            if (StringArray[i].equals(str)) {
                index = i;
                break;
            }
        }
        return index;
    }

//    @OnClick(R.id.email_verify)
//    void emailVerify(){
//        if(!mCurrentUser.isEmailVerified()){
//            mProgressDialog.show();
//            mCurrentUser.sendEmailVerification()
//                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Void> task) {
//                            if(mProgressDialog.isShowing())
//                                mProgressDialog.dismiss();
//                            if (task.isSuccessful()) {
//                                showToast(R.string.check_your_email);
//                            }else{
//                                showToast(R.string.failed_send_email);
//                            }
//                        }
//                    });
//        }
//    }

    /**
     * add left drawables to edit texts
     */
    private void addLeftDrawable() {
        //full name drawable
        Drawable drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_assignment_ind)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mFullNameEdit.setCompoundDrawables(drawable, null, null, null );

        //email drawable
        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_email)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mUserEmailEdit.setCompoundDrawables(drawable, null, null, null );

        //password drawable
        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_lock)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mUserNewPasswordEdit.setCompoundDrawables(drawable, null, null, null );


        //confirm password drawable
        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_low_priority)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mUserConfirmPasswordEdit.setCompoundDrawables(drawable, null, null, null );

        //phone drawable
        drawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_phone)
                .color(getResources().getColor(R.color.editProfile_icon))
                .sizeDp(20);
        mUserPhoneEdit.setCompoundDrawables(drawable, null, null, null );

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
    }

    /**
     * change toolbar style
     */
    private void customizeToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable clear = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_clear)
                .color(getResources().getColor(R.color.primary_text))
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
                         saveChanges();
                        break;
                    case STATE_RELOAD:
                        updateUI();
                        break;
                    case STATE_PROGRESS:
                        break;
                };
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

                 TedBottomPicker bottomSheetDialogFragment = new TedBottomPicker.Builder(EditProfile.this)
                         .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                             @Override
                             public void onImageSelected(Uri uri) {
                                 Log.d("ted","uri: "+uri);
                                 Log.d("ted","uri.getPath(): "+uri.getPath());

                                 String filePath = DocumentHelper.getPath(EditProfile.this, uri);
                                 //Safety check to prevent null pointer exception
                                 if (filePath == null || filePath.isEmpty()) {
                                     showToast(R.string.error_occurred);
                                     return;
                                 }
                                 mProgressDialog.show();
                                 uploadImageFirebase(filePath);
                             }
                         })
                         .setPeekHeight(getResources().getDisplayMetrics().heightPixels/2)
                         .create();

                 bottomSheetDialogFragment.show(getSupportFragmentManager());

             }

             @Override
             public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                 Toast.makeText(EditProfile.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
             }


         };

         new TedPermission(EditProfile.this)
                 .setPermissionListener(permissionlistener)
                 .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                 .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                 .check();
    }

    @Override
    public void onPhotoProfileUploaded(final String imageUrl) {
        if(imageUrl!=null){
            Map<String, Object> updateValues = new HashMap<>();
            updateValues.put("profile_picture_url", imageUrl);
            updateValues.put("updated_at", System.currentTimeMillis());

            FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).updateChildren(
                    updateValues,
                    new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                            if(mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            if (firebaseError != null) {
                                showToast(R.string.error_image);
                            }else{
                                //change boolean to truw
                                photoUploaded = true;
                                //save image to current user
                                mSharedPrefUtilities.setUserImageFull(imageUrl);
                                showToast(R.string.image_changed);
                                PicassoUtil.loadProfileIcon(imageUrl,mUserImage);
                                Log.d(TAG, "User profile updated.");

                            }
                        }
                    });
        }else {
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();

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
     * when save button pressed
     */
    private void saveChanges() {
        if(mCurrentUser.isAnonymous()){
            //change account to permanently
            saveNewUser();
        }else{
            //update user profile
            updateProfile();
        }
    }

    private void updateProfile() {
        //show dialog
        mProgressDialog.show();
        //get data from views
        final String fullName = mFullNameEdit.getText().toString();
        final String userPhone = mUserPhoneEdit.getText().toString();
        final String userDistrict = mUSerDistrictSpinner.getSelectedItem().toString();
        final String userCity = mUSerCitySpinner.getSelectedItem().toString();

        if (fullName.isEmpty() || userDistrict.isEmpty() || userCity.isEmpty()) {
            mFullNameEdit.setError(getString(R.string.required_field));
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            showToast(R.string.required_field);
            return;
        }
        if (!userPhone.isEmpty() && userPhone.length()<9){
            mUserPhoneEdit.setError(getString(R.string.phone_short));
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            showToast(R.string.phone_short);
            return;
        }
            Map<String, Object> updateValues = new HashMap<>();
            updateValues.put("full_name", fullName);
            updateValues.put("phone_number", userPhone);
            updateValues.put("city", userCity);
            updateValues.put("district", userDistrict);
            updateValues.put("updated_at", System.currentTimeMillis());

            FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).updateChildren(
                    updateValues,
                    new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                            if(mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            if (firebaseError != null) {
                                showToast(R.string.error_edit_profile);
                            }else{
                                showToast(R.string.done_editing);
                                //save data to shared
                                mSharedPrefUtilities.setFullName(fullName);
                                IntentToMainActivity();
                            }
                        }
                    });
    }

    /**
     * add new user to DB
     */
    private void saveNewUser() {
        //show dialog
        mProgressDialog.show();
        //get data from views
        final String fullName = mFullNameEdit.getText().toString();
        final String userEmail = mUserEmailEdit.getText().toString();
        final String newPass = mUserNewPasswordEdit.getText().toString();
        final String confirmPass = mUserConfirmPasswordEdit.getText().toString();
        final String userPhone = mUserPhoneEdit.getText().toString();
        final String userDistrict = mUSerDistrictSpinner.getSelectedItem().toString();
        final String userCity = mUSerCitySpinner.getSelectedItem().toString();

        if (fullName.isEmpty() || userDistrict.isEmpty() || userCity.isEmpty()) {
            showToast(R.string.required_field);
            mFullNameEdit.setError(getResources().getString(R.string.required_field));
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            return;
        }
        if (userEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(userEmail).matches()) {
            mUserEmailEdit.setError(getResources().getString(R.string.wrong_email));
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            return;
        }
        if (!userPhone.isEmpty() && userPhone.length()<9){
            mUserPhoneEdit.setError(getResources().getString(R.string.phone_short));
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            showToast(R.string.phone_short);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            mUserConfirmPasswordEdit.setError(getResources().getString(R.string.wrong_password));
            if(mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            return;
        }

        //change current user from anymouns to email user
        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, confirmPass);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "linkWithCredential:onComplete:" + task.isSuccessful());
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            if(mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            showToast(R.string.error_occurred_password);
                        } else{
                            Map<String, Object> updateValues = new HashMap<>();
                            updateValues.put("full_name", fullName);
                            updateValues.put("email", userEmail);
                            updateValues.put("phone_number", userPhone);
                            updateValues.put("city", userCity);
                            updateValues.put("district", userDistrict);
                            updateValues.put("updated_at", System.currentTimeMillis());
                            updateValues.put("created_at", System.currentTimeMillis());
                            updateValues.put("user_id",FirebaseUtil.getCurrentUserId());

                            FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).updateChildren(
                                    updateValues,
                                    new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                                            if(mProgressDialog.isShowing())
                                                mProgressDialog.dismiss();
                                            if (firebaseError != null) {
                                                showToast(R.string.error_edit_profile);
                                            }else{
                                                showToast(R.string.profile_created);
                                                //send email verify
                                                mAuth.getCurrentUser().sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "Email sent.");
                                                                }
                                                            }
                                                        });
                                                //save data to shared
                                                mSharedPrefUtilities.setFullName(fullName);
                                                IntentToMainActivity();
                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    @MainThread
    private void showToast(@StringRes int errorMessageRes) {
        Toast.makeText(this, errorMessageRes, Toast.LENGTH_LONG).show();
    }

    private void IntentToMainActivity(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
