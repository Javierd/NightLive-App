package com.javierd.nightlive.GMapPlace;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.javierd.nightlive.PlaceActivity;
import com.javierd.nightlive.R;

import java.util.List;

public class GMapPlacePreviewAdapter extends RecyclerView.Adapter<GMapPlacePreviewAdapter.GMapPlaceViewHolder>{
    private List<GMapPlace> items;

    static class GMapPlaceViewHolder extends  RecyclerView.ViewHolder{
        ImageView imageView;
        ProgressBar imageProgressbar;
        TextView nameTextView;
        TextView descTextView;
        TextView ratingTextView;
        RatingBar ratingBar;

        GMapPlaceViewHolder(View v){
            super(v);
            imageView = (ImageView) v.findViewById(R.id.imageView);
            imageProgressbar = (ProgressBar) v.findViewById(R.id.imageProgressBar);
            nameTextView = (TextView) v.findViewById(R.id.nameTextView);
            descTextView = (TextView) v.findViewById(R.id.descTextView);
            ratingTextView = (TextView) v.findViewById(R.id.ratingTextView);
            ratingBar = (RatingBar) v.findViewById(R.id.ratingBar);
        }
    }

    public GMapPlacePreviewAdapter(List<GMapPlace> items){
        this.items = items;
    }

    @Override
    public int getItemCount(){
        return items.size();
    }

    @Override
    public GMapPlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.cardview_place, viewGroup, false);
        return new GMapPlaceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final GMapPlaceViewHolder viewHolder, int i){
        viewHolder.descTextView.setText(items.get(i).getDesc());
        viewHolder.nameTextView.setText(items.get(i).getName());
        viewHolder.ratingTextView.setText(String.valueOf(items.get(i).getRating())+"/5");
        viewHolder.ratingBar.setRating(items.get(i).getRating());
        Bitmap image = items.get(i).getImage();
        if(image == null){
            viewHolder.imageProgressbar.setVisibility(View.VISIBLE);
        }else{
            viewHolder.imageProgressbar.setVisibility(View.INVISIBLE);
            viewHolder.imageView.setImageBitmap(image);
        }

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GMapPlace place = items.get(viewHolder.getAdapterPosition());

                Intent i = new Intent(view.getContext(), PlaceActivity.class);

                i.putExtra("place", place);
                i.putExtra("latlng", place.getLocation());
                PlaceImageHelper.image = place.getImage();

                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation((Activity) view.getContext(), view, view.getTransitionName());

                view.getContext().startActivity(i, options.toBundle());
            }
        });
    }
}
