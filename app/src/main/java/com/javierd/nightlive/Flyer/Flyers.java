package com.javierd.nightlive.Flyer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Flyers {

    @SerializedName("flyers")
    @Expose
    private List<Flyer> flyers = null;

    public List<Flyer> getFlyers() {
        return flyers;
    }

    public void setFlyers(List<Flyer> flyers) {
        this.flyers = flyers;
    }

}
