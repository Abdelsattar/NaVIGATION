package com.androidtech.around;

/**
 * Created by Ahmed Donkl on 10/29/2016.
 */


import android.util.Log;

import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.SharedPrefUtilities;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.Map;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    private SharedPrefUtilities mSharedPrefUtilities;

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);
        //intiate firebase database
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(final String token) {
        Map<String, Object> updateValues = new HashMap<>();
        updateValues.put("fcm_token", token);
        updateValues.put("updated_at", System.currentTimeMillis());

        if (FirebaseUtil.getCurrentUserId()!=null)
        FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).updateChildren(
                updateValues,
                new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {
                        if (firebaseError != null) {
                            Log.d(TAG,firebaseError.toString());
                            mSharedPrefUtilities.setFcmToken(token);
                        }else{
                            Log.d(TAG,"token added");
                        }
                    }
                });
    }
}