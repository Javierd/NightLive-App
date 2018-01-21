package com.javierd.nightlive.Flyer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Flyer {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("qr")
    @Expose
    private String qr;
    @SerializedName("price")
    @Expose
    private float price;
    @SerializedName("info")
    @Expose
    private String info;
    @SerializedName("startTimestamp")
    @Expose
    private long startTimestamp;
    @SerializedName("endTimestamp")
    @Expose
    private long endTimestamp;

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

}
