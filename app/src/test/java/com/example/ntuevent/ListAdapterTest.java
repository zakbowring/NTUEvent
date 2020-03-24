package com.example.ntuevent;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ListAdapterTest {
    @Test
    public void listAdapterEventTextCreationTest(){
        Event tempEvent = new Event();
        tempEvent.name = "ThisIsALongTestName";

        String date = "21-03-2020";
        SimpleDateFormat tempSimpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");

        try {
            tempEvent.date = tempSimpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        /* Can be null as they're not needed for this */
        ListAdapter tempListAdapter = new ListAdapter(null, null);

        /* Assert string has been cut correctly */
       assertEquals(tempListAdapter.createEventText(tempEvent), "21 Mar 2020 - ThisIsALongTestN...");
    }

    @Test
    public void listAdapterconstructorTest(){
        List<Event> tempListEvents = new ArrayList<>();

        for(int i =0; i < 5; i++){
            Event tempEvent = new Event();
            tempEvent.name = "TempEvent" + i;
            tempListEvents.add(tempEvent);
        }

        ListAdapter tempListAdapter = new ListAdapter(null, tempListEvents);

        assertEquals(tempListAdapter.context, null);
        assertEquals(tempListAdapter.events, tempListEvents);
    }
}