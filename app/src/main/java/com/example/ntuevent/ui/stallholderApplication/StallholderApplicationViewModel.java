package com.example.ntuevent.ui.stallholderApplication;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StallholderApplicationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StallholderApplicationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the Stallholder fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}

