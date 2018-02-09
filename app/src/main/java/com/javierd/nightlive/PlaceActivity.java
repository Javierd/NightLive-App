package com.javierd.nightlive;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.javierd.nightlive.Flyer.Flyer;
import com.javierd.nightlive.Flyer.FlyerItemDecoration;
import com.javierd.nightlive.Flyer.FlyerPreviewAdapter;
import com.javierd.nightlive.Flyer.Flyers;
import com.javierd.nightlive.GMapPlace.GMapPlace;
import com.javierd.nightlive.GMapPlace.PlaceImageHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceActivity extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        imageView = (ImageView) findViewById(R.id.imageView);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        GMapPlace place = getIntent().getParcelableExtra("place");
        LatLng location = getIntent().getParcelableExtra("latlng");
        place.changeLocation(location);
        place.changeImage(PlaceImageHelper.image);
        imageView.setImageBitmap(place.getImage());
        PlaceImageHelper.image = null;
        ratingBar.setRating(place.getRating());

        if(Utils.isOnline(PlaceActivity.this)){
            setUpFlyers(place.getId());
        }
        setUpToolbar(place.getName());
    }

    void setUpToolbar(String title){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null)
            return;
        //TODO It doesnt work well getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(title != null)
            getSupportActionBar().setTitle(title);
    }

    void setUpFlyers(String placeId){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface apiService =
                retrofit.create(RestInterface.class);

        Call<Flyers> call = apiService.placeGetFlyers(placeId);
        call.enqueue(new Callback<Flyers>() {
            @Override
            public void onResponse(@NonNull Call<Flyers> call, @NonNull Response<Flyers> response) {
                int statusCode = response.code();
                switch(statusCode){
                    case 400:
                        //Bad request
                        Toast.makeText(PlaceActivity.this, getString(R.string.wrong_server_direction), Toast.LENGTH_LONG).show();
                        break;
                }

                Flyers result = response.body();
                if(result == null) return;

                List<Flyer> flyerList = result.getFlyers();

                int spaceInPixels = getResources().getDimensionPixelSize(R.dimen.flyer_card_padding_bottom);
                RecyclerView flyerRecycler = (RecyclerView) findViewById(R.id.flyerRecyclerView);
                RecyclerView.LayoutManager flyerLManager = new LinearLayoutManager(PlaceActivity.this, LinearLayoutManager.VERTICAL, false);
                flyerRecycler.setLayoutManager(flyerLManager);
                flyerRecycler.addItemDecoration(new FlyerItemDecoration(spaceInPixels));
                RecyclerView.Adapter flyerAdapter = new FlyerPreviewAdapter(flyerList);
                flyerRecycler.setAdapter(flyerAdapter);
                flyerAdapter.notifyDataSetChanged();

                for (int i = 0; i < flyerList.size(); i++){
                    Flyer flyer = flyerList.get(i);
                    Log.i(flyer.getName(), flyer.getInfo());
                    if(flyer.getColor() !=null ){
                        //Log.i("Color", flyer.getColor());
                    }else{
                        //Log.i("ColorNull", flyer.getQr());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Flyers> call, @NonNull Throwable t) {
                // Log error here since request failed
                Log.i("Error gettings flyers", String.valueOf(t));
                Toast.makeText(PlaceActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            }
        });
    }
}
