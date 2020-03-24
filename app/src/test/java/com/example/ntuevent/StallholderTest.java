package com.example.ntuevent;

import org.junit.Test;

import static org.junit.Assert.*;

public class StallholderTest {
    @Test
    public void stallholderConstructor(){
        Stallholder tempStallholder = new Stallholder();

        assertEquals(tempStallholder.companyName, null);
        assertEquals(tempStallholder.filesUrl, null);
        assertEquals(tempStallholder.imageUrl, null);
        assertEquals(tempStallholder.stallholderInformation, null);
        assertEquals(tempStallholder.stallholderWebsiteUrl, null);
        assertEquals(tempStallholder.stallholderImage, null);
    }
}