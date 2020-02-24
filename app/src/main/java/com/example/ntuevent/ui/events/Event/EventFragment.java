package com.example.ntuevent.ui.events.Event;

import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.ntuevent.Event;
import com.example.ntuevent.ListAdapter;
//import com.example.ntuevent.ListAdapterStallholders;
import com.example.ntuevent.R;
import com.example.ntuevent.Stallholder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class EventFragment extends Fragment implements View.OnClickListener {

    private EventViewModel mViewModel;
    private Event event;
    ListView listView;
    //ListAdapterStallholders listAdapterStallholders;

    public static EventFragment newInstance() {
        return new EventFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return createEventFragment(getArguments().getString("eventName"), inflater, container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(EventViewModel.class);
        // TODO: Use the ViewModel
    }

    @Override
    public void onResume() {
        super.onResume();

        /* Pause user interactivity till everything has loaded */
        /* Required as if the user goes back another page before everything has loaded then */
        /* that'll load on the incorrect page, breaking the application */
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public View createEventFragment(final String eventName, LayoutInflater layoutInflater, ViewGroup viewGroup) {
        /* Retrieve view */
        View view = layoutInflater.inflate(R.layout.event_fragment, viewGroup, false);
        final View finalView = view;

        /* Retrieve event information from firebase */

        /* Get instance of firebase */
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Listen for return of firestore contents */
        firebaseFirestore.collection("events").document(eventName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        /* Get documents contents */
                        DocumentSnapshot document = task.getResult();

                        /* Extarct contents */
                        final Event tempEvent = new Event();
                        tempEvent.name = document.get("name").toString();
                        tempEvent.imageUrl = document.get("imageUrl").toString();
                        tempEvent.date = (Date) document.get("date");
                        tempEvent.eventInformation = document.get("eventInformation").toString();
                        tempEvent.location = document.get("location").toString();
                        tempEvent.longitude = document.get("longitude").toString();
                        tempEvent.latitude = document.get("latitude").toString();

                        event = tempEvent;

                        /* Get the stallholders of the event */
                        firebaseFirestore.collection("events").document(eventName).collection("stallholders")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        for(QueryDocumentSnapshot document : task.getResult()){
                                            /* Get stallholder data */
                                            Stallholder tempStallholder = new Stallholder();
                                            tempStallholder.companyName = document.get("companyName").toString();
                                            tempStallholder.filesUrl = document.get("filesUrl").toString();
                                            tempStallholder.imageUrl = document.get("imageUrl").toString();

                                            event.stallholders.add(tempStallholder);
                                        }

                                        /* Retrieve the event Image */
                                        final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReferenceFromUrl(event.imageUrl);

                                        final long ONE_MEGABYTE = 1024 * 1024;

                                        /* Listener for getting image bytes */
                                        firebaseStorage.getBytes(ONE_MEGABYTE)
                                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                    @Override
                                                    public void onSuccess(byte[] bytes) {
                                                        /* Decode bytes into a bitmap image */
                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                                        event.eventImage = bitmap;

                                                        /* Create Event Page */
                                                        TextView titleTextView = finalView.findViewById(R.id.event_title);
                                                        titleTextView.setText(event.name);

                                                        ImageView imageView = finalView.findViewById(R.id.event_image);
                                                        imageView.setImageBitmap(event.eventImage);

                                                        TextView eventInformationTextView = finalView.findViewById(R.id.event_information);
                                                        eventInformationTextView.setText(event.eventInformation);

                                                        TextView timeTextView = finalView.findViewById(R.id.event_time);
                                                        timeTextView.setText(new SimpleDateFormat("HH:mm").format(event.date));

                                                        TextView dateTextView = finalView.findViewById(R.id.event_date);
                                                        dateTextView.setText(new SimpleDateFormat("dd MMM yyyy").format(event.date));

                                                        TextView locationTextView = finalView.findViewById(R.id.event_location);
                                                        locationTextView.setText(event.location);

                                                        finalView.findViewById(R.id.add_to_calendar_button).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                switch (v.getId()) {
                                                                    case R.id.add_to_calendar_button:
                                                                        setCalendar();
                                                                }
                                                            }
                                                        });

                                                        finalView.findViewById(R.id.find_on_map_button).setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                /* Create bundle to hold longitude and latitude */
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("longitude", tempEvent.longitude);
                                                                bundle.putString("latitude", tempEvent.latitude);
                                                                bundle.putString("eventName", event.name);
                                                                bundle.putString("location", event.location);

                                                                /* Go to navigation fragment */
                                                                Navigation.findNavController(finalView).navigate(R.id.nav_navigation, bundle);
                                                            }
                                                        });

                                                        /* For each event */
                                                        for (int i = 0; i < event.stallholders.size(); i++) {
                                                            /* Get storage reference of event image */
                                                            final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReferenceFromUrl(event.stallholders.get(i).imageUrl);

                                                            final long ONE_MEGABYTE = 1024 * 1024;

                                                            /* Listener for getting image bytes */
                                                            firebaseStorage.getBytes(ONE_MEGABYTE)
                                                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                                        @Override
                                                                        public void onSuccess(byte[] bytes) {
                                                                            /* Decode bytes into a bitmap image */
                                                                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                                                            /* Get the download Url for this listener */
                                                                            String downloadUrl = firebaseStorage.toString();

                                                                            /* Loop looking for the correct event for eventImage */
                                                                            for (int i = 0; i < event.stallholders.size(); i++) {
                                                                                if (downloadUrl.equals(event.stallholders.get(i).imageUrl)) {
                                                                                    event.stallholders.get(i).stallholderImage = bitmap;
                                                                                    break;
                                                                                }
                                                                            }

                                                                            /* Check if all event images are loaded */
                                                                            boolean allEventImagesLoaded = true;
                                                                            for (int i = 0; i < event.stallholders.size(); i++) {
                                                                                if (event.stallholders.get(i).stallholderImage == null)
                                                                                    allEventImagesLoaded = false;
                                                                            }

                                                                            /* If all event images are loaded */
                                                                            if (allEventImagesLoaded) {
                                                                                /* Create a list of companies */
                                                                                /* Get linear layout to place in */
                                                                                LinearLayout stallholderLinearLayout = (LinearLayout) finalView.findViewById(R.id.stallholder__linear_list);

                                                                                /* For each company */
                                                                                for(int i = 0; i < event.stallholders.size(); i++) {
                                                                                    /* Create linear layout to contain */
                                                                                    LinearLayout tempLinearLayout = new LinearLayout(getContext());
                                                                                    tempLinearLayout.setOrientation(LinearLayout.VERTICAL);
                                                                                    int paddingPixels = 10;
                                                                                    int paddingPixelsDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, paddingPixels, getResources().getDisplayMetrics());
                                                                                    tempLinearLayout.setPadding(paddingPixelsDp,20,paddingPixelsDp, 20 );

                                                                                    /* Add the image */
                                                                                    ImageView tempImageView = new ImageView(getContext());
                                                                                    int dimensionInPixel = 200;
                                                                                    int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInPixel, getResources().getDisplayMetrics());
                                                                                    DisplayMetrics displayMetrics = new DisplayMetrics();
                                                                                   ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                                                                                    int pixelsSpaceImageView = 20;
                                                                                    int paddingPixelsSpaceIMageViewDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixelsSpaceImageView, getResources().getDisplayMetrics());
                                                                                    LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(displayMetrics.widthPixels - paddingPixelsSpaceIMageViewDp, dimensionInDp);
                                                                                    tempImageView.setLayoutParams(parms);
                                                                                    tempImageView.setImageBitmap(event.stallholders.get(i).stallholderImage);
                                                                                    tempImageView.setBackgroundResource(R.drawable.event_image_border);
                                                                                    tempLinearLayout.addView(tempImageView);

                                                                                    /* Add the text */
                                                                                    TextView tempTextView = new TextView(getContext());
                                                                                    tempTextView.setText(event.stallholders.get(i).companyName);
                                                                                    tempTextView.setTextSize(20);
                                                                                    tempTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                                                                    tempTextView.setBackgroundResource(R.drawable.textview_border);
                                                                                    tempTextView.setId(R.id.stallholder_text);

                                                                                    tempTextView.setTextColor(Color.parseColor("#000000"));
                                                                                    tempLinearLayout.addView(tempTextView);

                                                                                    tempLinearLayout.setOnClickListener(new View.OnClickListener() {
                                                                                        @Override
                                                                                        public void onClick(View v) {
                                                                                            /* Add stallholder to bundle to dynamically load on next page */
                                                                                            Bundle bundle = new Bundle();

                                                                                            /* Find which stallholder was selected */
                                                                                            TextView tempTextView = v.findViewById(R.id.stallholder_text);

                                                                                            /* Add to bundle with a key */
                                                                                            bundle.putString("stallholderName", tempTextView.getText().toString());
                                                                                            bundle.putString("eventName", event.name);

                                                                                            /* Go to clicked stallholder */
                                                                                            Navigation.findNavController(finalView).navigate(R.id.fragment_stallholder, bundle);
                                                                                        }
                                                                                    });

                                                                                    stallholderLinearLayout.addView(tempLinearLayout);
                                                                                }
                                                                            }

                                                                            /* Re-enables user interactivity */
                                                                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });

                    }
                });

        return view;
    }

    @Override
    public void onClick(View v) {
    }

    private void setCalendar(){
        /* Allows user to set an event in calendar */

        /* Create intent for setting calendar event */
        Intent calendarIntent = new Intent(Intent.ACTION_INSERT);

        /* Set the intents type */
        calendarIntent.setType("vnd.android.cursor.item/event");

        /* Set all the details for the event */
        calendarIntent.putExtra(CalendarContract.Events.TITLE, event.name); /* Name of event */
        calendarIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, event.location); /* Location of event */
        calendarIntent.putExtra(CalendarContract.Events.ALL_DAY, true); /* Sets event to all day */

        /* Begin Time */
        int tempYear = Integer.parseInt(new SimpleDateFormat("yyyy").format(event.date));
        int tempMonth = Integer.parseInt(new SimpleDateFormat("MM").format(event.date));
        int tempDay = Integer.parseInt(new SimpleDateFormat("dd").format(event.date));
        int tempHour = Integer.parseInt(new SimpleDateFormat("HH").format(event.date));
        int tempMinute = Integer.parseInt(new SimpleDateFormat("mm").format(event.date));
        Calendar tempBeginDate = Calendar.getInstance();
        tempBeginDate.set(tempYear, tempMonth, tempDay, tempHour, tempMinute);
        calendarIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, tempBeginDate.getTimeInMillis());

        /* Start calendar */
        startActivity(calendarIntent);
    }
}