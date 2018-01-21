package com.javierd.nightlive.PlaceStyle;

public class PlaceStyle {
    private String name;
    private boolean selected;
    private int drawable;

    public PlaceStyle(String name, boolean selected, int drawable){
        this.name = name;
        this.selected = selected;
        this.drawable = drawable;
    }

    public String getName(){
        return name;
    }

    public boolean getSelected(){
        return selected;
    }

    public int getDrawable(){
        return drawable;
    }

    public int getId() {
        return name.hashCode();
    }

    public void setName(String name){
        this.name = name;
    }

    public void setSelected(Boolean selected){
        this.selected = selected;
    }

    public static PlaceStyle getItem(PlaceStyle[] ITEMS, int id) {
        for (PlaceStyle item : ITEMS) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }
}
