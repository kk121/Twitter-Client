package com.krishna.twitterclient.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AllTweets {

    @SerializedName("statuses")
    @Expose
    public ArrayList<Status> statuses = new ArrayList<Status>();

}