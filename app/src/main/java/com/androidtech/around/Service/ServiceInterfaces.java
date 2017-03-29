package com.androidtech.around.Service;

import com.androidtech.around.Models.GooglePlaces.PlacesRespose;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Ahmed Badr on 14/6/2016.
 */
public interface ServiceInterfaces {

    interface Place {
        @GET("/maps/api/place/nearbysearch/json")
        Call<PlacesRespose> getPlaces(
                @Query("location") String location,
                @Query("radius") String radius,
                @Query("type") String type,
                @Query("key") String API_KEY
        );
    }
}
