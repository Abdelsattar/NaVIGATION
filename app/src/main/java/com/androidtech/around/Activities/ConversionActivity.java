package com.androidtech.around.Activities;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidtech.around.Adapters.ConversionAdapter;
import com.androidtech.around.App;
import com.androidtech.around.Models.FcmNotificationBuilder;
import com.androidtech.around.Models.Message;
import com.androidtech.around.Models.User;
import com.androidtech.around.R;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.SharedPrefUtilities;
import com.androidtech.around.Utils.Utils;
import com.androidtech.around.Widgets.CircleTransformation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConversionActivity extends AppCompatActivity {

    //views section
    @BindView(android.R.id.content)
    View mRootView;
    @BindView(R.id.message_recycler)
    RecyclerView mConversionRecycler;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.message_edit)
    EditText mMessageEditText;
    @BindView(R.id.message_send)
    RelativeLayout mSendMessage;
    @BindView(R.id.user_image_online)
    ImageView mOnlineImage;
    @BindView(R.id.chat_user_image)
    ImageView mToolbarUserImage;
    @BindView(R.id.user_name_toolbar)
    TextView mToolbarUserName;
    @BindView(R.id.online_text)
    TextView mOnlineText;

    //variables section
    public static String TAG = "ConversionActivity";
    public static final String USER_EXTRA = "USER_EXTRA";
    ConversionAdapter mConversionAdapter;
    List<Message> mMessageResponse;
    private User mChattingUser;
    private Dialog mProgressDialog;
    private boolean mIsLastMessageSeen = false;
    private LinearLayoutManager mLinearLayoutManager;
    Handler mDelayHandler ;

    private SharedPrefUtilities mSharedPrefUtilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversion);
        ButterKnife.bind(this);

        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
            mChattingUser =  Parcels.unwrap(bundle.getParcelable(USER_EXTRA));

        //change toolbar style
        customizeToolbar();

        //intaite Custom dialog
        customDialog();

        //setup feed
        setupFeed();

        getFeedsFromServer();

        //check if user online
        checkUserOnline();

        mDelayHandler = new Handler();
    }

    /**
     * check if current user is online or not
     */
    private void checkUserOnline() {
        FirebaseUtil.getOnlineUsersRef().child(mChattingUser.getUser_id()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!=null){//user is online
                    mOnlineImage.setImageResource(R.drawable.online_dotted);
                    mOnlineText.setText(R.string.online);
                }else{//user is offline
                    mOnlineImage.setImageResource(R.drawable.offline_dotted);
                    mOnlineText.setText(R.string.offline);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Error getting user status of online");
            }
        });
    }

    //seen last message
    private void seenLastMessage(final Message message, String room_type) {
        mIsLastMessageSeen = true;
        if(FirebaseUtil.getCurrentUserId().equals(message.getReceiverUid())){//if i'm the receiver of the message update it to be seen
            Map<String, Object> updateValues = new HashMap<>();
            updateValues.put("seen",true);

            FirebaseUtil.getChatRoomsRef().child(room_type).child(String.valueOf(message.getTimestamp())).updateChildren(
                    updateValues,
                    new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference databaseReference) {

                            if (firebaseError != null) {
                                Log.e(TAG, "message "+message.getTimestamp()+" has error to updated to seen");

                            }else{
                                Log.e(TAG, "message "+message.getTimestamp()+" has been updated to seen");
                            }
                        }
                    });
        }
    }

    private void customDialog() {
        mProgressDialog = new Dialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.custom_progress_dialog);
    }

    private void getFeedsFromServer(){

        mProgressDialog.show();

        //check internet first
        if(Utils.checkInternet(this)){
            //load messages from firebase
            getMessageFromFirebaseUser(FirebaseUtil.getCurrentUserId(),mChattingUser.getUser_id());
        }else{
            //stop progress and show error
            if (mProgressDialog.isShowing())
                mProgressDialog.dismiss();
            showSnackbar(R.string.error_occured);
        }
    }

    /**
     * get messages between users from firebase
     * @param senderUid
     * @param receiverUid
     */
    private void getMessageFromFirebaseUser(String senderUid, String receiverUid) {
        final String room_type_1 = senderUid + "_" + receiverUid;
        final String room_type_2 = receiverUid + "_" + senderUid;

        FirebaseUtil.getChatRoomsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                if (dataSnapshot.hasChild(room_type_1)) {
                    Log.e(TAG, "getMessageFromFirebaseUser: " + room_type_1 + " exists");

                    FirebaseUtil.getChatRoomsRef().child(room_type_1).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot!=null){
                                mMessageResponse = new ArrayList<Message>();
                                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                    Message message = postSnapshot.getValue(Message.class);
                                    mMessageResponse.add(message);
                                }
                                mConversionAdapter.addAll(mMessageResponse);

                                mDelayHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {//try to smooth scroll to end of recycler after delay
                                        try {
                                            //scroll to last position
                                            mConversionRecycler.smoothScrollBy(0, mConversionRecycler.getChildAt(0).getHeight() * mConversionAdapter.getItemCount());

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                }, 100);

                                if(!mIsLastMessageSeen)
                                    seenLastMessage(mMessageResponse.get(mMessageResponse.size() - 1),room_type_1);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            showSnackbar(R.string.error_occured);
                        }
                    });
                } else if (dataSnapshot.hasChild(room_type_2)) {
                    Log.e(TAG, "getMessageFromFirebaseUser: " + room_type_2 + " exists");
                    FirebaseUtil.getChatRoomsRef().child(room_type_2).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot!=null){
                                mMessageResponse = new ArrayList<Message>();
                                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                                    Message message = postSnapshot.getValue(Message.class);
                                    mMessageResponse.add(message);
                                }
                                mConversionAdapter.addAll(mMessageResponse);

                                mDelayHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {//try to smooth scroll to end of recycler after delay
                                        try {
                                            //scroll to last position
                                            mConversionRecycler.smoothScrollBy(0, mConversionRecycler.getChildAt(0).getHeight() * mConversionAdapter.getItemCount());

                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }

                                    }
                                }, 100);

                                if(!mIsLastMessageSeen)
                                    seenLastMessage(mMessageResponse.get(mMessageResponse.size() - 1),room_type_2);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            showSnackbar(R.string.error_occured);
                        }
                    });
                } else {
                    Log.e(TAG, "getMessageFromFirebaseUser: no such room available");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
                showSnackbar(R.string.error_occured);
            }
        });
    }

    private void setupFeed() {
        mLinearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };

        mLinearLayoutManager.setStackFromEnd(true);
        mConversionRecycler.setLayoutManager(mLinearLayoutManager);

        mConversionRecycler.setOverScrollMode(View.OVER_SCROLL_NEVER);

        mMessageResponse = new ArrayList<>();
        mConversionAdapter = new ConversionAdapter(this,mMessageResponse,mChattingUser);
        mConversionRecycler.setAdapter(mConversionAdapter);
    }

    @OnClick(R.id.message_send)
    void sendMessage(){
        String messageStr = mMessageEditText.getText().toString();
        if(messageStr.isEmpty()){
            mSendMessage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
            return;
        }

        Message message = new Message(FirebaseUtil.getCurrentUserId(),
                mChattingUser.getUser_id(),
                messageStr,System.currentTimeMillis(),
                false);

        //send message
        sendMessageToFirebaseUser(message);
    }

    public void sendMessageToFirebaseUser(final Message message) {
        mSendMessage.setEnabled(false);
        final String room_type_1 = message.getSenderUid() + "_" + message.getReceiverUid();
        final String room_type_2 = message.getReceiverUid() + "_" + message.getSenderUid();

        FirebaseUtil.getChatRoomsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(room_type_1)) {
                    Log.e(TAG, "sendMessageToFirebaseUser: " + room_type_1 + " exists");
                    FirebaseUtil.getChatRoomsRef().child(room_type_1).child(String.valueOf(message.getTimestamp())).setValue(message);
                    sendNotification(message);
                } else if (dataSnapshot.hasChild(room_type_2)) {
                    Log.e(TAG, "sendMessageToFirebaseUser: " + room_type_2 + " exists");
                    FirebaseUtil.getChatRoomsRef().child(room_type_2).child(String.valueOf(message.getTimestamp())).setValue(message);
                    sendNotification(message);
                } else {
                    Log.e(TAG, "sendMessageToFirebaseUser: success");
                    FirebaseUtil.getChatRoomsRef().child(room_type_1).child(String.valueOf(message.getTimestamp())).setValue(message);
                    sendNotification(message);
                    getFeedsFromServer();
                }
                mSendMessage.setEnabled(true);
                mMessageEditText.getText().clear();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mSendMessage.setEnabled(true);
                showSnackbar(R.string.error_occurred);
            }
        });
    }

    private void sendNotification(Message message) {
                String username = mSharedPrefUtilities.getFullName();
                String messageStr = message.getMessage();
                String uid =FirebaseUtil.getCurrentUserId();
                String firebaseToken = mSharedPrefUtilities.getFcmToken();
                String receiverFirebaseToken = mChattingUser.getFcm_token();
        FcmNotificationBuilder.initialize()
                .title(username)
                .message(messageStr)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(receiverFirebaseToken)
                .send();
    }

    /**
     * change toolbar style
     */
    private void customizeToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar supportedToolbar = getSupportActionBar();
       // supportedToolbar.setDisplayHomeAsUpEnabled(true);
        supportedToolbar.setDisplayShowTitleEnabled(false);
        supportedToolbar.setDisplayShowCustomEnabled(true);
        //set user name and image
        if(mChattingUser.getFull_name()!=null)
            mToolbarUserName.setText(mChattingUser.getFull_name());
        if(mChattingUser.getProfile_picture_url()!=null){
            Picasso.with(this).load(mChattingUser.getProfile_picture_url()).transform(new CircleTransformation()).into(mToolbarUserImage);
        }else{
            mToolbarUserImage.setImageResource(R.drawable.profile_image);
        }

    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    //on click on user image
    @OnClick(R.id.chat_user_image)
    void profileImageClicked(){
        showFullImage();
    }

    void showFullImage(){
        if(mChattingUser.getProfile_picture_url()!=null){
            final Dialog nagDialog = new Dialog(this,android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
            nagDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            nagDialog.setCancelable(true);
            nagDialog.setContentView(R.layout.preview_image);

            ImageView ivPreview = (ImageView)nagDialog.findViewById(R.id.iv_preview_image);
            if(mChattingUser.getProfile_picture_url()!=null)
                Picasso.with(this).load(mChattingUser.getProfile_picture_url()).into(ivPreview);

            nagDialog.show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        App.setChatActivityOpen(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        App.setChatActivityOpen(false);
    }
}
