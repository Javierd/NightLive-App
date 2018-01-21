package com.javierd.nightlive;

import com.javierd.nightlive.Flyer.Flyers;
import com.javierd.nightlive.RestUtils.Points;
import com.javierd.nightlive.RestUtils.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestInterface {
    // Request method and URL specified in the annotation
    // Callback for the parsed response is the last parameter
    //public static final String BASE_URL = "http://192.168.1.92:5000/";
    public static final String BASE_URL = "http://javierd.pythonanywhere.com/";

    @POST("location?")
    Call<Integer> postUserLocation(@Query("lat") double latitude, @Query("long") double longitude, @Query("user") String user, @Query("token") String token, @Query("public") int pub);

    @GET("location?")
    Call<Points> getUserLocationMap(@Query("lat") double latitude, @Query("long") double longitude, @Query("user") String user, @Query("token") String token);

    @POST("user?")
    Call<User> userSingUp(@Query("name") String name, @Query("pass") String password, @Query("sex") int sex, @Query("mail") String mail, @Query("birthdate") long birthdate, @Query("styles") String styles);
    //-1==Wrong request 0==Correct sign up, 1==Used username, 2==Used mail

    @GET("user?")
    Call<User> userSignIn(@Query("mail") String mail, @Query("pass") String password);
    //"-1"==Wrong request, "1"==Wrong mail, "2"==Wrong password, else: username, correct login

    @GET("user/check?")
    Call<Integer> userCheckMail(@Query("mail") String mail);
    //-1==Wrong request 0==Correct mail, 1==Not used mail

    @GET("user/check?")
    Call<Integer> userCheckName(@Query("name") String name);
    //-1==Wrong request 0==Free user name, 1==Used user name

    @GET("business/flyers?")
    Call<Flyers> placeGetFlyers(@Query("placeId") String placeId);


}
