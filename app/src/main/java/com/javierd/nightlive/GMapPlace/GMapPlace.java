package com.javierd.nightlive.GMapPlace;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class GMapPlace implements Parcelable {
    private String id;
    private String name;
    private String desc;
    private float rating;
    private Bitmap img;
    private LatLng location;
    private String address;
    private String phoneNumber;
    private Uri websiteUri;

    public GMapPlace(String id, String name, String desc, float rating, Bitmap img, LatLng location, String address, Uri websiteUri, String phoneNumber){
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.rating = rating;
        this.img = img;
        this.location = location;
        this.address = address;
        this.websiteUri = websiteUri;
        this.phoneNumber = phoneNumber;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDesc(){
        return desc;
    }

    public float getRating(){
        return rating;
    }

    public Bitmap getImage(){
        return img;
    }

    public LatLng getLocation(){
        return location;
    }

    public String getAddress() {
        return address;
    }

    public Uri getWebsiteUri() {
        return websiteUri;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void changeImage(Bitmap bitmap){
        this.img = bitmap;
    }

    public void changeLocation(LatLng location){
        this.location = location;
    }

    //Make the object parcelable so that we can pass it between activities

    public GMapPlace(Parcel in){
        String[] data= new String[7];
        in.readStringArray(data);

        this.id = data[0];
        this.name = data[1];
        this.desc =  data[2];
        this.rating = Float.parseFloat(data[3]);
        this.address =  data[4];
        this.websiteUri = Uri.parse(data[5]);
        this.phoneNumber =  data[6];
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub
        dest.writeStringArray(new String[]{
                this.id, this.name, this.desc, String.valueOf(this.rating), this.address, String.valueOf(this.websiteUri), this.phoneNumber
        });
    }

    public static final Parcelable.Creator<GMapPlace> CREATOR= new Parcelable.Creator<GMapPlace>() {

        @Override
        public GMapPlace createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new GMapPlace(source);  //using parcelable constructor
        }

        @Override
        public GMapPlace[] newArray(int size) {
            // TODO Auto-generated method stub
            return new GMapPlace[size];
        }
    };
}
