package com.androidtech.around.Activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidtech.around.Adapters.SpinnerAdapter;
import com.androidtech.around.Models.BusinessPlace;
import com.androidtech.around.Models.Category;
import com.androidtech.around.Models.City;
import com.androidtech.around.Models.Districs;
import com.androidtech.around.Models.GooglePlaces.Details.Details;
import com.androidtech.around.Models.GooglePlaces.PlacesRespose;
import com.androidtech.around.Models.GooglePlaces.Result;
import com.androidtech.around.Models.PlacesCategories;
import com.androidtech.around.Models.Specialization;
import com.androidtech.around.R;
import com.androidtech.around.Service.ServiceBuilder;
import com.androidtech.around.Service.ServiceInterfaces;
import com.androidtech.around.Utils.Constants;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.PermissionUtil;
import com.androidtech.around.Utils.SharedPrefUtilities;
import com.androidtech.around.Utils.Utils;
import com.androidtech.around.Widgets.CircleTransformation;
import com.androidtech.around.Widgets.CutomDrawerItem.CustomUrlPrimaryDrawerItem;
import com.androidtech.around.Widgets.PicassoMarker;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.androidtech.around.R.id.fab;
import static com.androidtech.around.R.id.map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerDragListener, View.OnClickListener{

    //region views section
    @BindString(R.string.extra_business_id)
    String extra_business_id;
    @BindView(android.R.id.content)
    View mRootView;
    @BindView(fab)
    com.github.clans.fab.FloatingActionButton mAddFab;
    @BindView(R.id.my_location_fab)
    com.github.clans.fab.FloatingActionButton mMyLocationFab;
    @BindView(R.id.places_here_fab)
    com.github.clans.fab.FloatingActionButton mPlacesHereFab;
    @BindView(R.id.filter)
    com.github.clans.fab.FloatingActionButton filter;
    @BindView(R.id.inbox)
    com.github.clans.fab.FloatingActionButton inbox;
    @BindView(R.id.sign_in_layout)
    RelativeLayout mSignInLayout;
    @BindView(R.id.add_place_layout)
    RelativeLayout mAddPlaceLayout;
    @BindView(R.id.add_business_text)
    TextView mAddPlaceText;
    @BindView(R.id.floating_search_view)
    FloatingSearchView mSearchView;
    @BindView(R.id.places_fab)
    FloatingActionButton placesSwitch;
    //    @BindView(R.id.dummy)
//    View mDummyView;
    @BindView(R.id.menu_yellow)
    FloatingActionMenu menuYellow;
    //endregion

    //region decalre fields
    AppCompatSpinner mBusinessDistrictSpinner;
    AppCompatSpinner mBusinessCitySpinner;
    AppCompatSpinner mBusinessCategorySpinner;
    AppCompatSpinner mBusinessSpecializationSpinner;
    AppCompatButton btn_search;

    //vars section
    private static final String TAG = "MainActivity";
    MaterialDialog dialog;
    private GoogleMap mMap;
    private PicassoMarker target;
    private FirebaseAuth mAuth;
    private Dialog mProgressDialog;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private AlertDialog mGpsDialog;
    private Location mCurrentLocation;
    private LocationManager mLocationManager;
    private Drawer mLeftDrawer = null;
    private AccountHeader mHeaderResult = null;
    private SharedPrefUtilities mSharedPrefUtilities;
    private List<BusinessPlace> myBusiness;
    private List<BusinessPlace> mUserBusiness;
    private Map<String, Object> mBusinessPlaceList;
    private Map<String, Object> mGooglePlaceList;
    private BusinessPlace mNewAddedBusiness;
    int counter = 1;
    int userPlacesCounter = 200;

    private ArrayAdapter<String> mDistrictsSpinAdapter;
    private ArrayAdapter<String> mCitySpinAdapter;
    private ArrayAdapter<String> mCategorySpinAdapter;
    private ArrayAdapter<String> mSpecialitySpinAdapter;
    private List<Districs> mDistrictsList;
    private boolean mSpinnerListener = true;
    private boolean mCategorySpinnerListener = true;
    private boolean firstMarker = true;
    private boolean fromMenue;

    private ArrayList<Category> mCategoryList;
    private List<Result> resultArrayList;
    private List <com.androidtech.around.Models.GooglePlaces.Details.Result> detailesResultsList;
    private Map<String, Object> mGooglePlaceDetailsList;

    private List<PlacesCategories> placesCategoriesList;
    private SpinnerAdapter mSpinnerAdapter;
    String currentCategory = "";
    String currentIcon = "";
    String[] googlePlaceDetails;


    /**
     * Id to identify a location permission request.
     */
    private static final int REQUEST_LOCATION = 1;
    private static final int LANGUAGE_ENGLISH = 0;
    private static final int LANGUAGE_FRANCE = 1;
    private int mChoosingLanguage = LANGUAGE_ENGLISH;

    /**
     * Permissions required to get location. Used by the {@link }.
     */

    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    private InterstitialAd mInterstitialAd;
    private boolean mustShowAdd = false;
    Handler mHandler = new Handler();
    int adsTimer = 60000;
    boolean isGoogleCategory = false;
    Bundle savedInstanceState;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        this.savedInstanceState = savedInstanceState;

        mDistrictsList = new ArrayList<>();
        mCategoryList = new ArrayList<>();
        resultArrayList = new ArrayList<>();
        mBusinessPlaceList = new HashMap<>();
        mGooglePlaceDetailsList = new HashMap<>();
        mGooglePlaceList = new HashMap<>();
        detailesResultsList = new ArrayList<>();
        googlePlaceDetails = new String[7];
        mustShowAdd = true;

        checkGAPSForHighVersions();

        menuYellow.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                String text;
                if (opened) {
                    adjustFabs();
                }
            }
        });

        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);

        //get extra bundle if new place added
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            mNewAddedBusiness = Parcels.unwrap(bundle.getParcelable(extra_business_id));

        //make full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        myBusiness = new ArrayList<>();
        mUserBusiness = new ArrayList<>();

        //get current user
        mCurrentUser = FirebaseUtil.getCurrentUser();
        if (mCurrentUser != null)
            mCurrentUser.reload();

        //user choose which category to use
        Intent intent = this.getIntent();
        if (intent != null && intent.hasExtra("Splash")) {
            showChooseCategoryDilog(false);

        } else {
            //add drawer
            addDrawer();

            setupSearchView();
            mLeftDrawer.openDrawer();

        }

        initiateGooglePlacesList();

        //intiate firebase auth
        firebaseAuthIntalize();

        //intaite Custom dialog
        customDialog();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        //setup inbox fab
        inbox.setImageDrawable(getDrawable(R.drawable.ic_message_black_24dp));

        mSignInLayout.setVisibility(View.INVISIBLE);

        //check if current user is
        if (mCurrentUser != null) {
            if (!mCurrentUser.isAnonymous()) {//user is signed in but before add place need to verify his email
                mAddFab.setVisibility(View.VISIBLE);
                //      mDummyView.setVisibility(View.VISIBLE);
                mSignInLayout.setVisibility(View.GONE);
                mAddPlaceLayout.setVisibility(View.VISIBLE);
            }
        } else {//user is not signed in
            // mAddFab.setLabelText(getString(R.string.sign_up));
            //  mAddFab.setVisibility(View.INVISIBLE);
            //      mDummyView.setVisibility(View.INVISIBLE);
            mSignInLayout.setVisibility(View.VISIBLE);
            mAddPlaceLayout.setVisibility(View.GONE);
        }


