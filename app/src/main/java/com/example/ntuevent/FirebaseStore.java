package com.example.ntuevent;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class FirebaseStore {

    public static void retrieveActiveUser(final Context context){
        /* Get instance of the database */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Return all users */
        firebaseFirestore.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            /* For each user */
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                /* Get user data */
                                Map<String, Object> userData = document.getData();

                                /* If the correct user */
                                // Update not to pull every user back
                                if (MainActivity.activeUser.email.toLowerCase().equals(userData.get("email").toString().toLowerCase())) {
                                    /* Retrieve active users information */

                                    MainActivity.activeUser.email = document.get("email").toString();
                                    MainActivity.activeUser.username = document.get("username").toString();
                                    MainActivity.activeUser.password = document.get("password").toString();
                                    MainActivity.activeUser.profilePictureURL = document.get("imageUrl").toString();



                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(context.getApplicationContext(), "Firestore retireval failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
