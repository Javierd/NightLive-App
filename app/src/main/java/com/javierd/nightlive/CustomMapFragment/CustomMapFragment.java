package com.javierd.nightlive.CustomMapFragment;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.maps.SupportMapFragment;

public class CustomMapFragment extends SupportMapFragment {
    public View mapView;
    public TouchableWrapper touchView;
    private CustomMapFragment.OnTouchListener listener;

    private long lastTouched = 0;
    private static final long SCROLL_TIME = 200L; // 200 Milliseconds

    public static CustomMapFragment newInstance() {
        return new CustomMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        mapView = super.onCreateView(inflater, parent, savedInstanceState);
        // overlay a touch view on map view to intercept the event
        touchView = new TouchableWrapper(getActivity());
        touchView.addView(mapView);
        return touchView;
    }

    @Override
    public View getView() {
        return mapView;
    }

    public void setOnTouchListener(CustomMapFragment.OnTouchListener listener) {
        this.listener = listener;
    }

    public interface OnTouchListener {
        void onTouch();
    }

    public class TouchableWrapper extends FrameLayout {
        public TouchableWrapper(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouched = SystemClock.uptimeMillis();
                    break;
                case MotionEvent.ACTION_UP:
                    final long now = SystemClock.uptimeMillis();
                    if (now - lastTouched > SCROLL_TIME) {
                        // Update the map
                        if (listener != null) {
                            listener.onTouch();
                        }
                    }
                    break;
            }

            return super.dispatchTouchEvent(event);
        }
    }
}