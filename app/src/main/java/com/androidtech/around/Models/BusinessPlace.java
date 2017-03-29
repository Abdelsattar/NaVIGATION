package com.androidtech.around.Models;

import android.os.Parcelable;

import org.parceler.Parcel;

/**
 * Created by OmarAli on 09/12/2016.
 */

@Parcel
public class BusinessPlace implements Parcelable{
    public static String OWNER_ID = "owner_id";
    public static String IMAGE = "picture";
    public static String TITLE = "title";
    public static String PHONE = "phone";
    public static String LOCATION = "location";
    public static String LAT = "latitude";
    public static String LONG = "longitude";
    public static String DESCRIPTION = "description";
    public static String CATEGORY = "category";
    public static String SPECIALIZATION = "specialization";
    public static String DISTRICT = "district";
    public static String CITY = "city";
    public static String UPDATED_AT = "updated_at";
    public static String CREATED_AT = "created_at";

    String owner_id , image , icon , title , phone ,email, location , description , category , specialization , district , city ,
            about , updated_at , created_at ;
    double latitude, longitude;

    public BusinessPlace(){};

    protected BusinessPlace(android.os.Parcel in) {
        owner_id = in.readString();
        image = in.readString();
        icon = in.readString();
        title = in.readString();
        phone = in.readString();
        email = in.readString();
        location = in.readString();
        description = in.readString();
        category = in.readString();
        specialization = in.readString();
        district = in.readString();
        city = in.readString();
        about = in.readString();
        updated_at = in.readString();
        created_at = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<BusinessPlace> CREATOR = new Creator<BusinessPlace>() {
        @Override
        public BusinessPlace createFromParcel(android.os.Parcel in) {
            return new BusinessPlace(in);
        }

        @Override
        public BusinessPlace[] newArray(int size) {
            return new BusinessPlace[size];
        }
    };

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getIcon() {
        return icon;
    }

    public String getAbout() {
        return about;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(android.os.Parcel parcel, int i) {
        parcel.writeString(owner_id);
        parcel.writeString(image);
        parcel.writeString(icon);
        parcel.writeString(title);
        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(location);
        parcel.writeString(description);
        parcel.writeString(category);
        parcel.writeString(specialization);
        parcel.writeString(district);
        parcel.writeString(city);
        parcel.writeString(about);
        parcel.writeString(updated_at);
        parcel.writeString(created_at);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
    }
}
