package com.androidtech.around.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Ahmed Donkl on 21/4/16.
 */

/**
 * this is helper  class to communicate with  shared preference
 */
public class SharedPrefUtilities {

    //constants
    private static String FULL_NAME = "fullName";
    private static String USER_IMAGE_FULL = "userImageFull";
    private static String APP_FIRST_OPEN = "app_first_open";

    private static String FCM_TOKEN = "fcm_token";
    SharedPreferences preferences;

    public SharedPrefUtilities(Context context){
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * clear data from shared pref
     */
    public void clearData(){
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }


    /**
     *
     * @param fullName
     * add fullName to shared preference
     */
    public void setFullName(String fullName) {
        preferences.edit().putString(FULL_NAME, fullName).apply();
    }

    /**
     * @return Full name from shared preference
     */
    public String getFullName() {
        return preferences.getString(FULL_NAME, null);
    }


    /**************************/
    public void setUserImageFull(String userImageFull) {
        preferences.edit().putString(USER_IMAGE_FULL, userImageFull).apply();
    }

    public String getUserImageFull() {
        return preferences.getString(USER_IMAGE_FULL, null);
    }
    /**************************/

    public void setFirstOpen(boolean isFirst ){
        preferences.edit().putBoolean(APP_FIRST_OPEN , isFirst).apply();
    }

    public boolean getFirstOpen(){
        return preferences.getBoolean(APP_FIRST_OPEN , true);
    }
    /**************************/
    public void setFcmToken(String fcmToken) {
        preferences.edit().putString(FCM_TOKEN, fcmToken).apply();
    }

    public String getFcmToken() {
        return preferences.getString(FCM_TOKEN, null);
    }
    /**************************/
}
