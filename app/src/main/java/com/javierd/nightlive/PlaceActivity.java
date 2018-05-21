package com.javierd.nightlive;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceActivity extends NetworkActivity {

    private ImageView imageView;
    private List<Flyer> flyerList;
    private RecyclerView flyerRecycler;
    private RecyclerView.Adapter flyerAdapter;
    private Snackbar mNetworkSnackbar;
    private GMapPlace place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);

        postponeEnterTransition();

        imageView = (ImageView) findViewById(R.id.imageView);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        TextView descriptionTextView = (TextView) findViewById(R.id.description);


        place = getIntent().getParcelableExtra("place");
        LatLng location = getIntent().getParcelableExtra("latlng");
        place.changeLocation(location);
        place.changeImage(PlaceImageHelper.image);
        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){

            @Override
            public boolean onPreDraw() {
                try {
                    imageView.setImageBitmap(place.getImage());
                    return true;    //note, that "true" is important, since you don't want drawing pass to be canceled
                } finally {
                    imageView.getViewTreeObserver().removeOnPreDrawListener(this);    //we don't need any further notifications
                    startPostponedEnterTransition();
                }
            }
        });

        PlaceImageHelper.image = null;
        ratingBar.setRating(place.getRating());
        descriptionTextView.setText(place.getDesc());

        /*Set up the recycler view*/
        int spaceInPixels = getResources().getDimensionPixelSize(R.dimen.flyer_card_padding_bottom);
        flyerList = new ArrayList<>();
        flyerRecycler = (RecyclerView) findViewById(R.id.flyerRecyclerView);
        flyerRecycler.setNestedScrollingEnabled(false);
        RecyclerView.LayoutManager flyerLManager = new LinearLayoutManager(PlaceActivity.this, LinearLayoutManager.VERTICAL, false);
        flyerRecycler.setLayoutManager(flyerLManager);
        flyerRecycler.addItemDecoration(new FlyerItemDecoration(spaceInPixels));
        flyerAdapter = new FlyerPreviewAdapter(flyerList);
        flyerRecycler.setAdapter(flyerAdapter);

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

        //TODO Improve the animation. It is not smooth enough
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(title != null)
            getSupportActionBar().setTitle(title);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q="+place.getLocation().latitude+","+place.getLocation().longitude
                    +"("+Uri.encode(place.getName()+")"));

                Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                try {
                    startActivity(intent);
                }catch (ActivityNotFoundException e){
                    Toast.makeText(PlaceActivity.this, getResources().getString(R.string.no_map_application), Toast.LENGTH_SHORT).show();
                }
            }
        });
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

                List<Flyer> flyers = result.getFlyers();

                for(Flyer f: flyers){
                    flyerList.add(f);
                    flyerAdapter.notifyItemInserted(flyerList.size() - 1);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Flyers> call, @NonNull Throwable t) {
                // Log error here since request failed
                Log.i("Error gettings flyers", String.valueOf(t));
                // Make sure the error is not due to internet connection
                if(Utils.isOnline(PlaceActivity.this)){
                    Toast.makeText(PlaceActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            /*Ensure the animation is displayed when we go back*/
            supportFinishAfterTransition();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onNetworkChange(){
        if(Utils.isOnline(PlaceActivity.this)){
            Log.i("NETWORK", "Connected");
            if(mNetworkSnackbar != null && mNetworkSnackbar.isShown()){
                mNetworkSnackbar.dismiss();
            }
        }else{
            //TODO Look for better options
            if(mNetworkSnackbar != null && mNetworkSnackbar.isShown()){
                mNetworkSnackbar.dismiss();
            }

            mNetworkSnackbar = Snackbar.make(findViewById(R.id.coordinator),
                    R.string.no_internet, Snackbar.LENGTH_INDEFINITE);
            mNetworkSnackbar.show();
            Log.i("NETWORK", "Disconnected");
        }
    }
}
