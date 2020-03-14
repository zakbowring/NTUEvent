package com.example.ntuevent.ui.registration;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.ntuevent.MainActivity;
import com.example.ntuevent.R;
import com.example.ntuevent.ui.registration.RegistrationViewModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.Region;

import java.util.HashMap;
import java.util.Map;

public class RegistrationFragment extends Fragment implements BeaconConsumer, View.OnClickListener {

    private RegistrationViewModel registrationViewModel;
    private static final int FINE_LOCATION_REQUEST_CODE = 1;
    private BeaconManager beaconManager;
    private boolean beaconServiceActive = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        registrationViewModel =
                ViewModelProviders.of(this).get(RegistrationViewModel.class);
        View root = inflater.inflate(R.layout.fragment_registration, container, false);
        final TextView textView = root.findViewById(R.id.text_registration);
        registrationViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        /* If active user isn't empty */
        if(MainActivity.activeUser != null)
            checkIfUserAlreadyRegistered();

        /* Registers user by beacon */
        if (MainActivity.activeUser != null) {
            /* Request location permissions */
            requestFineLocation();

            /* Setup beacon layout to be detected */
            setBeaconLayout();
        } else
            Toast.makeText(getContext(), "You must be logged into access this feature. Please login and try again.", Toast.LENGTH_SHORT).show();

        /* Set listeners */
        root.findViewById(R.id.registration_button).setOnClickListener(this);

        return root;
    }

    private void setBeaconLayout(){
        /* Gets instance of the beacon manager to run */
        beaconManager = BeaconManager.getInstanceForApplication(getContext());

        /* Sets the layout the app is looking for to that of an estimote beacon */
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        /* Binds activity to the beacon service */
        beaconManager.bind(this);

        beaconServiceActive = true;
    }

    private void requestFineLocation(){
        /* Check that fine location permissions is enabled on the device */
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            /* If it isn't ask for it */
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* Unbinds the activity from the beacon service if the service was activated */
        if(beaconServiceActive == true)
            beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        /* Removes all monitor notifiers */
        beaconManager.removeAllMonitorNotifiers();

        /* Defines functions to call upon detetion/loss of sight of beacons */
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                /* Runs when beacon detected */

                /* Register user for the event */
                registerUserAtEvent();
            }

            @Override
            public void didExitRegion(Region region) {
                /* Runs whenever the application leaves the range of the beacon */
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {
                /* Runs when the state of the beacon changes */
            }
        });

        try {
            /* Starts the monitoring of the application for beacons fitting the specified layout */
            beaconManager.startMonitoringBeaconsInRegion(new Region("", null, null, null));
        } catch (RemoteException e) {    }
    }

    @Override
    public Context getApplicationContext() {
        return null;
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {

    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return false;
    }

    private void registerUserAtEvent(){
        /* Get the active events Information */
        /* Get Firestore instance */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Retrieve active events info */
        firebaseFirestore.collection("registers").document("events").collection("activeEvents").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        /* If contents returned */

                        String eventName = "";
                        String eventId = "";

                        /* Will only ever be one contents returned */
                        for(QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            eventId = documentSnapshot.get("eventId").toString();
                            eventName = documentSnapshot.get("eventName").toString();
                        }

                        /* Need to validate user isn't already registered */
                        validateUserIsNotRegistered(eventId, eventName);
                    }
                });
    }

    private void validateUserIsNotRegistered(final String eventId, final String eventName){
        /* Get Firestore instance */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users").document(MainActivity.activeUser.email).collection("registeredEvents").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        boolean userRegistered = false;

                        /* Look through every registered event */
                        for(QueryDocumentSnapshot document : task.getResult()){
                            if(document.get("eventId").toString().equals(eventId)){
                                /* They're already registered */
                                userRegistered = true;
                                break;
                            }
                        }

                        if(userRegistered == false)
                            registerUserSide(eventId, eventName);
                    }
                });
    }

    private void registerUserSide(final String eventId, final String eventName){
        /* Get Firebase Instance */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Create map with info to upload to user in database */
        Map<String, Object> contents = new HashMap<>();
        contents.put("eventId", eventId);
        contents.put("eventName", eventName);

        /* Uploads registered info to the users account */
        firebaseFirestore.collection("users").document(MainActivity.activeUser.email).collection("registeredEvents").document(eventName)
                .set(contents).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                /* Once registered on user side, do event side */
                registerEventSide(eventId, eventName);
            }
        });
    }

    private void registerEventSide(String eventId, String eventName){
        /* Get Firebase Instance */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        Map<String, Object> contents = new HashMap<>();
        contents.put("name", MainActivity.activeUser.username);
        contents.put("email", MainActivity.activeUser.email);

        /* Upload register info on event side */
        firebaseFirestore.collection("registers").document("events").collection("activeEvents").document(eventName)
                .collection("attendees").document(MainActivity.activeUser.email).set(contents).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "You have successfully been registered.", Toast.LENGTH_SHORT).show();

                removeRegisterButton();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.registration_button:
                /* Remind user if they're not logged in */
                if(MainActivity.activeUser == null)
                    Toast.makeText(getContext(), "You must be logged into access this feature. Please login and try again.", Toast.LENGTH_SHORT).show();
                else
                    registerUserAtEvent();
                break;
        }
    }

    private void removeRegisterButton(){
        Button registerButton = (Button) getView().findViewById(R.id.registration_button);
        registerButton.setVisibility(View.GONE);

        ImageView successfulRegistrationImageView = (ImageView) getView().findViewById(R.id.successful_registration_imageview);
        successfulRegistrationImageView.setVisibility(View.VISIBLE);

        TextView successfulRegistrationTextView = (TextView) getView().findViewById(R.id.successful_registration_text);
        successfulRegistrationTextView.setVisibility(View.VISIBLE);
    }

    private void checkIfUserAlreadyRegistered() {
        /* Checks if user is already registered upon opening page */
        /* Get Firestore instance */
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Retrieve active events info */
        firebaseFirestore.collection("registers").document("events").collection("activeEvents").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        /* If contents returned */
                        if (task.isSuccessful()) {

                            String tempEventName = "";
                            String tempEventId = "";

                            /* Will only ever be one contents returned */
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                tempEventId = documentSnapshot.get("eventId").toString();
                                tempEventName = documentSnapshot.get("eventName").toString();
                            }

                            final String eventName = tempEventName;
                            final String eventId = tempEventId;

                            firebaseFirestore.collection("users").document(MainActivity.activeUser.email).collection("registeredEvents").get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            boolean userRegistered = false;

                                            /* Look through every registered event */
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String tempEventId = document.get("eventId").toString();
                                                if (document.get("eventId").toString().equals(eventId)) {
                                                    /* They're already registered */
                                                    userRegistered = true;
                                                    break;
                                                }
                                            }

                                            if (userRegistered == true) {
                                                removeRegisterButton();
                                            }
                                        }
                                    });

                        }
                    }
                });
    }
}