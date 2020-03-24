package com.example.ntuevent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, onLoginEventListener {

    private static final int CHOOSE_IMAGE = 101 ;
    private static final int FINE_LOCATION_REQUEST_CODE = 1;
    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private MenuItem menuItem;

    /* Account popup */
    private PopupWindow accountPopupWindow;
    private PopupWindow signInPopupWindow;
    public PopupWindow registrationPopupWindow;

    /* User account */
    public static User activeUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_events, R.id.nav_navigation, R.id.nav_contact_us,
                R.id.nav_registration, R.id.nav_qr_scanner,
                R.id.nav_account)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        mAuth = FirebaseAuth.getInstance();
        FirebaseAuthentication firebaseAuthLogin = new FirebaseAuthentication(this, this);

        /* No user when app first opened */
        activeUser = null;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.menuItem = item;

        if (item.getItemId() == R.id.account_login) {
            if (item.getTitle().equals("Logout")) {
                /* Log the user out */
                MainActivity.activeUser = null;
                updateLoginButton("Login");
                updateSideNavBarAccount("", "", "");
                toggleAccountFragment(false);
            } else {
                createAccountPopup();
            }
        }
        return false;
    }

    //Loads the account icon
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //Opens up side nav bar
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /* Button listener */
    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.registerButton:
                /* Sleep to allow button effect */
                try { Thread.sleep(200); }
                catch (InterruptedException ex) { android.util.Log.d("RegisterButtonSleepErr", ex.toString()); }
                accountPopupWindow.dismiss();
                registrationPopupWindow = createPopupWindow(registrationPopupWindow, (int)R.layout.registration_popup);
                break;
            case R.id.registerationRegisterButton:
                /* Register the new user */
                FirebaseAuthentication firebaseAuth = new FirebaseAuthentication(this, null);
                firebaseAuth.registerNewUser(registrationPopupWindow);
                registrationPopupWindow.dismiss();
                break;
            case R.id.accountSignInButton:
                try { Thread.sleep(200); }
                catch (InterruptedException ex) { android.util.Log.d("RegisterButtonSleepErr", ex.toString()); }
                accountPopupWindow.dismiss();
                signInPopupWindow = createPopupWindow(signInPopupWindow, (int)R.layout.signin_popup);
                break;
            case R.id.signInButton:
                FirebaseAuthentication firebaseAuth2 = new FirebaseAuthentication(this, this);
                activeUser = firebaseAuth2.loginUser(signInPopupWindow);
                break;
            case R.id.registrationCancelButton:
                registrationPopupWindow.dismiss();
                break;
            case R.id.signInCancelButton:
                signInPopupWindow.dismiss();
                break;
        }
    }

    public void createAccountPopup() {
        accountPopupWindow = createPopupWindow(accountPopupWindow,(int)R.layout.account_popup);
    }

    private PopupWindow createPopupWindow(PopupWindow window, int layout)
    {
        /* Creates popup window */
        LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        View popupView = layoutInflater.inflate(layout, null);

        /* Retrieve height and width */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        window = new PopupWindow(popupView, (int)(width*0.7), (int)(height*0.5), true);

        window.setTouchable(true);
        window.setFocusable(true);
        popupView.setElevation(30);

        /* Shows the popup window */
        window.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        /* Set up button listeners */
        /* For each child of the layout */
        for(int i = 0; i < ((ViewGroup) popupView).getChildCount(); i++){
            /* Retrieve child (layout) within layout */
            View viewGroupChild = ((ViewGroup) popupView).getChildAt(i);
            /* For each child of the layout within the layout */
            for(int j=0; j<((ViewGroup) viewGroupChild).getChildCount(); j++){
                /* Retrieve child within the layout */
                View viewGroupChildChild = ((ViewGroup) viewGroupChild).getChildAt(j);
                /* Looking for button instances */
                if(viewGroupChildChild instanceof Button){
                    /* Set onClick listeners for button */
                    popupView.findViewById(viewGroupChildChild.getId()).setOnClickListener(this);
                }
            }
        }

        return window;
    }

    /* Below is for handling the logging in of a user */

    /* Listens for sign in confirmation */
    @Override
    public void onLoginEvent(String email, String password){
        /* Execute post-login methods */
        updateUserOnLogin(email);
    }

    /* Executes post-login authentication methods */
    private void updateUserOnLogin(String email){
        /* Initialises new user */
        activeUser = new User(this);
        activeUser.email = email;

        /* Retrieve active user from firebase */
        retrieveActiveUser(this);
    }

    public void retrieveActiveUser(final Context context){
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

                                    activeUser.email = document.get("email").toString();
                                    activeUser.username = document.get("username").toString();
                                    activeUser.password = document.get("password").toString();
                                    activeUser.profilePictureURL = document.get("imageUrl").toString();

                                    /* Loads linked files information */
                                    loadLinkedFiles();

                                    toggleAccountFragment(true);
                                    updateSideNavBarAccount(activeUser.username, activeUser.email, activeUser.profilePictureURL);
                                    updateLoginButton("Logout");
                                    signInPopupWindow.dismiss();

                                    break;
                                }
                            }
                        } else {
                            Toast.makeText(context.getApplicationContext(), "Firestore retireval failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        firebaseFirestore.collection("users").document(MainActivity.activeUser.email.toLowerCase()).collection("files").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        activeUser.linkedFiles = new ArrayList<>();

                        /* For each file attached to the user */
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            /* Get file name */
                            activeUser.linkedFiles.add(document.get("name").toString());
                    }
                    }
                });
    }

    public void updateSideNavBarAccount(String username, String email, String profilePictureUrl) {
        /* Updates app when user signed in */

        /* Retrieve navigation view */
        NavigationView sideNavBarNavigationView = findViewById(R.id.nav_view);

        /* Retrieve the view */
        View sideNavBarView = sideNavBarNavigationView.getHeaderView(0);

        /* Retrieve textViews for toolbar account info */
        TextView textViewUsername = (TextView) sideNavBarView.findViewById(R.id.sideNavBarUserName);
        TextView textViewEmail = (TextView) sideNavBarView.findViewById(R.id.sideNavBarEmail);

        /* Update views account info */
        textViewUsername.setText(username);
        textViewEmail.setText(email);

        updateSideNavBarProfilePicture(profilePictureUrl, sideNavBarView);
    }

    /* Updates the profile picture in the side navigation bar */
     private void updateSideNavBarProfilePicture(String profilePictureUrl, final View sideNavBarView) {
         /* Makes sure url is not empty */
         if(profilePictureUrl != "") {
             /* Get storage reference of event image */
             final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference(profilePictureUrl);

             /* Max file size */
             final long TEN_MEGABYTE = 10240 * 10240;

             /* Listener for getting image bytes */
             firebaseStorage.getBytes(TEN_MEGABYTE)
                     .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                         @Override
                         public void onSuccess(byte[] bytes) {
                             /* Decode bytes into bitmap */
                             Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                             /* Locate side nav bar profile picture imageView */
                             ImageView sideNavBarProfilePicture = (ImageView) sideNavBarView.findViewById(R.id.sideNavBarProfilePicture);

                             /* Set new profile picture */
                             sideNavBarProfilePicture.setImageBitmap(bitmap);

                             /* Save bitmap to active user */
                             activeUser.profilePicture = bitmap;
                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             Toast.makeText(getApplicationContext(), "Failed to load profile picture, error: " + e, Toast.LENGTH_SHORT).show();
                         }
                     });
         }
         else
         {
             /* Locate side nav bar profile picture imageView */
             ImageView sideNavBarProfilePicture = (ImageView) sideNavBarView.findViewById(R.id.sideNavBarProfilePicture);

             sideNavBarProfilePicture.setImageBitmap(BitmapFactory.decodeResource(this.getResources(),
                     R.drawable.account_picture));

             /* Set new profile picture */
             sideNavBarProfilePicture.setBackgroundResource(R.drawable.account_picture);
         }
     }

    private void toggleAccountFragment(Boolean state){
        /* Enables account option in side nav bar */

        /* Retrieve nav bar view */
        NavigationView navigationView = findViewById(R.id.nav_view);

        /* Get menu items from side nav bar view */
        Menu navMenu = navigationView.getMenu();

        /* Get account menu item */
        MenuItem account_Item = navMenu.findItem(R.id.nav_account);

        /* Enable the option */
        account_Item.setEnabled(state);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if( data.getData() != null && data != null && resultCode == RESULT_OK && requestCode == CHOOSE_IMAGE){
            /* Returns image URI */
            activeUser.uriProfilePicture = data.getData();
            activeUser.newProfilePictureUploaded = true;
        }

        super.onActivityResult(requestCode,resultCode,data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case FINE_LOCATION_REQUEST_CODE:
                /* If it doesn't go through the results are empty */
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    /* Request has been successful */
                    Toast.makeText(this, "Fine Location permissions enabled. Feature acessible.", Toast.LENGTH_SHORT).show();
                }
                else{
                    /* Request has been unsuccessful */
                    Toast.makeText(this, "To access this feature you must enable fine location.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void updateLoginButton(String text){
        menuItem.setTitle(text);
    }

    private void loadLinkedFiles(){
         /* Get firestore instance */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users").document(MainActivity.activeUser.email).collection("files").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot document : task.getResult()){
                                MainActivity.activeUser.linkedFiles.add(document.get("name").toString());
                            }
                        }
                    }
                });
    }
}
