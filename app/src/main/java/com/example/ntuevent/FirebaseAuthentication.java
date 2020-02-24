package com.example.ntuevent;

import android.content.Context;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.HashMap;
import java.util.Map;

public class FirebaseAuthentication {

    private Context mContext;
    private FirebaseAuth firebaseAuth;

    private onLoginEventListener mListener;

    public FirebaseAuthentication(Context context, onLoginEventListener loginEventListener) {
        this.mContext = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.mListener = loginEventListener;
    }

    /* Login listener */
    public void loginListener(onLoginEventListener eventListener){
        this.mListener = eventListener;
    }

    public void registerNewUser(PopupWindow popupWindow) {
        /* Registers the new user with Firebase */

        /* Retrieve the popup view */
        View registrationPopupView = popupWindow.getContentView();

        /* Retrieve contents of editTexts */
        EditText editTextEmail = ((EditText) registrationPopupView.findViewById(R.id.registrationEmailInput));
        EditText editTextPassword = ((EditText) registrationPopupView.findViewById(R.id.registrationPasswordInput));
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        /* Validate new email and password */
        if ((!validateNewUserEmail(editTextEmail, email)) || (!validateNewUserPassword(editTextPassword, password)))
            return;

        /* Authentication */
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    createNewUserFirestore(email, password);

                } else {
                    Toast.makeText(mContext.getApplicationContext(), "User registration unsuccessful", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public User loginUser(PopupWindow popupWindow) {
        final User user = null;
        View registrationPopupView = popupWindow.getContentView();

        /* Retrieve contents of editTexts */
        EditText editTextEmail = ((EditText) registrationPopupView.findViewById(R.id.signInEmailInput));
        EditText editTextPassword = ((EditText) registrationPopupView.findViewById(R.id.signInPasswordInput));
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        /* Validate new email and password */
        if ((!validateNewUserEmail(editTextEmail, email)) || (!validateNewUserPassword(editTextPassword, password)))
            return null;

        /* Authentication */
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>(){
           @Override
           public void onComplete(@NonNull Task<AuthResult> task){
               if(task.isSuccessful()){
                   Toast.makeText(mContext.getApplicationContext(), "User successfully logged in", Toast.LENGTH_SHORT).show();
                   mListener.onLoginEvent(email, password);
               }
               else {
                   Toast.makeText(mContext.getApplicationContext(), "User could not be logged in", Toast.LENGTH_SHORT).show();
               }
           }
        });

        return user;
    }

    private boolean validateNewUserEmail(EditText editTextEmail, String email) {
        /* Validates email meets Firebase requirements */

        /* Email must be present */
        if(email.isEmpty()) {
            editTextEmail.setError("Email required");
            editTextEmail.requestFocus();
            return false;
        }

        /* Email must have email format */
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email");
            editTextEmail.requestFocus();
            return false;
        }

        return true;
    }

    private boolean validateNewUserPassword(EditText editTextPassword, String password) {
        /* Validates password meets Firebase requirements */

        /* Password must be present */
        if(password.isEmpty()){
            editTextPassword.setError("Password required");
            editTextPassword.requestFocus();
            return false;
        }

        /* Password must be at least 6 long */
        if(password.length() < 6){
            editTextPassword.setError("Password must be at least 6 characters long");
            editTextPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void createNewUserFirestore(String email, String password){
        /* New user to upload */
        Map<String, Object> newUserData=  new HashMap<>();

        /* Add values */
        newUserData.put("email", email);
        newUserData.put("filesUrl", "");
        newUserData.put("imageUrl", "" );
        newUserData.put("password", password);
        newUserData.put("username", "");

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users").document(email).set(newUserData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mContext.getApplicationContext(), "User registration successful", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Could not create new user, error: " + e, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}