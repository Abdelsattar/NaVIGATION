package com.androidtech.around.Models;

import java.util.List;

/**
 * Created by Badr on 29/3/2017.
 */

public class PlacesCategories {

    String fr_name;
    String api_name;
    String icon;

    public PlacesCategories(String fr_name, String api_name, String icon) {
        this.fr_name = fr_name;
        this.api_name = api_name;
        this.icon = icon;
    }

    public void setFr_name(String fr_name) {
        this.fr_name = fr_name;
    }

    public String getIcon() {
        return icon;
    }

    public String getFr_name() {
        return fr_name;
    }

    public String getApi_name() {
        return api_name;
    }
}
