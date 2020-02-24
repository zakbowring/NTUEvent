package com.example.ntuevent.ui.navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.ntuevent.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class NavigationFragment extends Fragment implements OnMapReadyCallback {

    private NavigationViewModel navigationViewModel;
    private GoogleMap mMap;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        navigationViewModel =
                ViewModelProviders.of(this).get(NavigationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_navigation, container, false);
        final TextView textView = root.findViewById(R.id.text_navigation);
        navigationViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if(getActivity()!=null) {
            SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map));
//            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
//                    .findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Bundle eventBundle = getArguments();

        /* If true then not come via an event */
        if(eventBundle == null ){
            /* Set camera to start over Nottingham */
            /* Set lat lng */
            LatLng startLatLang = new LatLng(52.958435, -1.154190);

            /* Move camera to lat lng */
            float zoom = 4.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLang, zoom));
        }
        else{
            /* Setup map for event */
            /* Get data from event bundle */
            String location = getArguments().get("location").toString();
            String eventName = getArguments().get("eventName").toString();
            String latitude = getArguments().get("latitude").toString();
            String longitude = getArguments().get("longitude").toString();

            /* Get latitude and longitude */
            LatLng startLatLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

            /* Add marker to map with title */
            mMap.addMarker(new MarkerOptions().position(startLatLng).title(eventName + ". " + location));

            /* Move the camera to lat lng and zoom*/
            float zoom = 14.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, zoom));
        }
    }
}
