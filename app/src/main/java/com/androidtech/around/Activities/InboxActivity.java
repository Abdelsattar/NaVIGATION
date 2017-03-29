package com.androidtech.around.Activities;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.androidtech.around.Adapters.InboxAdapter;
import com.androidtech.around.App;
import com.androidtech.around.Models.Message;
import com.androidtech.around.Models.User;
import com.androidtech.around.R;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class InboxActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,SearchView.OnQueryTextListener{

    //views section
    @BindView(android.R.id.content)
    View mRootView;
    @BindView(R.id.inbox_recycler_view)
    RecyclerView mInboxRecycler;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mRefreshLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    //variables section
    public static String TAG = "InboxActivity";
    private InboxAdapter mInboxAdapter;
    private List<User> feedResponse;
    private List<Message> mMessagesList;
    private Dialog mProgressDialog;
    private SearchView mSearchView;
    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        ButterKnife.bind(this);
        //change toolbar style
        customizeToolbar();

        //intaite Custom dialog
        customDialog();

        //implement pull to refresh
        pullToRefresh();

        //setup feed
        setupFeed();

        getFeedsFromServer();
    }

    private void customDialog() {
        mProgressDialog = new Dialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mProgressDialog.setContentView(R.layout.custom_progress_dialog);
    }

    @Override
    public void onRefresh() {
        getFeedsFromServer();
    }

    /**
     * implement pull to refresh functionality
     */
    private void pullToRefresh(){
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    private void getFeedsFromServer(){

        mRefreshLayout.setRefreshing(true);
        //check internet first
        if(Utils.checkInternet(this)){
            //load data from firebase of districts
            FirebaseUtil.getChatRoomsRef().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot!=null){
                        mInboxAdapter.getUserItems().clear();
                        //access chat rooms
                        for (DataSnapshot chatRoomsSnapshot: dataSnapshot.getChildren()) {
                            mUser = new User();

                            //access chat
                            mMessagesList = new ArrayList<Message>();
                            for (DataSnapshot chatsSnapshot: chatRoomsSnapshot.getChildren()) {
                                Message message = chatsSnapshot.getValue(Message.class);
                                mMessagesList.add(message);
                            }

                            //get last message in this conversion
                            final Message message = mMessagesList.get(mMessagesList.size() -1);
                            if( message.getReceiverUid().equals(FirebaseUtil.getCurrentUserId())
                                    || message.getSenderUid().equals(FirebaseUtil.getCurrentUserId())){

                                //get the other user id
                                String otherUid ;
                                if(message.getReceiverUid().equals(FirebaseUtil.getCurrentUserId()))
                                    otherUid = message.getSenderUid();
                                else
                                    otherUid = message.getReceiverUid();

                                //get user profile
                                FirebaseUtil.getUsersRef().child(otherUid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot!=null){
                                            //complete user with profile
                                            mUser = dataSnapshot.getValue(User.class);
                                            //set user last message data
                                            mUser.setLast_message(message.getMessage());
                                            mUser.setLast_message_seen(message.isSeen());
                                            mUser.setLast_message_timestamp(message.getTimestamp());
                                            mUser.setLast_message_sender(message.getSenderUid());

                                            //add user to list array
                                            mInboxAdapter.addUser(mUser);
                                        }else{
                                            mRefreshLayout.setRefreshing(false);
                                            showSnackbar(R.string.error_occured);
                                            return;
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        mRefreshLayout.setRefreshing(false);
                                        showSnackbar(R.string.error_occured);
                                        return;
                                    }
                                });
                            }

                        }


                        mRefreshLayout.setRefreshing(false);

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                    //stop progress and show error
                    mRefreshLayout.setRefreshing(false);

                    showSnackbar(R.string.error_occured);
                }
            });
        }else{
            //stop progress and show error
            mRefreshLayout.setRefreshing(false);

            showSnackbar(R.string.error_occured);
        }
    }

    private void setupFeed() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this) {
            @Override
            protected int getExtraLayoutSpace(RecyclerView.State state) {
                return 300;
            }
        };
        mInboxRecycler.setLayoutManager(linearLayoutManager);

        feedResponse = new ArrayList<>();
        mInboxAdapter = new InboxAdapter(this,feedResponse);
        mInboxRecycler.setAdapter(mInboxAdapter);
    }

    /**
     * change toolbar style
     */
    private void customizeToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar supportedToolbar = getSupportActionBar();
        supportedToolbar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contacts, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<User> filteredModelList = filter(query);
        mInboxAdapter.addAll(filteredModelList);
        mInboxRecycler.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        final List<User> filteredModelList = filter(query);
        mInboxAdapter.addAll(filteredModelList);
        mInboxRecycler.scrollToPosition(0);
        return false;
    }

    private List<User> filter(String query) {
        query = query.toLowerCase();
        final List<User> filteredModelList = new ArrayList<>();
        for (User model : feedResponse) {
            final String text = model.getFull_name().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }


    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
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
