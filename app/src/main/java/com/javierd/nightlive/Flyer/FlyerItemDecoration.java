package com.javierd.nightlive.Flyer;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class FlyerItemDecoration  extends RecyclerView.ItemDecoration {
    private int space;

    public FlyerItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        /*outRect.left = space;
        outRect.right = space;
        outRect.bottom = space;*/

        // Add top margin for every item except the first one
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = space;
        }
    }
}