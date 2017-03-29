package com.androidtech.around.Utils;

/**
 * Created by Ahmed Donkl on 11/13/2016.
 */


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    public static String profileImages = "profileImages";

    public static DatabaseReference getBaseRef() {
      //  FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getChatRoomsRef() {
        return getBaseRef().child("chat_rooms");
    }


    public static DatabaseReference getOnlineUsersRef() {
        return getBaseRef().child("online");
    }

    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            return user.getUid();
        }
        return null;
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static boolean isCurrentUserAnonymous() {
        return getCurrentUser().isAnonymous();
    }

       public static DatabaseReference getUsersRef() {
        return getBaseRef().child("users");
    }

    public static DatabaseReference getUserBusinessRef() {
        return getCurrentUserRef().child("business");
    }


    public static DatabaseReference getDistrictsRef() {
        return getBaseRef().child("districs");
    }

    public static DatabaseReference getCategoryRef() {
        return getBaseRef().child("category");
    }

    public static DatabaseReference getBusinessRef() {
        return getBaseRef().child("business");
    }


    public static DatabaseReference getCurrentUserRef() {
        String uid = getCurrentUserId();
        if (uid != null) {
            return getBaseRef().child("users").child(getCurrentUserId());
        }
        return null;
    }
}
