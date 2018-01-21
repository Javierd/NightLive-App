package com.javierd.nightlive.PlaceStyle;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.javierd.nightlive.R;

import java.util.ArrayList;

public class StylesGridViewAdapter extends ArrayAdapter {
    private Context context;
    private int layoutResourceId;
    private ArrayList<PlaceStyle> data = new ArrayList<PlaceStyle>();

    public StylesGridViewAdapter(Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View grid = convertView;
        ViewHolder holder = null;

        if (grid == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            grid = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.styleName = (TextView) grid.findViewById(R.id.styleNameTextView);
            holder.styleImage = (ImageView) grid.findViewById(R.id.styleImageView);
            grid.setTag(holder);
        } else {
            holder = (ViewHolder) grid.getTag();
        }

        PlaceStyle item = data.get(position);
        holder.styleName.setText(item.getName());
        holder.styleImage.setImageResource(item.getDrawable());
        Boolean selected = item.getSelected();
        if(selected){
            holder.styleName.setBackgroundColor(ContextCompat.getColor(context, R.color.gridBackgroundFooter));
        }else{
            holder.styleName.setBackgroundColor(ContextCompat.getColor(context, R.color.gridBackgroundFooterClear));
        }
        return grid;
    }

    static class ViewHolder {
        TextView styleName;
        ImageView styleImage;
    }

}
