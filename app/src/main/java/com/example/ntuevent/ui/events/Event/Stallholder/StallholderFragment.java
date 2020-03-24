package com.example.ntuevent.ui.events.Event.Stallholder;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.example.ntuevent.R;
import com.example.ntuevent.Stallholder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class StallholderFragment extends Fragment {

    private StallholderViewModel mViewModel;

    private Stallholder stallholder;

    public static StallholderFragment newInstance() {
        return new StallholderFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return createStallholderFragment(getArguments().getString("stallholderName"), inflater, container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(StallholderViewModel.class);
        // TODO: Use the ViewModel
    }

    private View createStallholderFragment(final String stallholderName, LayoutInflater layoutInflater, ViewGroup viewGroup){
        /* Retrieve view */
        View view = layoutInflater.inflate(R.layout.stallholder_fragment, viewGroup, false);
        final View finalView = view;

        /* Retrieve stallholder information from firebase */
        /* Get instance of firebase */
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Listen for return of firestore contents */
        firebaseFirestore.collection("events").document(getArguments().getString("eventName"))
                .collection("stallholders").document(stallholderName).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        /* Get documents contents */
                        DocumentSnapshot document = task.getResult();

                        Stallholder tempStallholder = new Stallholder();
                        tempStallholder.companyName = document.get("companyName").toString();
                        tempStallholder.imageUrl = document.get("imageUrl").toString();
                        tempStallholder.stallholderInformation = document.get("stallholderInformation").toString();
                        tempStallholder.stallholderWebsiteUrl = document.get("stallholderWebsiteUrl").toString();

                        stallholder = tempStallholder;

                        /* Retrieve the stallholder image */
                        final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReferenceFromUrl(stallholder.imageUrl);

                        final long ONE_MEGABYTE = 1024 * 1024;

                        firebaseStorage.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                /* Decode bytes into a bitmap image */
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                stallholder.stallholderImage = bitmap;

                                /* Setup stallholder page */
                                TextView stallholderNameTextView = finalView.findViewById(R.id.stallholder_fragment_name);
                                stallholderNameTextView.setText(stallholder.companyName);

                                ImageView stallholderImageView = finalView.findViewById(R.id.stallholder_fragment_image);
                                stallholderImageView.setImageBitmap(stallholder.stallholderImage);
                                stallholderImageView.setContentDescription("Picture of the " + stallholder.companyName + " stallholder");

                                TextView stallholderInformationTextView = finalView.findViewById(R.id.stallholder_fragment_information);
                                stallholderInformationTextView.setText(stallholder.stallholderInformation);

                                Button stallholderWebsiteButton = finalView.findViewById(R.id.stallholder_fragment_website_button);
                                stallholderWebsiteButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        switch(v.getId()){
                                            case R.id.stallholder_fragment_website_button:
                                                Uri uri = Uri.parse(stallholder.stallholderWebsiteUrl);
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                                                startActivity(browserIntent);
                                                break;
                                        }
                                    }
                                });
                            }
                        });
                    }
                });

        return view;
    }
}
