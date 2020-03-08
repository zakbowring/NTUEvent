package com.example.ntuevent.ui.account.EditAccountFragment;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ntuevent.MainActivity;
import com.example.ntuevent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1beta1.WriteResult;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditAccountFragment extends Fragment implements View.OnClickListener {

    private EditAccountViewModel mViewModel;
    private List<Uri> fileUrisToAdd = new ArrayList<>();
    private List<String> fileNamesToAdd = new ArrayList<>();

    public static EditAccountFragment newInstance() {
        return new EditAccountFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.edit_account_fragment, container, false);

        return inflater.inflate(R.layout.edit_account_fragment, container, false);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        /* Set username */
        EditText editTextUsername = (EditText) getView().findViewById(R.id.edit_account_username);
        editTextUsername.setText(MainActivity.activeUser.username);

        /* Set password */
        EditText editTextPassword = (EditText) getView().findViewById(R.id.edit_account_password);
        editTextPassword.setText(MainActivity.activeUser.password);

        /* Loads files to be added/removed */
        loadUploadedFiles(getView());

        /* Set email */
        TextView textViewEmail = (TextView) getView().findViewById(R.id.edit_account_page_email);
        textViewEmail.setText(MainActivity.activeUser.email);

        /* Update account profile picture */
        ImageView imageViewProfilePicture = (ImageView) getView().findViewById(R.id.edit_account_profile_picture);
        imageViewProfilePicture.setImageBitmap(MainActivity.activeUser.profilePicture);

        /* Set listeners */
        getView().findViewById(R.id.add_file_button).setOnClickListener(this);
        getView().findViewById(R.id.save_account_details_button).setOnClickListener(this);
        getView().findViewById(R.id.select_new_profile_picture_button).setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(EditAccountViewModel.class);
        // TODO: Use the ViewModel
    }

    private void loadUploadedFiles(View view){
        /* Get the linear layout to put them in */
        LinearLayout tempLinearLayout = (LinearLayout) view.findViewById(R.id.edit_account_linked_files_layout);

        /* Create line by line (file by file) */
        for(int i = 0; i < 5; i++){
            CheckBox tempCheckBox = new CheckBox(getContext());
            tempCheckBox.setText("Testing");
            tempCheckBox.setTextSize(18);
            tempLinearLayout.addView(tempCheckBox);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.add_file_button:
                addFileToAccount();
                break;
            case R.id.save_account_details_button:
                saveAccountDetails();
                break;
            case R.id.select_new_profile_picture_button:
                break;
        }
    }

    private void addFileToAccount(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Open Folder"), 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            /* Request code of file manager */
            case 1:
                String uri =  data.getData().getPath();

                /* If there's a file to be added, as user can go back without selecting a file */
                if(uri != "") {
                    /* Get the absolute path from the uri */
                    /* Get the file */
                    File loadedFile = new File(data.getData().toString());

                    /* Get absolute path */
                    String absolutePath = loadedFile.getAbsolutePath();

                    String fileName = "";
                    /* Two methods to get files depending on type */
                    if(data.getData().toString().startsWith("file://"))
                        fileName = loadedFile.getName();
                    else if(data.getData().toString().startsWith("content://")){
                        Cursor cursor = null;
                        cursor = getActivity().getContentResolver().query(data.getData(), null, null, null, null);

                        /* Check cursor can be moved to find file name */
                        if(cursor != null && cursor.moveToFirst()){
                            /* Get the file name */
                            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        }
                    }

                    /* Store on lists to be added to the database */
                    fileUrisToAdd.add(data.getData());
                    fileNamesToAdd.add(fileName);

                    /* Add new file to checkbox list */
                    LinearLayout tempLinearLayout = (LinearLayout) getView().findViewById(R.id.edit_account_linked_files_layout);
                    addNameToCheckBoxList(tempLinearLayout, fileName);
                }

                break;
        }
    }

    private void addNameToCheckBoxList(LinearLayout linearLayout, String name){
        CheckBox tempCheckBox = new CheckBox(getContext());
        tempCheckBox.setText(name);
        tempCheckBox.setTextSize(18);
        linearLayout.addView(tempCheckBox);
    }

    private void saveAccountDetails(){
        /* Function uploads all new account details and returns to previous page */

        /* Update username if changed*/
        EditText editTextUsername = (EditText) getView().findViewById(R.id.edit_account_username);

        if(editTextUsername.getText().toString() != MainActivity.activeUser.username)
            updateUsername(editTextUsername.getText().toString());

        /* Update password if changed */
        EditText editTextPassword = (EditText) getView().findViewById((R.id.edit_account_password));

        if(editTextPassword.getText().toString() != MainActivity.activeUser.password)
            updatePassword(editTextPassword.getText().toString());

        /* Check if there's files to be added */
        if(fileUrisToAdd.size() != 0 && fileNamesToAdd.size() != 0)
            addNewFilesToDatabase();

        //Add remove files
        //Return to previous page
    }

    private void updateUsername(final String editTextUsername){
        /* Get Firebase Instance */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Get doc reference to uodate */
        DocumentReference documentReference = firebaseFirestore.collection("users").document(MainActivity.activeUser.email);

        /* Update */
        documentReference.update("username", editTextUsername).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        /* Update Main Activities username once complete */
                        MainActivity.activeUser.username = editTextUsername;
                    }
                });
    }

    private void updatePassword(final String editTextPassword){
        /* Get Firebase Instance */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Get doc reference to uodate */
        DocumentReference documentReference = firebaseFirestore.collection("users").document(MainActivity.activeUser.email);

        /* Update */
        documentReference.update("password", editTextPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        });

        /* Update password in authenticator */
        /* Get Firebase User */
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        /* Update users password */
        currentUser.updatePassword(editTextPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                /* Update Main Activities username once complete */
                MainActivity.activeUser.password = editTextPassword;
            }
        });
    }

    private void addNewFilesToDatabase(){
        /* Function adds new files to database */
        /* For each file to upload */
        for(int i = 0; i < fileUrisToAdd.size(); i ++){
            /* Get storage reference from uri */
            StorageReference storageReference = FirebaseStorage.getInstance().getReference("userFiles/" + MainActivity.activeUser.email + "/" + fileNamesToAdd.get(i));

            /* String of filename for later use */
            final String finalFileName = fileNamesToAdd.get(i);

            /* Upload to storage */
            storageReference.putFile(fileUrisToAdd.get(i)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    /* Once file has been added it needs adding to list in database */
                    /* Get firebase path */
                    String storagePath = taskSnapshot.getMetadata().getPath();

                    /* Add Path to the database */
                    /* Get Firebase Instance */
                    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

                    /* Create map of new data */
                    Map<String, Object> contents = new HashMap<>();
                    contents.put("name", finalFileName);
                    contents.put("fileUrl", storagePath);

                    firebaseFirestore.collection("users").document(MainActivity.activeUser.email).collection("files")
                            .document(finalFileName).set(contents).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            /* Add new file to active user */
                            MainActivity.activeUser.linkedFiles.add(finalFileName);
                        }
                    });
                }
            });
        }
    }
}
