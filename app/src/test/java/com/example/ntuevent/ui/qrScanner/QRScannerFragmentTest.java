package com.example.ntuevent.ui.qrScanner;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class QRScannerFragmentTest {
    @Test
    public void resetQRVarsTest(){
        /* Tests all QR variables have been reset */
        QRScannerFragment tempQrScannerFragment = new QRScannerFragment();

        tempQrScannerFragment.filesTransferred = 5;
        tempQrScannerFragment.fileNames = new ArrayList<>();
        tempQrScannerFragment.fileNames.add("Test1");
        tempQrScannerFragment.fileNames.add("Test2");
        tempQrScannerFragment.fileUrls = new ArrayList<>();
        tempQrScannerFragment.fileUrls.add("Test1");
        tempQrScannerFragment.fileUrls.add("Test2");
        tempQrScannerFragment.companyUrl = "TestUrl";
        tempQrScannerFragment.userFiles = new ArrayList<>();

        tempQrScannerFragment.reset();

        assertEquals(tempQrScannerFragment.filesTransferred, 0);
        assertEquals(tempQrScannerFragment.fileNames, new ArrayList<>());
        assertEquals(tempQrScannerFragment.fileUrls, new ArrayList<>());
        assertEquals(tempQrScannerFragment.userFiles, new ArrayList<>());
        assertEquals(tempQrScannerFragment.companyUrl, "");
    }
}