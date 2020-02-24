package com.example.ntuevent.ui.contactUs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.ntuevent.R;

public class ContactUsFragment extends Fragment implements View.OnClickListener {

    private ContactUsViewModel contactUsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        contactUsViewModel =
                ViewModelProviders.of(this).get(ContactUsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_contact_us, container, false);
        final TextView textView = root.findViewById(R.id.text_contact_us);
        contactUsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        /* Set submit onClickListener */
        root.findViewById(R.id.contact_us_submit_button).setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.contact_us_submit_button:
                sendContactUsForm();
        }
    }

    /* Validates and sends query */
    private void sendContactUsForm(){
        /* Retrieve name */
        EditText editTextName = (EditText) getView().findViewById(R.id.contact_us_name);
        String name = editTextName.getText().toString();

        /* Retrieve email */
        EditText editTextEmail = (EditText) getView().findViewById(R.id.contact_us_email);
        String email = editTextEmail.getText().toString();

        /* Retrieve query */
        EditText editTextQuery = (EditText) getView().findViewById(R.id.contact_us_query);
        String query = editTextQuery.getText().toString();

        /* Validate Query */
        if(validateContactUsForm(editTextName, editTextEmail, editTextQuery, name, email, query)){
            /* Querys will be sent to */
            String recipient = "ntueventcwk@gmail.com";

            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.setType("text/plain");

            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Query from: " + name);
            emailIntent.putExtra(Intent.EXTRA_TEXT, query);

            try {
                startActivity(Intent.createChooser(emailIntent, "Select email client..."));
            } catch (android.content.ActivityNotFoundException ex) {

            }
        }
    }

    /* Validates all fields in contact us form */
    private boolean validateContactUsForm(EditText editTextName, EditText editTextEmail, EditText editTextQuery, String name, String email, String query){
        /* Validate name */
        if(name.equals("")){
            editTextName.setError("Name is blank");
            editTextEmail.requestFocus();
            return false;
        }

        /* Validate email */
        if(email.equals("")) {
            editTextName.setError("Email is blank");
            editTextEmail.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Invalid email");
            editTextEmail.requestFocus();
            return false;
        }

        /* Validate Query */
        if(query.equals("")){
            editTextName.setError("Query is blank");
            editTextEmail.requestFocus();
            return false;
        }

        return true;
    }
}