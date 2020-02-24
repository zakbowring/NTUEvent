package com.example.ntuevent.ui.qrScanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupWindow;
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
import com.example.ntuevent.WindowPopup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QRScannerFragment extends Fragment implements View.OnClickListener {
    /* QR codes taken in this fragment have the format: */
    /* Event:Event 1;Company:FakeCompany; */

    private QRScannerViewModel qrScannerViewModel;

    /* For popup when transferring files */
    private PopupWindow fileTransferPopup;
    private  View popupView;

    /* Stores the company file Url once QR scanned */
    String companyUrl = "";

    /* List view for displaying users files */
    ListView lView;
    QRScannerListAdapter qrScannerListAdapter;

    /* List of user files */
    List<Map<String, String>> userFiles = new ArrayList<>();

    /* List of user files */
    List<String> fileNames = new ArrayList<>();
    List<String> fileUrls = new ArrayList<>();

    /* Count to ensure all files are sent */
    int filesTransferred = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        qrScannerViewModel =
                ViewModelProviders.of(this).get(QRScannerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_qr_scanner, container, false);
        final TextView textView = root.findViewById(R.id.text_qr_scanner);
        qrScannerViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        /* Initialises main qr scanner button */
        root.findViewById(R.id.qr_scanner_button).setOnClickListener(this);

        return root;
    }

        @Override
        public void onClick (View v){
        switch (v.getId()) {
            case R.id.qr_scanner_button:
                /* Request camera access */
                reset();
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 0);
                launchQrScanner();
                break;
            case R.id.qrScannerCancelButton:
                /* Dismiss the popup */
                fileTransferPopup.dismiss();
                break;
            case R.id.qrScannerSendButton:
                getUserFileUrlsToSend();
                break;
        }
    }

    /* Launches the QR scanner */
    private void launchQrScanner() {
        if(MainActivity.activeUser != null) {
            /* Validate app has access to camera */
            if (validateCameraPermission()) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(getActivity());

                /* Customisation options */
                intentIntegrator.setPrompt("Scan a barcode");
                intentIntegrator.setBeepEnabled(true);
                intentIntegrator.setOrientationLocked(false);

                /* Start QR scanner */
                intentIntegrator.forSupportFragment(QRScannerFragment.this).initiateScan();
            }
        }else{
            Toast.makeText(getContext(), "You must be logged in to use this feature", Toast.LENGTH_SHORT).show();
        }
    }

    /* Validate app has camera permission */
    private boolean validateCameraPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext().getApplicationContext(), "Enable camera permissions to access this feature", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* When camera scans qr code */

        /* Extract result */
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            if (intentResult.getContents() == null)
                Toast.makeText(getContext().getApplicationContext(), "Scan was cancelled", Toast.LENGTH_SHORT).show();
            else{
                extractQrContents(intentResult);
            }
        }
    }

    /* Extracts contents of QR code */
    private void extractQrContents(IntentResult intentResult){
        /* Get the contents from the result */
        String contents = intentResult.getContents();

        /* Extract the event name */
        String eventSubstring = contents.substring(contents.indexOf("Event:") + 6, contents.length());
        String event = eventSubstring.substring(0, eventSubstring.indexOf(";"));

        /* Extract company name */
        String companyNameSubstring = contents.substring( contents.indexOf("Company:") + 8, contents.length());
        String companyName = companyNameSubstring.substring(0, companyNameSubstring.indexOf(";"));

        /* Retrieve url for files of company */
        getCompanyUrl(event, companyName);
    }

    private void getCompanyUrl(String event, final String companyName){
        /* Create firestore reference */
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Return all stallholders at said event */
        firebaseFirestore.collection("events").document(event).collection("stallholders").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            /* For each stallholder at event */
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                /* Get stallholder data */
                                Map<String, Object> eventData = document.getData();

                                /* If stallholder is the same as the one from the QR */
                                if(document.get("companyName").toString().equals(companyName)){
                                    /* Get the companies file directory */
                                    companyUrl = document.get("filesUrl").toString();

                                    /* Load the popup for transferring files */
                                    loadFileTransferScreen();
                                }
                            }
                        }
                        else{
                            Toast.makeText(getContext(), "Could not find event and/or company", Toast.LENGTH_SHORT);
                        }
                    }
                });
    }

    /* Loads popup window for transferring files */
    private void loadFileTransferScreen(){
        /* Create popupWindow */
        WindowPopup windowPopup = new WindowPopup();

        /* Create the popup windows view */
        popupView = windowPopup.createQrScannerPopupView(getContext(), R.layout.file_transfer_popup);

        /* Create popup window */
        fileTransferPopup = windowPopup.createQrScannerPopupWindow(getContext(), popupView, fileTransferPopup);

        /* Set button listeners */
        popupView.findViewById(R.id.qrScannerCancelButton).setOnClickListener(this);
        popupView.findViewById(R.id.qrScannerSendButton).setOnClickListener(this);

        /* Retrieves users files to populate window */
        retrieveUserFiles();
    }

    private void retrieveUserFiles(){
        /* User must be logged in */
        if(MainActivity.activeUser != null){
            /* Get firestore instance */
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            /* Return all users files */
            firebaseFirestore.collection("users").document(MainActivity.activeUser.email).collection("files")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                /* This will give all files attached to users account */

                                /* For each file attached to account */
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    /* Get file data */
                                    Map<String, Object> userData = document.getData();

                                    /* Extract name and url */
                                    fileNames.add(document.get("name").toString());
                                    fileUrls.add(document.get("fileUrl").toString());
                                }

                                /* Display all file names in list view on popup */
                                lView = (ListView) popupView.findViewById(R.id.qrScannerFilesListView);
                                qrScannerListAdapter = new QRScannerListAdapter(getContext(), fileNames);
                                lView.setAdapter(qrScannerListAdapter);
                            }else {
                                Toast.makeText(getContext(), "Failed to retrieve user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    /* Retrieves file Urls to send */
    private void getUserFileUrlsToSend(){
        /* Get selected checkbox file names */
        List<String> selectedFiles = qrScannerListAdapter.selectedFiles;

        /* Must be files to send */
        if(selectedFiles.size() != 0){
            /* File urls to be uploaded */
            List<String> tempFileUrls = new ArrayList<>();

            /* For each file selected in check boxes */
            for(int i=0; i<selectedFiles.size(); i++){
                /* For each file loaded from user profile */
                for(int j=0; j<fileNames.size(); j++){
                    /* if selected file matched current file from profile */
                    if(fileNames.get(j).equals(selectedFiles.get(i))){
                        /* Add fileUrl to list to be transferred */
                        tempFileUrls.add(fileUrls.get(j));
                    }
                }
            }

            /* Send files to stallholder */
            sendFilesToStallholder(tempFileUrls);
        }else{
            Toast.makeText(getContext(), "You must select files to send", Toast.LENGTH_SHORT).show();
        }
    }

    /* Send files to stallholder */
    private void sendFilesToStallholder(final List<String> fileUrls){
        /* For each file to be sent */
        for(int i=0; i<fileUrls.size(); i++) {
            /* Get firebase storage instance */
            final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

            /* Get a storage reference for the url location  */
            final StorageReference storageReference = firebaseStorage.getReferenceFromUrl(fileUrls.get(i));

            /* Create a temp local file location to download the file to */
            File localFile = null;
            try {
                localFile = File.createTempFile("files", ".docx");
            } catch (IOException e) {
                e.printStackTrace();
            }

            /* Get corresponding file names for urls */
            String fileName = "";
            for(int j = 0; j < fileUrls.size(); j++){
                if(fileUrls.get(j).equals(fileUrls.get(i))){
                    fileName = fileNames.get(j);
                }
            }

            /* Final variables to use in listeners */
            final File finalLocalFile = localFile;
            final String finalFileName = fileName;

            /* Retrieve file from firebase storage */
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    /* Get storage reference for company url */
                    final StorageReference putStorageReference = firebaseStorage.getReferenceFromUrl(companyUrl + MainActivity.activeUser.email + " - " + finalFileName);

                    /* Upload downloaded file to company */
                    putStorageReference.putFile(Uri.fromFile(finalLocalFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            /* Increase transferred tally */
                            filesTransferred++;

                            /* Check to see if all files transferred */
                            if(filesTransferred == fileNames.size()) {
                                /* Dismiss popup window */
                                fileTransferPopup.dismiss();
                                Toast.makeText(getContext(), "Files Successfully transferred", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getContext(), "Error: " + exception, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /* Resets all variables */
    private void reset() {
        filesTransferred = 0;
        fileNames = new ArrayList<>();
        fileUrls = new ArrayList<>();
        userFiles = new ArrayList<>();
        companyUrl = "";
    }
}