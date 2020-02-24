package com.example.ntuevent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    Context context;
    private final List<Event> events;

    public ListAdapter(Context context, List<Event> eventsList) {
        this.context = context;
        this.events = eventsList;
    }

    @Override
    public int getCount() {
        return events.size();
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
            convertView = inflater.inflate(R.layout.listview_row, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.eventName);
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.eventImage);

            result = convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        viewHolder.txtName.setText(createEventText(events.get(position)));
        viewHolder.icon.setImageBitmap(events.get(position).eventImage);

        return convertView;
    }

    private static class ViewHolder {

        TextView txtName;
        ImageView icon;

    }

    /* Creates text to go below image in ListView */
    private String createEventText(Event event){
        String eventDate = new SimpleDateFormat("dd MMM yyyy").format(event.date);

        String eventText = eventDate + " - " + event.name;

        int eventTextLength = eventText.length();

        if(eventTextLength > 31){
            eventText = eventText.substring(0, 30) + "...";
        }

        return eventText;
    }
}

