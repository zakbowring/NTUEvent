package com.example.ntuevent.ui.qrScanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class QRScannerViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public QRScannerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("QR Scanner");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
