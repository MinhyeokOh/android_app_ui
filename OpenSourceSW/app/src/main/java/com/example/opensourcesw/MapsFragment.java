package com.example.opensourcesw;
import android.location.Geocoder;
import android.view.LayoutInflater;

import android.view.ViewGroup;
import android.os.Bundle;
import android.view.View;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import android.location.Address;
import java.util.List;
import java.util.ArrayList;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.example.opensourcesw.MainActivity;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import java.util.List;

public class MapsFragment extends SupportMapFragment implements OnMapReadyCallback, OnMapClickListener {
    private GoogleMap mMap;
    private String addressName;

    public MapsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        getMapAsync(this);
        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStack();
        transaction.commitNow();
        Geocoder geocoder = new Geocoder(getContext());


        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                this.addressName = address.getAddressLine(0);

            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public String getAddressName(){
        return this.addressName;
    }
}