//        if (mLeftDrawer != null) {
//            //open drawer
//            mLeftDrawer.openDrawer();
//        }
//        setupSearchView();

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {

            }

            @Override
            public void onSearchAction(String currentQuery) {
                if (!isGoogleCategory) {
                    searchMapByTitle(currentQuery);
                }else {
                    if (mCurrentLocation!=null) {
                        mMap.clear();
                        PerformGetTextSearchPlaces(currentQuery, String.valueOf(mCurrentLocation.getLatitude()), String.valueOf(mCurrentLocation.getLongitude()), true);
                    }
                }
            }
        });

        //checkGps
        checkGps();
//        checkGAPSForHighVersions();

        //change fab icon
        mMyLocationFab.setImageDrawable(getDrawable(R.drawable.ic_my_location_black_24dp));

        //get about from remote config
        getAboutFromRemoteConfig();

        //get fcm token
        getFcmToken();

        connectUserOnline();


        mAddPlaceText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddBusiness.class));
            }
        });
        initAdMob();

        placesSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChooseCategoryDilog(false);
            }
        });
    }

    public void adjustFabs(){
        if (mCurrentUser == null) {
            if (isGoogleCategory) {
                mAddFab.setLabelText(getString(R.string.sign_in));
                mAddFab.setVisibility(View.GONE);
                filter.setVisibility(View.GONE);
                inbox.setVisibility(View.GONE);
                mAddPlaceLayout.setVisibility(View.GONE);
                mPlacesHereFab.setVisibility(View.VISIBLE);
            } else {
                mAddFab.setVisibility(View.VISIBLE);
                mAddFab.setLabelText(getString(R.string.sign_in));
                inbox.setVisibility(View.GONE);
                filter.setVisibility(View.VISIBLE);
                mAddPlaceLayout.setVisibility(View.GONE);
                mPlacesHereFab.setVisibility(View.GONE);
            }
        } else {
            if (isGoogleCategory) {
                mAddFab.setLabelText(getString(R.string.sign_in));
                mAddFab.setVisibility(View.GONE);
                filter.setVisibility(View.GONE);
                inbox.setVisibility(View.GONE);
                mAddPlaceLayout.setVisibility(View.GONE);
                mPlacesHereFab.setVisibility(View.VISIBLE);
            } else {
                mAddFab.setVisibility(View.VISIBLE);
                mAddFab.setLabelText(getString(R.string.title_activity_add_business));
                inbox.setVisibility(View.VISIBLE);
                filter.setVisibility(View.VISIBLE);
                mAddPlaceLayout.setVisibility(View.VISIBLE);
                mPlacesHereFab.setVisibility(View.GONE);
            }
        }
    }

    private void getPlacesHere() {

            if (mCurrentLocation!=null && !Objects.equals(currentCategory, "") && !Objects.equals(currentIcon, "")){
                mMap.clear();
                moveCameraToGooglePlaces(mMap.getCameraPosition().target);
                PerformGetPlaces(currentCategory, currentIcon, String.valueOf(mMap.getCameraPosition().target.latitude),
                        String.valueOf(mMap.getCameraPosition().target.longitude),false);
                MarkerOptions markerOne = new MarkerOptions()
                        .position(new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude)).title("Drag to any location");
                target = new PicassoMarker(mMap.addMarker(markerOne), getApplication());
            }
    }



    private void checkGAPSForHighVersions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    setMyLocationOnMap();
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                }
            };

            new TedPermission(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                    .check();
        }
    }

    public void showChooseCategoryDilog(boolean canceable) {
        new MaterialDialog.Builder(this)
                .title(R.string.choose_places)
                .items(new String[]{getString(R.string.google_map_places), getString(R.string.app_places)})
                .cancelable(false)
                .itemsCallbackSingleChoice(1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        if (which == 0) {
                            mMap.clear();
                            isGoogleCategory = true;
                        } else {
                            mMap.clear();
                            isGoogleCategory = false;
                        }

                        adjustFabs();
                        //add drawer
                        addDrawer();

                        setupSearchView();
                        mLeftDrawer.openDrawer();


                        /**
                         * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                         * returning false here won't allow the newly selected radio button to actually be selected.
                         **/
                        return true;
                    }
                })
                .positiveText(R.string.choose)
                .show();

    }

    private void initiateGooglePlacesList() {
        placesCategoriesList = new ArrayList<>();
        placesCategoriesList.add(0, new PlacesCategories("Hôpitaux et cliniques", "hospital","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fhospital.png?alt=media&token=ab3c098f-3467-45ee-b761-1507c50440f3"));
        placesCategoriesList.add(1, new PlacesCategories("Médecins", "doctor","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fdoctor3.png?alt=media&token=dbdae35c-698b-465d-9d04-a7b20feb17c0"));
        placesCategoriesList.add(2, new PlacesCategories("Pharmaciens", "pharmacy","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fdoctor3.png?alt=media&token=dbdae35c-698b-465d-9d04-a7b20feb17c0"));
        placesCategoriesList.add(3, new PlacesCategories("Dentists", "dentist","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fdoctor3.png?alt=media&token=dbdae35c-698b-465d-9d04-a7b20feb17c0"));
        placesCategoriesList.add(4, new PlacesCategories("Restaurants", "restaurant","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fresturant.png?alt=media&token=8d64d944-b55f-4036-acd5-cf4bdefb5d78"));
        placesCategoriesList.add(5, new PlacesCategories("Cafeterias", "cafe", "https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fresturant.png?alt=media&token=8d64d944-b55f-4036-acd5-cf4bdefb5d78"));
        placesCategoriesList.add(6, new PlacesCategories("Parkings", "parking","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Ftaxi.png?alt=media&token=e5c84dc2-70a5-48fd-8e8b-99edfe0f36fa"));
        placesCategoriesList.add(7, new PlacesCategories("Stations service", "gas_station","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Ftaxi.png?alt=media&token=e5c84dc2-70a5-48fd-8e8b-99edfe0f36fa"));
        placesCategoriesList.add(8, new PlacesCategories("Stations Taxi", "taxi_stand","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Ftaxi.png?alt=media&token=e5c84dc2-70a5-48fd-8e8b-99edfe0f36fa"));
        placesCategoriesList.add(9, new PlacesCategories("Magazines", "store", "https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fagency.png?alt=media&token=74378db7-f7e6-497f-a3e4-a5e52129c09e"));
        placesCategoriesList.add(10, new PlacesCategories("Banques", "bank","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fagency.png?alt=media&token=74378db7-f7e6-497f-a3e4-a5e52129c09e"));
        placesCategoriesList.add(11, new PlacesCategories("Administrations", "local_government_office","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fagency.png?alt=media&token=74378db7-f7e6-497f-a3e4-a5e52129c09e"));
        placesCategoriesList.add(12, new PlacesCategories("Ecoles", "school","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fagency.png?alt=media&token=74378db7-f7e6-497f-a3e4-a5e52129c09e"));
        placesCategoriesList.add(13, new PlacesCategories("Universités", "university","https://firebasestorage.googleapis.com/v0/b/around-672b4.appspot.com/o/markers-icons%2Fagency.png?alt=media&token=74378db7-f7e6-497f-a3e4-a5e52129c09e"));
    }

    //region build google ads
    void initAdMob() {
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.ad_mob_real_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
//        NativeExpressAdView mAdView = (NativeExpressAdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);
        // Defined in res/values/strings.xml
        mInterstitialAd.setAdUnitId(getString(R.string.ad_mob_real_full_screen_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                reinitFullScreenAd();
            }
        });

        reinitFullScreenAd();
        generateToast();
    }

    void generateToast() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (true) {
                    try {
                        Thread.sleep(adsTimer);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {


                                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                    if (mustShowAdd) {
                                        mInterstitialAd.show();
                                    }
                                } else {
                                    //Toast.makeText(getBaseContext(), "Ad did not load", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                }
            }
        }).start();
    }

    void reinitFullScreenAd() {
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    //endregion

    private void connectUserOnline() {
        if (FirebaseUtil.getCurrentUserId() != null) {
            //add current user to online
            FirebaseUtil.getOnlineUsersRef().child(FirebaseUtil.getCurrentUserId()).setValue(FirebaseUtil.getCurrentUserId());
            //remove current user from online
            FirebaseUtil.getOnlineUsersRef().child(FirebaseUtil.getCurrentUserId()).onDisconnect().removeValue();
        }
    }

    /**
     * try to get user fcm token and update it on firebase
     */
    private void getFcmToken() {
        if (FirebaseUtil.getCurrentUserId() != null) {
            if (mSharedPrefUtilities.getFcmToken() == null) {
                final String token = FirebaseInstanceId.getInstance().getToken();
                Map<String, Object> updateValues = new HashMap<>();
                updateValues.put("fcm_token", token);
                updateValues.put("updated_at", System.currentTimeMillis());

                FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).updateChildren(
                        updateValues,
                        new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                                if (firebaseError != null) {
                                    Log.d(TAG, firebaseError.toString());
                                } else {
                                    mSharedPrefUtilities.setFcmToken(token);
                                    Log.d(TAG, "token added");
                                }
                            }
                        });
            }
        }
    }

    private void showFilterDialog() {
        boolean wrapInScrollView = true;
        dialog = new MaterialDialog.Builder(this)
                .customView(R.layout.search_custom_view, wrapInScrollView)
                .show();


        View view = dialog.getCustomView();

        mBusinessDistrictSpinner = (AppCompatSpinner) view.findViewById(R.id.district_spin);
        mBusinessCitySpinner = (AppCompatSpinner) view.findViewById(R.id.city_spin);
        mBusinessCategorySpinner = (AppCompatSpinner) view.findViewById(R.id.category_spin);
        mBusinessSpecializationSpinner = (AppCompatSpinner) view.findViewById(R.id.subcategory_spin);
        btn_search = (AppCompatButton) view.findViewById(R.id.btn_search);

        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBusinessSpecializationSpinner.getChildAt(0) != null) {
                    Category category = (Category) mBusinessCategorySpinner.getSelectedItem();

                    filterMap(mBusinessDistrictSpinner.getSelectedItem().toString(),
                            mBusinessCitySpinner.getSelectedItem().toString(),
                            category.getFr_name(),
                            mBusinessSpecializationSpinner.getSelectedItem().toString());
                }

            }
        });

        //intiate spinner views
        setupDistrictSpinner();

        setupCategorySpinner();

        updateUI();
    }

    /**
     * setup flaoting search view
     */
    private void setupSearchView() {
        if (mLeftDrawer != null) {
            View left_view = mSearchView.findViewById(R.id.left_action);
            left_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLeftDrawer.openDrawer();
                }
            });
        }
    }

    /**
     * add navigation drawer
     */
    private void addDrawer() {

        //initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).transform(new CircleTransformation()).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }

        });

        //check if current user is exist
        if (mCurrentUser != null) {
            if (!mCurrentUser.isAnonymous()) {//user is signed in but before add place need to verify his email
                // Create the AccountHeader
                ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem();
                if (mSharedPrefUtilities.getFullName() != null)
                    profileDrawerItem.withName(mSharedPrefUtilities.getFullName());
                if (mCurrentUser.getEmail() != null)
                    profileDrawerItem.withEmail(mCurrentUser.getEmail());
                if (mSharedPrefUtilities.getUserImageFull() != null)
                    profileDrawerItem.withIcon(Uri.parse(mSharedPrefUtilities.getUserImageFull()));

                mHeaderResult = new AccountHeaderBuilder()
                        .withActivity(this)
                        .withCompactStyle(true)
                        .withHeaderBackground(R.drawable.header)
                        .addProfiles(profileDrawerItem)
                        .withSelectionListEnabled(false)
                        .withSavedInstance(savedInstanceState)
                        .withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
                            @Override
                            public boolean onClick(View view, IProfile profile) {
                                intentToEditProfile();
                                return false;
                            }
                        })
                        .build();
            }
        }

        //create the drawer and remember the `Drawer` result object
        mLeftDrawer = new DrawerBuilder()
                .withActivity(this)
                .withMultiSelect(false)
                .withSavedInstance(savedInstanceState)
                .withShowDrawerOnFirstLaunch(true)
                .withHasStableIds(true)
                .withAccountHeader(mHeaderResult)
                .withCloseOnClick(true)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Log.e("Drawer", drawerItem.getIdentifier() + "  pos " + position);

                        if (drawerItem.getIdentifier() == 0) {
                            showChooseCategoryDilog(true);
                        } else if (drawerItem.getIdentifier() == -3) {//log out
                            logOut();
                        } else if (drawerItem.getIdentifier() == -2) {//about
                            goToAbout();
                        } else if (drawerItem.getIdentifier() > 199) {
//                            Log.e("Drawer", "getIdentifier > 200");
                            //user place clicked
                            mMap.clear();
                            BusinessPlace businessPlace = mUserBusiness.get((int) drawerItem.getIdentifier() - 200);
                            String key = businessPlace.getLatitude() + "," + businessPlace.getLongitude();
                            mBusinessPlaceList.put(key, businessPlace);
                            addMarker(businessPlace);
                            moveCameraToPosition(new LatLng(businessPlace.getLatitude(), businessPlace.getLongitude()));
                        } else {//navigate to place

                            if (isGoogleCategory) {
                                mMap.clear();
                                if (position != 1) {
                                    PlacesCategories category = placesCategoriesList.get((int) drawerItem.getIdentifier() - 50);
                                    if (mCurrentLocation!=null) {
                                        firstMarker = true;
                                        PerformGetPlaces(category.getApi_name(), category.getIcon(),String.valueOf(mCurrentLocation.getLatitude()),
                                                String.valueOf(mCurrentLocation.getLongitude()), true);
                                    }
                                    currentCategory = category.getApi_name();
                                    currentIcon = category.getIcon();
                                }

                            } else {
                                Category category = mCategoryList.get((int) drawerItem.getIdentifier() - 1);
                                getCategories(category.getFr_name());
                            }
                        }
                        return false;
                    }
                })
                .build();
        CustomUrlPrimaryDrawerItem customUrlPrimaryDrawerItem2 =
                new CustomUrlPrimaryDrawerItem().withIdentifier(0)
                        .withName(getResources().getString(R.string.choose_category))
                        .withIcon(R.drawable.marker_icon);
        mLeftDrawer.addItem(customUrlPrimaryDrawerItem2);
        mLeftDrawer.addItem(new SectionDrawerItem().withName(R.string.category));

        mCategoryList = new ArrayList<>();
