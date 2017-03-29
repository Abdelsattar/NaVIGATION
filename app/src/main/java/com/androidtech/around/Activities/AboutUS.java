package com.androidtech.around.Activities;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import com.androidtech.around.R;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.Calendar;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutUS extends AppCompatActivity {


    private View aboutPage;
    // Remote Config keys
    public static final String ABOUT_PHRASE_CONFIG_KEY = "about_phrase";
    public static final String CONTACT_MAIL_KEY = "contact_mail";
    public static final String FACEBOOK_PAGE = "facebook_page";
    //public static final String TWITTER_PAGE = "twitter_page";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String aboutText = mFirebaseRemoteConfig.getString(ABOUT_PHRASE_CONFIG_KEY);
        String aboutEmail = mFirebaseRemoteConfig.getString(CONTACT_MAIL_KEY);
        String facebook = mFirebaseRemoteConfig.getString(FACEBOOK_PAGE);
        //String twitter = mFirebaseRemoteConfig.getString(TWITTER_PAGE);

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.logo)
                .addItem(new Element().setTitle("Version 1.0"))
                .setDescription(aboutText)
                .addGroup(getString(R.string.connect_us))
                .addEmail(aboutEmail)
                .addFacebook(facebook)
                .addItem(getCopyRightsElement())
                .create();
        Log.d("facebook ",facebook);

        setContentView(aboutPage);
    }


    Element getCopyRightsElement() {
        Element copyRightsElement = new Element();
        final String copyrights = String.format(getString(R.string.copy_right), Calendar.getInstance().get(Calendar.YEAR));
        copyRightsElement.setTitle(copyrights);
        copyRightsElement.setIcon(R.drawable.about_icon_copy_right);
        copyRightsElement.setColor(ContextCompat.getColor(this, mehdi.sakout.aboutpage.R.color.about_item_icon_color));
        copyRightsElement.setGravity(Gravity.CENTER);
        return copyRightsElement;
    }
}
