package com.krishna.twitterclient.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Status {

    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("id_str")
    @Expose
    public String idStr;
    @SerializedName("text")
    @Expose
    public String text;
    @SerializedName("source")
    @Expose
    public String source;
    @SerializedName("user")
    @Expose
    public User user;

}