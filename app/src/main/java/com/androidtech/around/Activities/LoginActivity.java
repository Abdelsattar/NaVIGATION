package com.androidtech.around.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.androidtech.around.Models.User;
import com.androidtech.around.R;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.SharedPrefUtilities;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    //views section
    @BindView(R.id.login_email)
    EditText mEmailText;
    @BindView(R.id.login_password)
    EditText mPasswordText;
    @BindView(R.id.btn_login)
    Button mLoginButton;
    @BindView(R.id.password_layout)
    TextInputLayout mPasswordLayout;
    @BindView(R.id.email_layout)
    TextInputLayout mEmailLayout;
    @BindView(R.id.link_forget_password)
    TextView mForgetPasswordText;
    @BindView(R.id.sign_up_text)
    TextView mSignUp;

    //variables section
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private Dialog mProgressDialog;

    private SharedPrefUtilities mSharedPrefUtilities;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);

        //intaite Custom dialog
        customDialog();

        //make full screen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //get current user
        mCurrentUser = FirebaseUtil.getCurrentUser();
        if (mCurrentUser!=null)
             mCurrentUser.reload();

        //intiate firebase auth
        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.sign_up_text)
    void signUpClicked(){
        if(mCurrentUser!=null){
            intentToEditProfile();
            return;
        }

        mProgressDialog.show();
        mAuth.signInAnonymously().addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                mProgressDialog.dismiss();
                intentToEditProfile();
            }
        }).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                mProgressDialog.dismiss();
                showToast(R.string.error_occurred);
                Log.e(TAG, e.getMessage());
            }
        });
    }

    /**
     * show forget dialog to user
     */
    @OnClick(R.id.link_forget_password)
     void showForgetDialog() {

        new MaterialDialog.Builder(LoginActivity.this)
                .title(getResources().getString(R.string.recover_password_title))
                .positiveText(getResources().getString(R.string.button_text_send))
                .negativeText(getResources().getString(R.string.cancel))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .theme(Theme.LIGHT)
                .cancelable(false)
                .positiveColorRes(R.color.black)
                .negativeColorRes(R.color.black)
                .buttonRippleColorRes(R.color.black)
                .content(getResources().getString(R.string.enter_email))
                .inputType(InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT)
                .autoDismiss(false)
                .input(getResources().getString(R.string.email_hint), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(final MaterialDialog dialog, CharSequence input) {
                        //get email as string
                        String email = input.toString();
                        // check if email is valid
                        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            dialog.getInputEditText().setError(getResources().getString(R.string.wrong_email));
                        }else{
                            //disable buttons
                            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
                            dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
                            mAuth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Email sent.");
                                                dialog.dismiss();
                                                Toast.makeText(LoginActivity.this,getResources().getString(R.string.check_your_email), Toast.LENGTH_SHORT)
                                                        .show();
                                            }else{
                                                //enable buttons
                                                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
                                                dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(true);

                                                dialog.getInputEditText().setError(getResources().getString(R.string.wrong_email));
                                            }
                                        }
                                    });
                        }
                    }
                }).show();
    }

    @OnClick(R.id.btn_login)
     void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        mLoginButton.setEnabled(false);

        mProgressDialog.show();

        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            if (mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                           onLoginFailed();
                        }else if(task.isSuccessful()){
                            //load user data from firebase
                            FirebaseUtil.getUsersRef().child(FirebaseUtil.getCurrentUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot!=null){
                                        User user = dataSnapshot.getValue(User.class); // get the user
                                        if(user !=null){
                                           //save user data
                                            if(user.getFull_name()!=null){
                                                mSharedPrefUtilities.setFullName(user.getFull_name());
                                            }

                                            if(user.getProfile_picture_url()!=null){
                                                mSharedPrefUtilities.setUserImageFull(user.getProfile_picture_url());
                                            }
                                            //go to home screen
                                            IntentToMainActivity();
                                            return;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Getting Post failed, log a message
                                    if (mProgressDialog.isShowing())
                                        mProgressDialog.dismiss();
                                    onLoginFailed();

                                }
                            });

                        }

                    }
                });

    }

    private void IntentToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish(); // Call once you redirect to another activity

    }

    private void onLoginFailed() {
        Toast.makeText(getBaseContext(), getResources().getString(R.string.login_error), Toast.LENGTH_LONG).show();
        mLoginButton.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        String email = mEmailText.getText().toString();
        String password = mPasswordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailLayout.setError(getResources().getString(R.string.wrong_email));
            valid = false;
        } else {
            mEmailLayout.setError(null);
        }

        if (password.isEmpty()) {
            mPasswordLayout.setError(getResources().getString(R.string.wrong_password));
            valid = false;
        }else{
            mPasswordLayout.setError(null);
        }

        return valid;
    }

    private void customDialog() {
        mProgressDialog = new Dialog(LoginActivity.this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.custom_progress_dialog);
    }

    /**
     * go to edit profile screen
     */
    private void intentToEditProfile(){
        Intent intent = new Intent(this,EditProfile.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
    }

    @MainThread
    private void showToast(@StringRes int errorMessageRes) {
        Toast.makeText(this, errorMessageRes, Toast.LENGTH_LONG).show();
    }
}
