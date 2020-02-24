package com.example.ntuevent.ui.events.Event.Stallholder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class StallholderViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public StallholderViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Stallholder Fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
