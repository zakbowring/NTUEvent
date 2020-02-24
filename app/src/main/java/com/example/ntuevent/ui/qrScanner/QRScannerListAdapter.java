package com.example.ntuevent.ui.qrScanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ntuevent.R;

import java.util.ArrayList;
import java.util.List;

public class QRScannerListAdapter extends BaseAdapter {
    Context context;

    /* Users file names */
    private final List<String> userFilenames;

    /* Keeps track of checked files on popup window */
    ArrayList<String> selectedFiles = new ArrayList<String>();

    public QRScannerListAdapter(Context context, List<String> userFilenames) {
        this.context = context;
        this.userFilenames = userFilenames;
    }

    @Override
    public int getCount() {
        return userFilenames.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.qrscanner_listview, parent, false);

            /* Sets checkboxes */
            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.fileCheckBox);
            checkBox.setText(userFilenames.get(position));

            final CheckBox finalCheckBox = checkBox;

            finalCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedFiles.add(finalCheckBox.getText().toString());
                    }else{
                        selectedFiles.remove(finalCheckBox.getText().toString());
                    }

                }
            });

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        return convertView;
    }

    private static class ViewHolder {

        TextView txtName;
        ImageView icon;

    }
}

