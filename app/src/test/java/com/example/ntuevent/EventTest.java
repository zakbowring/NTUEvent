package com.example.ntuevent;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class EventTest {
    @Test
    public void eventConstructorTest(){
        Event tempEvent = new Event();

        assertEquals(tempEvent.name, null);
        assertEquals(tempEvent.imageUrl, null);
        assertEquals(tempEvent.eventImage, null);
        assertEquals(tempEvent.stallholders, new ArrayList<>());
        assertEquals(tempEvent.eventInformation, null);
        assertEquals(tempEvent.location, null);
        assertEquals(tempEvent.longitude, null);
        assertEquals(tempEvent.latitude, null);
    }
}