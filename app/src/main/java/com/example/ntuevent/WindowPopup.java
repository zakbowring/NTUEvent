package com.example.ntuevent;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

public class WindowPopup {

    public View createQrScannerPopupView(Context context, int layout){
        /* Create popupview for QR Scanner popup */
        LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        /* Get view for popup */
        View popupView = layoutInflater.inflate(layout, null);

        popupView.setElevation(30);

        return popupView;
    }

    public PopupWindow createQrScannerPopupWindow(Context context, View popupView, PopupWindow popupWindow){
        /* Creates popup window */
        /* Get height and width */
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        /* Set popup window height and width */
        popupWindow = new PopupWindow(popupView, (int)(width*0.8), (int)(height*0.7), true);

        /* Set customisables */
        popupWindow.setFocusable(true);
        popupWindow.setTouchable(true);

        /* Show window */
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        return popupWindow;
    }
}
