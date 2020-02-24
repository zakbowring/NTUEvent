package com.example.ntuevent;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Event {
    public String name;
    public String imageUrl;
    public Bitmap eventImage;
    public Date date;
    public List<Stallholder> stallholders;
    public String eventInformation;
    public String location;
    public String longitude;
    public String latitude;

    public Event(){
        name = null;
        imageUrl = null;
        eventImage = null;
        stallholders = new ArrayList<>();
        eventInformation = null;
        location = null;
        longitude = null;
        latitude = null;
    }
}
