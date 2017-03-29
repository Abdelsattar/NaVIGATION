package com.androidtech.around.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.androidtech.around.R;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by OmarAli on 15/12/2016.
 */

public class IntroActivity extends AppIntro2{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setZoomAnimation();

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance(getString(R.string.title_bg_screen)
                , getString(R.string.description_bg_screen), R.drawable.intro1, getResources().getColor(R.color.light_blue)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.title_bg_screen1)
                , getString(R.string.description_bg_screen1), R.drawable.intro2, getResources().getColor(R.color.light_blue)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.title_bg_screen2)
                , getString(R.string.description_bg_screen2), R.drawable.intro3, getResources().getColor(R.color.light_blue)));


        // Hide Skip/Done button.
        showSkipButton(true);
        setProgressButtonEnabled(true);

    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Do something when users tap on Skip button.
        startApp();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        // Do something when users tap on Done button.
        startApp();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }


    public void startApp(){
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}
