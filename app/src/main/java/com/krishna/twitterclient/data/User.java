package com.krishna.twitterclient.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("id_str")
    @Expose
    public String idStr;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("screen_name")
    @Expose
    public String screenName;
    @SerializedName("profile_image_url")
    @Expose
    public String profileImageUrl;
    @SerializedName("profile_image_url_https")
    @Expose
    public String profileImageUrlHttps;

}