package com.example.ntuevent.ui.events;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.core.app.ComponentActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.example.ntuevent.Event;
import com.example.ntuevent.ListAdapter;
import com.example.ntuevent.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class EventsFragment extends Fragment {

    ListView lView;
    ListAdapter lAdapter;

    /* List of all event images */
    List<Event> events = new ArrayList<>();

    private EventsViewModel eventsViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        eventsViewModel =
                ViewModelProviders.of(this).get(EventsViewModel.class);
        View root = inflater.inflate(R.layout.fragment_events, container, false);
        final TextView textView = root.findViewById(R.id.text_events);
        eventsViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        displayEvents(root);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        /* Re-display listview */
        /* If not first loop */
        if (events.size() != 0) {
            List<Event> tempEvents = new ArrayList<>();
            tempEvents = events;

            /* Clear events */
            events.clear();

            /* Add events back */
            events.addAll(tempEvents);

            /* Notify there has been a change */
            lAdapter.notifyDataSetChanged();
            lView.invalidateViews();
        }
    }

    /* Display all events in list view */
    private void displayEvents(View root){
        /* Displaying the events in ListView */
        final View view = root;

        /* Get instance of firestore */
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        /* Listener for return of firestore contents */
        firebaseFirestore.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            /* For each event */
                            for(QueryDocumentSnapshot document : task.getResult()){
                                Map<String, Object> eventData = document.getData();

                                /* Temp for current event */
                                Event tempEvent = new Event();
                                tempEvent.name = document.get("name").toString();
                                tempEvent.imageUrl = document.get("imageUrl").toString();
                                tempEvent.date = (Date) document.get("date");

                                /* Add temp to list of events */
                                events.add(tempEvent);
                            }

                            /* For each event */
                            for(int i = 0; i < events.size(); i++) {
                                /* Get storage reference of event image */
                                final StorageReference firebaseStorage = FirebaseStorage.getInstance().getReferenceFromUrl(events.get(i).imageUrl);

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
                                                for(int i = 0; i < events.size(); i++)
                                                {
                                                    if(downloadUrl.equals(events.get(i).imageUrl))
                                                    {
                                                        events.get(i).eventImage = bitmap;
                                                        break;
                                                    }
                                                }

                                                /* Check if all event images are loaded */
                                                boolean allEventImagesLoaded = true;
                                                for(int i = 0; i <events.size(); i++){
                                                    if(events.get(i).eventImage == null)
                                                        allEventImagesLoaded = false;
                                                }

                                                /* If all event images are loaded */
                                                if(allEventImagesLoaded)
                                                {
                                                    /* List view */
                                                    lView = (ListView) view.findViewById(R.id.listview);
                                                    lAdapter = new ListAdapter(getContext(), events);
                                                    lView.setAdapter(lAdapter);

                                                    lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                                        @Override
                                                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                                            /* Add event clicked to bundle for sending to destination */
                                                            Bundle bundle = new Bundle();
                                                            bundle.putString("eventName", events.get(i).name);

                                                            /* Go to clicked event */
                                                            Navigation.findNavController(view).navigate(R.id.fragment_event, bundle);
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }
}