//        Log.e("isGoogleCategory", isGoogleCategory + "");
        if (isGoogleCategory) {

            for (int i = 0; i < placesCategoriesList.size(); i++) {
                PlacesCategories category = placesCategoriesList.get(i);

                //add category as item in left drawer
                CustomUrlPrimaryDrawerItem customUrlPrimaryDrawerItem =
                        new CustomUrlPrimaryDrawerItem().withIdentifier(i + 50)
                                .withName(category.getFr_name())
                                .withIcon(category.getIcon());
                mLeftDrawer.addItem(customUrlPrimaryDrawerItem);
            }
            addOfflineItems();

        } else {
            counter = 1;
            //load data from firebase of category's
            FirebaseUtil.getCategoryRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Category category = postSnapshot.getValue(Category.class); // get the category
                            category.setFr_name(category.getFr_name());
                            mCategoryList.add(category);

                            //add category as item in left drawer
                            CustomUrlPrimaryDrawerItem customUrlPrimaryDrawerItem =
                                    new CustomUrlPrimaryDrawerItem().withIdentifier(counter).withName(category.getFr_name())
                                            .withIcon(category.getIcon());
                            mLeftDrawer.addItem(customUrlPrimaryDrawerItem);

                            if (counter == dataSnapshot.getChildrenCount()) {
                                //add user places
                                if (FirebaseUtil.getCurrentUserId() != null) {
                                    loadUserData();
                                } else {
                                    addOfflineItems();
                                }

                            }
                            counter = counter + 1;
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadCategories:onCancelled", databaseError.toException());
                    //don't show view and reload data

                    //add offline items
                    addOfflineItems();
                }
            });
        }
    }

    private void loadUserData() {
        counter = 0;
        mLeftDrawer.addItem(new SectionDrawerItem().withName(R.string.myplaces));
        //load user places
        FirebaseUtil.getUserBusinessRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                        String businessPlaceKey = placeSnapshot.getValue().toString();

                        FirebaseUtil.getBusinessRef().child(businessPlaceKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot buisnessdataSnapshot) {
                                if (buisnessdataSnapshot.getValue() != null) {
                                    BusinessPlace mBusinessPlace = buisnessdataSnapshot.getValue(BusinessPlace.class);
                                    mUserBusiness.add(mBusinessPlace);

                                    //add business as item in left drawer
                                    CustomUrlPrimaryDrawerItem customUrlPrimaryDrawerItem = new CustomUrlPrimaryDrawerItem().withIdentifier(userPlacesCounter).withName(mBusinessPlace.getTitle())
                                            .withIcon(mBusinessPlace.getIcon());
                                    mLeftDrawer.addItem(customUrlPrimaryDrawerItem);

                                    userPlacesCounter = userPlacesCounter + 1;


                                    if (counter == dataSnapshot.getChildrenCount()) {
                                        Log.d("Counter ", String.valueOf(counter) + " , " +
                                                String.valueOf(dataSnapshot.getChildrenCount()));
                                        //add offline items
                                        addOfflineItems();
                                    }
                                    //addOfflineItems();
                                    counter = counter + 1;
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    //addOfflineItems();
                } else
                    addOfflineItems();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //add offline items
                //   addOfflineItems();
            }
        });
    }

    /**
     * get places based on the selected category and papulate on map
     *
     * @param category
     */
    private void getCategories(String category) {
        if (mMap != null) {
            mProgressDialog.show();

            mMap.clear();
            //load user places
            myBusiness = new ArrayList<>();
            FirebaseUtil.getBusinessRef().orderByChild(BusinessPlace.CATEGORY).equalTo(category)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot buisnessdataSnapshot) {
                            if (buisnessdataSnapshot.getChildrenCount() == 0) {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.dismiss();
                                showSnackbar(R.string.no_places);
                                return;
                            }

                            for (DataSnapshot placeSnapshot : buisnessdataSnapshot.getChildren()) {
                                BusinessPlace place = placeSnapshot.getValue(BusinessPlace.class);
                                myBusiness.add(place);
                                String key = place.getLatitude() + "," + place.getLongitude();
                                mBusinessPlaceList.put(key, place);
                                addMarker(place);
                            }

                            //reset map camera
                            resetMapCamera();
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            showSnackbar(R.string.error_occurred);
                        }
                    });

        } else {
            showSnackbar(R.string.map_not_ready);
        }
    }

    //region places

    public void addPlacesMarker(String icon, LatLng place) {

        MarkerOptions markerOne = new MarkerOptions()
                .position(place).title("");

        target = new PicassoMarker(mMap.addMarker(markerOne), this);
        Picasso.with(MainActivity.this).load(icon).into(target);
    }

    public void PerformGetTextSearchPlaces(String query, String lat, String lng, final boolean fromMenue) {
        //"restaurants"
        mProgressDialog.show();
        ServiceBuilder builder = new ServiceBuilder();
        ServiceInterfaces.TextSearch textSearch = builder.textSearch();
        if (mCurrentLocation != null) {
            Call<PlacesRespose> apiModelCall = textSearch.textSearch(query, lat + "," + lng, "10000", Constants.SERVER_KEY);
            apiModelCall.enqueue(new Callback<PlacesRespose>() {
                @Override
                public void onResponse(Call<PlacesRespose> call, Response<PlacesRespose> response) {
                    int code = response.code();
                    if (response.body()!=null){
                        resultArrayList = response.body().results;
                    }
                    if (response.body().getResults().size() == 0) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        moveCameraToGooglePlaces(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        showSnackbar(R.string.no_places);
                        return;
                    }
                    Log.e("Response", new Gson().toJson(response.body()));
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        PerformGetDetails(response.body().getResults().get(i).getPlaceId());
                        LatLng latLng = new LatLng(response.body().getResults().get(i).getGeometry().getLocation().getLat(),
                                response.body().getResults().get(i).getGeometry().getLocation().getLng());
                        String key = response.body().getResults().get(i).getGeometry().getLocation().getLat() + "," +
                                response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        mGooglePlaceList.put(key,response.body().getResults().get(i));
                        addPlacesMarker(response.body().getResults().get(i).getIcon(), latLng);
                        if (fromMenue) {
                            moveCameraToGooglePlaces(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        }
//                    Log.e("currentLocation name ", response.body().getResults().get(i).getName());
//                    Log.e("currentLocation ratin ", String.valueOf(response.body().getResults().get(i).getRating()));
                        if (response.body().getResults().get(i).getPhotos() != null && response.body().getResults().get(i).getPhotos().size() > 0) {
                            for (int j = 0; j < response.body().getResults().get(i).getPhotos().size(); j++) {
//                            Log.e("currentLocation photo ", response.body().getResults().get(i).getPhotos().get(j).getPhotoReference());
                            }
                        }
                    }

                    //reset map camera
                    //resetMapCamera();
                    if (fromMenue) {
                        if (mCurrentLocation != null) {
                            LatLng mlatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                            moveCameraToGooglePlaces(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        }
                    }
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<PlacesRespose> call, Throwable t) {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    showSnackbar(R.string.error_occurred);
                }
            });
        }

    }

    public void PerformGetPlaces(String categoryName, final String Icon, String lat, String lng, final boolean fromMenue) {
        //"restaurants"
        Log.e("Category_key", categoryName);

        mProgressDialog.show();
        ServiceBuilder builder = new ServiceBuilder();
        ServiceInterfaces.Place place = builder.getPlaces();
        if (mCurrentLocation != null) {
            Call<PlacesRespose> apiModelCall = place.getPlaces(lat + "," + lng, "10000",
                    categoryName, Constants.SERVER_KEY);
            apiModelCall.enqueue(new Callback<PlacesRespose>() {
                @Override
                public void onResponse(Call<PlacesRespose> call, Response<PlacesRespose> response) {
                    int code = response.code();
                    if (response.body()!=null){
                        resultArrayList = response.body().results;
                    }
                    if (response.body().getResults().size() == 0) {
                        if (mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                        moveCameraToGooglePlaces(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        showSnackbar(R.string.no_places);
                        return;
                    }
                    Log.e("Response", new Gson().toJson(response.body()));
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        PerformGetDetails(response.body().getResults().get(i).getPlaceId());
                        LatLng latLng = new LatLng(response.body().getResults().get(i).getGeometry().getLocation().getLat(),
                                response.body().getResults().get(i).getGeometry().getLocation().getLng());
                        String key = response.body().getResults().get(i).getGeometry().getLocation().getLat() + "," +
                                response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        mGooglePlaceList.put(key,response.body().getResults().get(i));
                        addPlacesMarker(Icon, latLng);
                        if (fromMenue) {
                            moveCameraToGooglePlaces(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        }
//                    Log.e("currentLocation name ", response.body().getResults().get(i).getName());
//                    Log.e("currentLocation ratin ", String.valueOf(response.body().getResults().get(i).getRating()));
                        if (response.body().getResults().get(i).getPhotos() != null && response.body().getResults().get(i).getPhotos().size() > 0) {
                            for (int j = 0; j < response.body().getResults().get(i).getPhotos().size(); j++) {
//                            Log.e("currentLocation photo ", response.body().getResults().get(i).getPhotos().get(j).getPhotoReference());
                            }
                        }
                    }

                    //reset map camera
                    //resetMapCamera();
                    if (fromMenue) {
                        if (mCurrentLocation != null) {
                            LatLng mlatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                            moveCameraToGooglePlaces(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
                        }
                    }
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<PlacesRespose> call, Throwable t) {
                    if (mProgressDialog.isShowing())
                        mProgressDialog.dismiss();
                    showSnackbar(R.string.error_occurred);
                }
            });
        }

    }

    public void PerformGetDetails(String id) {
        //"restaurants"
        mProgressDialog.show();
        ServiceBuilder builder = new ServiceBuilder();
        ServiceInterfaces.Details details = builder.details();
        if (mCurrentLocation != null) {
            Call<Details> apiModelCall = details.details(id, Constants.SERVER_KEY);
            apiModelCall.enqueue(new Callback<Details>() {
                @Override
                public void onResponse(Call<Details> call, Response<Details> response) {
                    int code = response.code();
                    if (code == 200){
                        detailesResultsList.add(response.body().getResult());
                        String key = response.body().getResult().getGeometry().getLocation().getLat()+","+
                                response.body().getResult().getGeometry().getLocation().getLng();
                        mGooglePlaceDetailsList.put(key,response.body().getResult());
                    }
                    Log.e("Details", String.valueOf(response.body()));
                }

                @Override
                public void onFailure(Call<Details> call, Throwable t) {
                    Log.e("Details", "failed");
                }
            });
        }

    }
    //endregion

    private void addOfflineItems() {
        //add offline items
        mLeftDrawer.addItem(new DividerDrawerItem());
        //if you want to update the items at a later time it is recommended to keep it in a variable
        //check if current user is
        if (mCurrentUser != null) {
            if (!mCurrentUser.isAnonymous()) {//user is signed in but before add place need to verify his email
                SecondaryDrawerItem logOutItem = new SecondaryDrawerItem().withIdentifier(-3)
                        .withName(R.string.action_log_out).withIcon(getDrawable(R.drawable.ic_exit_to_app_black_24dp));
                mLeftDrawer.addItem(logOutItem);
            }
        }

        SecondaryDrawerItem aboutItem = new SecondaryDrawerItem().withIdentifier(-2)
                .withName(R.string.about_app).withIcon(getDrawable(R.drawable.ic_info_black_24dp));
        mLeftDrawer.addItem(aboutItem);
    }

    private void goToAbout() {
        startActivity(new Intent(this, AboutUS.class));
    }


    @OnClick(R.id.sign_in_text)
    public void signIn() {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the drawer to the bundle
        if (mLeftDrawer != null)
            outState = mLeftDrawer.saveInstanceState(outState);
        if (mHeaderResult != null)
            outState = mHeaderResult.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mustShowAdd = false;
    }

    @Override
    public void onBackPressed() {

        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (mLeftDrawer != null && mLeftDrawer.isDrawerOpen()) {
            mLeftDrawer.closeDrawer();
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.exit)
                    .content(R.string.exit_warnning)
                    .cancelable(false)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void logOut() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(getResources().getString(R.string.log_out_action));
        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //clear data from pref
                mSharedPrefUtilities.clearData();
                //sign out from firebase
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish(); // Call once you redirect to another activity
            }
        });
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), null);
        android.app.AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    /**
     * when click on fab animate fab to done button and show picker on the map
     */
    @OnClick(fab)
    void fabClicked() {
        //check if user email verified
        if (mCurrentUser != null) {
            startActivity(new Intent(this, AddBusiness.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    /**
     * check gps and location permission
     */
    private void checkGps() {
        if (!Utils.checkGps(this)) {
            if (mGpsDialog == null) {
                mGpsDialog = buildAlertMessageNoGps();
            }
            if (mGpsDialog != null) {
                mGpsDialog.show();
            }
        }
    }

    private AlertDialog buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.no_gps))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

    /**
     * Requests the location permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestLocationPermission() {
        Log.i(TAG, "Location permission has NOT been granted. Requesting permission.");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Log.i(TAG,
                    "Displaying Location permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar.make(mRootView, R.string.permission_location,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(MainActivity.this, PERMISSIONS_LOCATION,
                                            REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {

            // Storage permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_LOCATION, REQUEST_LOCATION);
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
        mProgressDialog = new Dialog(MainActivity.this);
        //   mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.custom_progress_dialog);
    }


    @OnClick(R.id.my_location_fab)
    void myLocationFabClicked() {
        if (mCurrentLocation != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
            // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
            moveCameraToPosition(new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude()));
            if (isGoogleCategory){
                //addLocationMarker(mCurrentLocation);
            }
        } else {
            checkGps();
        }
    }

    @OnClick(R.id.filter)
    void filter() {
        showFilterDialog();
    }

    @OnClick(R.id.inbox)
    void inbox() {
        if (FirebaseUtil.getCurrentUserId() != null)
            startActivity(new Intent(MainActivity.this, InboxActivity.class));
        else
            showSnackbar(R.string.login_first);
    }

    /**
     * move map camera to selected position
     *
     * @param latLng
     */
    private void moveCameraToPosition(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * move map camera to selected position
     *
     * @param latLng
     */
    private void moveCameraToGooglePlaces(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(12)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * move map camera to default position
     */
    private void resetMapCamera() {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(35.695340, 4.796031))      // Sets the center of the map to Mountain Vi
                .zoom(5)                   // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * move map camera to My position
     *
     * @param latLng
     */
    private void moveCameraToMyPosition(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)      // Sets the center of the map to Mountain View
                .zoom(14)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // Sets the map type to be "hybrid"
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap = googleMap;

        mMap.setOnMarkerDragListener(this);

        mPlacesHereFab.setOnClickListener(this);

//        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
//            @Override
//            public void onCameraChange(CameraPosition cameraPosition) {
//                mMap.clear();
//                MarkerOptions markerOne = new MarkerOptions()
//                        .position(new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude)).title("").draggable(true);
//                target = new PicassoMarker(mMap.addMarker(markerOne), getApplication());
//            }
//        });

        //disable toolbar navigation icon
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //populateBusinessPlaces();

        //check if user add new place navigate to it
        if (mNewAddedBusiness != null) {
            getCategories(mNewAddedBusiness.getCategory());
            moveCameraToPosition(new LatLng(mNewAddedBusiness.getLatitude(), mNewAddedBusiness.getLongitude()));
        }

        resetMapCamera();

        // Add a marker in Sydney and move the camera
        Log.i(TAG, "location map opened. Checking permission.");
        // Check if the lcoation permission is already available.
        // Verify that all required contact permissions have been granted.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.LOCATION_HARDWARE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Location permission has not been granted.
                //requestLocationPermission();
            }
        } else {
            setMyLocationOnMap();
        }

        //check if map my location is available
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (location != null) {
                    mCurrentLocation = location;
                    //change fab icon
                    mMyLocationFab.setImageDrawable(getDrawable(R.drawable.ic_my_location_black_24dp));
                    mPlacesHereFab.setImageDrawable(getDrawable(R.drawable.ic_pin_drop_black_24dp));
                } else {
                    mCurrentLocation = getLastKnownLocation();
                }

            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (!isGoogleCategory){
                    BusinessPlace place = new BusinessPlace();
                    place = (BusinessPlace) mBusinessPlaceList.get(marker.getPosition().latitude + "," + marker.getPosition().longitude);
                    if (place != null) {
                        startActivity(new Intent(MainActivity.this, MarkerDetailsActivity.class)
                                .putExtra("extra_place", place));
//                            .putExtra("lat",mCurrentLocation.getLatitude())
//                            .putExtra("lng",mCurrentLocation.getLongitude()));
                    }
                }else if (isGoogleCategory){
                    Result result = (Result) mGooglePlaceList.get(marker.getPosition().latitude + "," + marker.getPosition().longitude);
                    com.androidtech.around.Models.GooglePlaces.Details.Result detailsResult = new com.androidtech.around.Models.GooglePlaces.Details.Result();
                    detailsResult = (com.androidtech.around.Models.GooglePlaces.Details.Result)  mGooglePlaceDetailsList.get(marker.getPosition().latitude + "," + marker.getPosition().longitude);
                    if (result != null) {
                        googlePlaceDetails[0] = result.getName();
                        googlePlaceDetails[1] = String.valueOf(result.getRating());
                        googlePlaceDetails[2] = String.valueOf(detailsResult.getFormattedAddress());
                        googlePlaceDetails[3] = String.valueOf(detailsResult.getInternationalPhoneNumber());
                        googlePlaceDetails[4] = String.valueOf(result.getGeometry().getLocation().getLat());
                        googlePlaceDetails[5] = String.valueOf(result.getGeometry().getLocation().getLng());
                        startActivity(new Intent(MainActivity.this, GooglePlacesDetails.class)
                                .putExtra("extra_place", googlePlaceDetails));
                    }
                }
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                View v = getLayoutInflater().inflate(
                        R.layout.info_window, null);
                final TextView tv_title = (TextView) v.findViewById(R.id.tv_title);
                final TextView tv_category = (TextView) v.findViewById(R.id.tv_category);
                final TextView tv_description = (TextView) v.findViewById(R.id.tv_description);

                final ImageView iv_navigation = (ImageView) v.findViewById(R.id.iv_navigation);
                final ImageView iv_action = (ImageView) v.findViewById(R.id.iv_action);
                final ImageView iv_chat = (ImageView) v.findViewById(R.id.iv_chat);

                if(!isGoogleCategory){
                    if(mBusinessPlaceList.size() != 0){
                        BusinessPlace place = new BusinessPlace();
                        place = (BusinessPlace) mBusinessPlaceList.get(marker.getPosition().latitude + "," + marker.getPosition().longitude);
                        if (place != null) {
                            if (mCurrentUser != null && !mCurrentUser.isAnonymous())
                                iv_chat.setVisibility(View.VISIBLE);
                            tv_description.setText(place.getSpecialization());
                            tv_title.setText(place.getTitle());
                            tv_category.setText(place.getCategory());  //place.getspecialization
                        } else
                            showSnackbar(R.string.no_info_error);
                    }
                } else if (isGoogleCategory){
                    if(resultArrayList.size()!=0){
                        BusinessPlace place = new BusinessPlace();
                        Result result = (Result) mGooglePlaceList.get(marker.getPosition().latitude + "," + marker.getPosition().longitude);
                        com.androidtech.around.Models.GooglePlaces.Details.Result detailsResult = new com.androidtech.around.Models.GooglePlaces.Details.Result();
                        detailsResult = (com.androidtech.around.Models.GooglePlaces.Details.Result)  mGooglePlaceDetailsList.get(marker.getPosition().latitude + "," + marker.getPosition().longitude);
                        if (result != null) {
                            tv_description.setText(String.valueOf(detailsResult.getFormattedPhoneNumber()));
                            tv_title.setText(result.getName());
                            tv_category.setText(currentCategory);  //place.getspecialization
                        } else
                            showSnackbar(R.string.no_info_error);
                    }
                }

                return v;
            }
        });
    }

    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Location l = mLocationManager.getLastKnownLocation(provider);
                //Log.d("last known location, provider: %s, location: %s", provider, l);

                if (l == null) {
                    continue;
                }
                if (bestLocation == null
                        || l.getAccuracy() < bestLocation.getAccuracy()) {
                    //Log.d("found best last known location: %s", l);
                    bestLocation = l;
                }
            }
        }
        if (bestLocation == null) {
            mCurrentLocation = null;
            mMyLocationFab.setImageDrawable(getDrawable(R.drawable.ic_location_disabled_black_24dp));
            mPlacesHereFab.setImageDrawable(getDrawable(R.drawable.ic_pin_drop_black_24dp));
        }
        return bestLocation;
    }

    /**
     * go to edit profile screen
     */
    private void intentToEditProfile() {
        Intent intent = new Intent(this, EditProfile.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_LOCATION) {
            Log.i(TAG, "Received response for location permissions request.");

            // We have requested multiple permissions for contacts, so all of them need to be
            // checked.
            if (PermissionUtil.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display current location
                showSnackbar(R.string.location_permission_granted);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                setMyLocationOnMap();
            } else {
                Log.i(TAG, "Location permissions were NOT granted.");
                showSnackbar(R.string.location_permission_not_granted);
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * set my location button enabled on map and set padding to make
     */
    private void setMyLocationOnMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.LOCATION_HARDWARE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        View mMapView = findViewById(map);
        View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        locationButton.setVisibility(View.GONE);
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    private void populateBusinessPlaces() {
        mProgressDialog.show();
        mBusinessPlaceList = new HashMap<>();

        FirebaseUtil.getBusinessRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                    BusinessPlace place = placeSnapshot.getValue(BusinessPlace.class);
                    String key = place.getLatitude() + "," + place.getLongitude();
                    mBusinessPlaceList.put(key, place);
                    addMarker(place);
                }

                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mProgressDialog.dismiss();
            }
        });
    }

    public void addMarker(BusinessPlace place) {

        MarkerOptions markerOne = new MarkerOptions()
                .position(new LatLng(place.getLatitude(), place.getLongitude())).title("");

        target = new PicassoMarker(mMap.addMarker(markerOne), this);
        Picasso.with(MainActivity.this).load(place.getIcon()).into(target);
    }

    public void addLocationMarker(Location currentLocation) {

        if (firstMarker) {
            MarkerOptions markerOne = new MarkerOptions()
                    .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title("Drag to any location").draggable(true);
            target = new PicassoMarker(mMap.addMarker(markerOne), this);
        }
        firstMarker = false;
//        Picasso.with(MainActivity.this).load(place.getIcon()).into(target);
    }

    private void getAboutFromRemoteConfig() {

        // Get Remote Config instance.
        // [START get_remote_config_instance]
        final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // [END get_remote_config_instance]

        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        // [START enable_dev_mode]
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                // .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        // [END enable_dev_mode]

        // Set default Remote Config values. In general you should have in app defaults for all
        // values that you may configure using Remote Config later on. The idea is that you
        // use the in app defaults and when you need to adjust those defaults, you set an updated
        // value in the App Manager console. Then the next time you application fetches from the
        // server, the updated value will be used. You can set defaults via an xml file like done
        // here or you can set defaults inline by using one of the other setDefaults methods.S
        // [START set_default_values]
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        // [END set_default_values]

        long cacheExpiration = 36; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }

        // [START fetch_config_with_callback]
        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
        // fetched and cached config would be considered expired because it would have been fetched
        // more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
        // throttling is in progress. The default expiration duration is 43200 (12 hours).
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        }
                    }
                });
        // [END fetch_config_with_callback]

    }

    public void filterMap(final String district, final String city, final String category, final String sub_category) {
        if (!category.isEmpty()) {
            // category
            FirebaseUtil.getBusinessRef().orderByChild("category").equalTo(category).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    int index = 0;
                    ArrayList<BusinessPlace> mBusinessPlaces = new ArrayList<BusinessPlace>();
                    Map<Integer, Object> pp = new HashMap<Integer, Object>();

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        BusinessPlace businessPlace = postSnapshot.getValue(BusinessPlace.class);
                        //      pp.put(index,businessPlace);

                        if (!district.isEmpty() && !district.equals("None") && city.equals("None") && sub_category.equals("None")) {
                            if (businessPlace.getDistrict().equals(district)) {
                                pp.put(index, businessPlace);
                            }
                        }

                        if (!district.isEmpty() && !district.equals("None") && !city.isEmpty() && !city.equals("None")) {
                            if (businessPlace.getDistrict().equals(district) && businessPlace.getCity().equals(city))
                                pp.put(index, businessPlace);
                        }

                        if (!district.isEmpty() && !district.equals("None")
                                && !city.isEmpty() && !city.equals("None")
                                && !sub_category.isEmpty() && !sub_category.equals("None")) {
                            if (businessPlace.getDistrict().equals(district) && businessPlace.getSpecialization().equals(sub_category))
                                pp.put(index, businessPlace);
                        }

                        if (!sub_category.isEmpty() && !sub_category.equals("None")
                                && district.equals("None") && city.equals("None")) {
                            if (businessPlace.getSpecialization().equals(sub_category))
                                pp.put(index, businessPlace);
                        }

                        if (sub_category.equals("None") && district.equals("None") && city.equals("None")) {
                            pp.put(index, businessPlace);
                        }

                        index++;
                    }

                    mBusinessPlaceList.clear();

                    mMap.clear();

                    if (pp.isEmpty())
                        showSnackbar(R.string.no_places);
                    else {
                        Iterator it = pp.entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry) it.next();
                            BusinessPlace pl = (BusinessPlace) pair.getValue();
                            String key = pl.getLatitude() + "," + pl.getLongitude();
                            mBusinessPlaceList.put(key, pl);

                            addMarker(pl);
                            it.remove();
                        }

                        //reset map camera
                        resetMapCamera();

                    }


                    dialog.dismiss();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

    private void searchMapByTitle(final String currentQuery) {
        FirebaseUtil.getBusinessRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() != null) {
                    mBusinessPlaceList.clear();
                    mMap.clear();
                    LatLng latLng = null;
                    for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                        BusinessPlace mBusinessPlace = placeSnapshot.getValue(BusinessPlace.class);
                        if (mBusinessPlace.getTitle().contains(currentQuery)) {
                            String key = mBusinessPlace.getLatitude() + "," + mBusinessPlace.getLongitude();
                            mBusinessPlaceList.put(key, mBusinessPlace);
                            latLng = new LatLng(mBusinessPlace.getLatitude(), mBusinessPlace.getLongitude());
                            addMarker(mBusinessPlace);
                        }

                    }

                    //reset map camera
                    if (latLng != null)
                        moveCameraToPosition(latLng);
                    else
                        resetMapCamera();

                } else {
                    showSnackbar(R.string.no_places);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /***********************************************
     * Filter stuff
     ************************************************/
    private void setupDistrictSpinner() {
        //intiate gender spinner
        String[] emptyArray = new String[0];
        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(emptyArray));


        //intiate district spinner
        mDistrictsSpinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lst);
        mDistrictsSpinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBusinessDistrictSpinner.setAdapter(mDistrictsSpinAdapter);

        mBusinessDistrictSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mSpinnerListener) {
                    String district = mBusinessDistrictSpinner.getSelectedItem().toString();
                    if (!district.isEmpty()) {
                        //intiate city spinner
                        mCitySpinAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, getCityItems(district));
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


        ArrayList<Category> categories = new ArrayList<>();
        mSpinnerAdapter = new SpinnerAdapter(this, R.layout.item_spinner, categories);
        mBusinessCategorySpinner.setAdapter(mSpinnerAdapter);

        mBusinessCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mCategorySpinnerListener) {
                    Category category = (Category) mBusinessCategorySpinner.getAdapter().getItem(i);
                    if (!category.getFr_name().isEmpty()) {
                        //intiate city spinner
                        mSpecialitySpinAdapter = new ArrayAdapter<String>(MainActivity.this,
                                android.R.layout.simple_spinner_item,
                                getSpecializationItems(category.getFr_name()));
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

        //check internet first
        if (Utils.checkInternet(this)) {

            //load data from firebase of category's
            FirebaseUtil.getCategoryRef().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Category category = postSnapshot.getValue(Category.class); // get the category
                            category.setFr_name(category.getFr_name());
                            mCategoryList.add(category);
                        }
                        //add data to spinners
                        mSpinnerAdapter.clear();
                        mSpinnerAdapter.addAll(mCategoryList);
                        mSpinnerAdapter.notifyDataSetChanged();

                        if (!mDistrictsList.isEmpty()) {

                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    //don't show view and reload data

                }
            });


            //load data from firebase of districts
            FirebaseUtil.getDistrictsRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            Districs district = postSnapshot.getValue(Districs.class); // get the district
                            mDistrictsList.add(district);
                        }

                        //add data to spinners
                        mDistrictsSpinAdapter.clear();
                        mDistrictsSpinAdapter.addAll(getDistrictsItems());
                        mDistrictsSpinAdapter.notifyDataSetChanged();


                        if (!mCategoryList.isEmpty()) {

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    //don't show view and reload data
                }
            });
        }

    }

    private ArrayList<String> getDistrictsItems() {
        List<String> districts = new ArrayList<>();
        switch (mChoosingLanguage) {
            case LANGUAGE_ENGLISH:
                for (int i = 0; i < mDistrictsList.size(); i++) {
                    if (i == 0)
                        districts.add("None");
                    else
                        districts.add(mDistrictsList.get(i).en_name);
                }
                break;
        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(districts.toArray(new String[0])));
        return lst;
    }

    private ArrayList<String> getCityItems(String districtStr) {
        List<String> cities = new ArrayList<>();
        List<City> cityList = getCitiesOfDistrict(districtStr);
        if (districtStr.equals("None"))
            cities.add("None");
        switch (mChoosingLanguage) {
            case LANGUAGE_ENGLISH:
                for (City city : cityList) {
                    cities.add(city.en_name);
                }
                break;
        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(cities.toArray(new String[0])));
        return lst;
    }


    List<City> getCitiesOfDistrict(String districtStr) {
        for (Districs districs : mDistrictsList) {
            if (districs.en_name.equals(districtStr))
                return districs.cities;
        }
        return new ArrayList<>();
    }


    private ArrayList<String> getCategoryItems() {
        List<String> categorys = new ArrayList<>();
        switch (mChoosingLanguage) {
            case LANGUAGE_ENGLISH:
                for (Category category : mCategoryList) {
                    categorys.add(category.getFr_name());
                }
                break;

        }

        ArrayList<String> lst = new ArrayList<String>(Arrays.asList(categorys.toArray(new String[0])));
        return lst;
    }


    private ArrayList<String> getSpecializationItems(String categoryStr) {
        List<String> sub_category = new ArrayList<>();
        sub_category.add("None");
        ArrayList<String> lst = new ArrayList<>();
        List<Specialization> specializationList = getSubOfCategory(categoryStr);
        if (specializationList != null) {
            switch (mChoosingLanguage) {
                case LANGUAGE_ENGLISH:
                    for (Specialization specialization : specializationList) {
                        specialization.setfr_name(specialization.getfr_name());
                        sub_category.add(specialization.getfr_name());
                    }
                    break;
                case LANGUAGE_FRANCE:
                    for (Specialization specialization : specializationList) {
                        specialization.setfr_name(specialization.getfr_name());
                        sub_category.add(specialization.getfr_name());
                    }
                    break;
            }

            lst = new ArrayList<String>(Arrays.asList(sub_category.toArray(new String[0])));
        }

        return lst;
    }

    List<Specialization> getSubOfCategory(String categoryStr) {
        for (Category category : mCategoryList) {
            if (category.getFr_name().equals(categoryStr))
                return category.getmSpecializations();
        }
        return new ArrayList<>();
    }

    @Override
    protected void onResume() {
        if (mCurrentUser != null) {
            mCurrentUser.reload();
        }
        mustShowAdd = true;
        super.onResume();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // TODO Auto-generated method stub
        Log.e("System out", "onMarkerDragStart..."+marker.getPosition().latitude+"..."+marker.getPosition().longitude);
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // TODO Auto-generated method stub
        Log.e("System out", "onMarkerDragEnd..."+marker.getPosition().latitude+"..."+marker.getPosition().longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        // TODO Auto-generated method stub
        Log.e("System out", "onMarkerDrag...");
        mMap.clear();
        moveCameraToMyPosition(marker.getPosition());
        if (mCurrentLocation!=null && !Objects.equals(currentCategory, "") && !Objects.equals(currentIcon, "")){
            PerformGetPlaces(currentCategory, currentIcon, String.valueOf(marker.getPosition().latitude), String.valueOf(marker.getPosition().longitude),false);
        }
        MarkerOptions markerOne = new MarkerOptions()
                .position(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude)).title("Drag to any location");
        target = new PicassoMarker(mMap.addMarker(markerOne), this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.places_here_fab:
                getPlacesHere();
        }
    }
}
