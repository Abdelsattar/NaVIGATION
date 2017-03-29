/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidtech.around.Models;

import org.parceler.Parcel;

@Parcel
public class User {
    private String full_name;
    private String profile_picture_url;
    private long created_at;
    private long updated_at;
    private String email;
    private String gender;
    private String district;
    private String age;
    private String city;
    private String phone_number;
    private String fcm_token;
    private String user_id;

    private String last_message;
    private String last_message_sender;
    private long last_message_timestamp;
    private boolean last_message_seen;

    public User() {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public boolean isLast_message_seen() {
        return last_message_seen;
    }

    public void setLast_message_seen(boolean last_message_seen) {
        this.last_message_seen = last_message_seen;
    }

    public long getLast_message_timestamp() {
        return last_message_timestamp;
    }

    public void setLast_message_timestamp(long last_message_timestamp) {
        this.last_message_timestamp = last_message_timestamp;
    }

    public String getLast_message_sender() {
        return last_message_sender;
    }

    public void setLast_message_sender(String last_message_sender) {
        this.last_message_sender = last_message_sender;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getFcm_token() {
        return fcm_token;
    }

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getCity() {
        return city;
    }

    public String getAge() {
        return age;
    }

    public String getProfile_picture_url() {
        return profile_picture_url;
    }

    public long getCreated_at() {
        return created_at;
    }

    public long getUpdated_at() {
        return updated_at;
    }

    public String getEmail() {
        return email;
    }

    public String getGender() {
        return gender;
    }

    public String getDistrict() {
        return district;
    }
}
