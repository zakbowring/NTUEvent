package com.example.ntuevent;

import android.app.Activity;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UserTest {
    @Test
    public void userConstructorTest(){
        Activity tempActivity = new Activity();

        User tempUser = new User(tempActivity);

        /* Check the constructor correctly sets the activity */
        assertEquals(tempActivity, tempUser.activity);
    }

    @Test
    public void userVariableSetting(){
        User tempUser = new User(new Activity());

        String testEmail = "test@email.com";
        String testPassword = "testPassword";
        String testUsername = "testUsername";
        String testProfilePictureUrl = "testProfilePictureUrl";
        List<String> testLinkedFiles = new ArrayList<>();
        testLinkedFiles.add("testFile1");
        testLinkedFiles.add("testFile2");

        tempUser.email = testEmail;
        tempUser.password = testPassword;
        tempUser.username = testUsername;
        tempUser.profilePictureURL = testProfilePictureUrl;
        tempUser.linkedFiles = testLinkedFiles;

        /* Assert all have been set and there's been no interruption */
        assertEquals(tempUser.email, testEmail);
        assertEquals(tempUser.password, testPassword);
        assertEquals(tempUser.username, testUsername);
        assertEquals(tempUser.profilePictureURL, testProfilePictureUrl);
        assertEquals(tempUser.linkedFiles, testLinkedFiles);
    }
}