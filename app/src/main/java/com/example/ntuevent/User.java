package com.example.ntuevent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class User extends AppCompatActivity {

    Activity activity;
    /* User variables */
    public String email;
    public String password;
    public String username;
    public Uri uriProfilePicture;
    public String profilePictureURL;
    public Bitmap profilePicture;

    /* Flags */
    public boolean newProfilePictureUploaded = false;

    /* Gallery global variables */
    private static final int CHOOSE_IMAGE = 101;

    public User(Activity activity){
        this.activity = activity;
    }

    /* Opens gallery to choose new PP */
    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, CHOOSE_IMAGE);
    }
}
