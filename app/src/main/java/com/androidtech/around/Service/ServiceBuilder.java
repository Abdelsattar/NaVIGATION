package com.androidtech.around.Service;

import com.androidtech.around.Utils.Constants;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ahmed Badr on 14/6/2016.
 */
public class ServiceBuilder {

    public Retrofit retrofit;
    Constants constants;

    public ServiceBuilder(){
        retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASIC_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public ServiceInterfaces.Place getPlaces(){
        return retrofit.create(ServiceInterfaces.Place.class);
    }
}
