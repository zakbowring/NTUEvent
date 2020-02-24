package com.example.ntuevent.ui.account;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ntuevent.FirebaseStore;
import com.example.ntuevent.MainActivity;
import com.example.ntuevent.R;
import com.example.ntuevent.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private AccountViewModel accountViewModel;
    FirebaseAuth firebaseAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        accountViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        View root = inflater.inflate(R.layout.fragment_account, container, false);
        final TextView textView = root.findViewById(R.id.text_account);
        accountViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) { textView.setText(s);
            }
        });

        /* On click listener for add account image */
        root.findViewById(R.id.add_profile_picture_button).setOnClickListener(this);
        root.findViewById(R.id.account_save_button).setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        return root;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_profile_picture_button:
                MainActivity.activeUser.openGallery();
                break;
            case R.id.account_save_button:
                saveAccount();
                //uploadUserImage(MainActivity.activeUser);

                break;
        }
    }

    private void saveAccount(){
        /* Upload the user image */
        uploadUserImage();

        /* Upload the new username */
        uploadNewUserInfo();

        /* new retrieval on active user */
        FirebaseStore.retrieveActiveUser(getContext());
    }

    /* Saves user profile picture to firebase storage */
    // Move to firebase store long term
    private void uploadUserImage(){
        /* If user is uploading a new PP */
        if((MainActivity.activeUser.uriProfilePicture != null) && (MainActivity.activeUser.newProfilePictureUploaded)) {
            /* PP folder location */

            final String pictureUrl = "profilePictures/" + MainActivity.activeUser.email + ".jpg";
            final StorageReference profileReference = FirebaseStorage.getInstance().getReference(pictureUrl);

            /* Send PP to firebase store */
            profileReference.putFile(MainActivity.activeUser.uriProfilePicture)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>(){
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(getActivity().getApplicationContext(), "Account profile picture successfully saved", Toast.LENGTH_SHORT).show();

                            uploadNewProfilePictureUrl(pictureUrl);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void uploadNewProfilePictureUrl(final String profilePictureUrl) {
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        DocumentReference contact = firebaseFirestore.collection("users").document(MainActivity.activeUser.email);
        contact.update("imageUrl", profilePictureUrl)
                .addOnSuccessListener(new OnSuccessListener < Void > () {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getContext(), "Profile picture Url sent",
                                Toast.LENGTH_SHORT).show();

                        /* Extract URL */
                        MainActivity.activeUser.profilePictureURL = profilePictureUrl;
                    }
                });

        MainActivity.activeUser.newProfilePictureUploaded = false;
    }

    private void uploadNewUserInfo(){
        /* Retrieve username from editText */
        EditText editTextUsername = (EditText) getView().findViewById(R.id.account_username);
        String username = editTextUsername.getText().toString().trim();

        /* Validation */
        if(username.isEmpty()){
            editTextUsername.setError("Name required");
            editTextUsername.requestFocus();
            return;
        }

        /* Validate there's an active user */
        if(MainActivity.activeUser != null){
            /* Get instance of the database */
            final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            DocumentReference contact = firebaseFirestore.collection("users").document(MainActivity.activeUser.email);
            contact.update("username", username)
                    .addOnSuccessListener(new OnSuccessListener < Void > () {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "Ting is working",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
