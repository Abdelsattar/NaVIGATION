package com.androidtech.around.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidtech.around.Models.Category;
import com.androidtech.around.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ahmed Donkl on 12/11/2016.
 */

public class SpinnerAdapter extends ArrayAdapter<Category> {

    Activity context;
    ArrayList<Category> list;
    LayoutInflater inflater;
    public SpinnerAdapter(Activity context, int id, ArrayList<Category>
            list){
        super(context,id,list);
        this.list=list;
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent ){

        ViewHolder  holder = null;

        if (convertView == null) {

            // inflate the layout
            convertView = inflater.inflate(R.layout.item_spinner, parent, false);

            // well set up the ViewHolder
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.spinner_txt);
            holder.icon = (ImageView) convertView.findViewById(R.id.spinner_img);

            // store the holder with the view.
            convertView.setTag(holder);
        }
        else
        {
            // we've just avoided calling findViewById() on resource every time
            // just use the viewHolder
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(list.get(position).getFr_name());
        Picasso.with(context).load(list.get(position).getIcon()).into( holder.icon);

        return convertView;
    }

    public View getDropDownView(int position, View convertView, ViewGroup
            parent){
        return getView(position,convertView,parent);

    }

    class ViewHolder {
        TextView name;
        ImageView icon;
    }
}