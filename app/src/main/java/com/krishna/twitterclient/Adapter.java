package com.krishna.twitterclient;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.krishna.twitterclient.data.Status;

import java.util.ArrayList;

/**
 * Created by Krishna on 13/03/16.
 */
public class Adapter extends BaseAdapter {
    private static final String NO_DATA_TO_DISPLAY = "No Data To Display";
    private Context context;
    private ArrayList<Status> statusList;
    private int itemTweetView;
    private LayoutInflater inflater;

    /* ViewHolder class to hold the view */
    private static class ViewHolder {
        public TextView tvName;
        public TextView tvHandle;
        public TextView tvTweet;

        public ViewHolder(View view) {
            this.tvName = (TextView) view.findViewById(R.id.name);
            this.tvHandle = (TextView) view.findViewById(R.id.handle);
            this.tvTweet = (TextView) view.findViewById(R.id.tweet);
        }
    }

    public Adapter(Context context, ArrayList<Status> statusList, @LayoutRes int itemTweetView) {
        this.context = context;
        this.statusList = statusList;
        this.itemTweetView = itemTweetView;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(itemTweetView, null);
            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (statusList == null) {
            viewHolder.tvTweet.setText(NO_DATA_TO_DISPLAY);
        } else {
            viewHolder.tvHandle.setText(String.format("%s%s", "@", getItem(position).user.screenName));
            viewHolder.tvName.setText(getItem(position).user.name);
            viewHolder.tvTweet.setText(getItem(position).text);
        }
        return view;
    }

    @Override
    public int getCount() {
        return statusList.size();
    }

    @Override
    public Status getItem(int position) {
        return statusList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
