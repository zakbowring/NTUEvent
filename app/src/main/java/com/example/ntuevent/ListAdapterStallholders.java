//package com.example.ntuevent;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.BaseAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import java.text.SimpleDateFormat;
//import java.util.List;
//
//public class ListAdapterStallholders extends BaseAdapter {
//    Context context;
//    private final List<Stallholder> stallholders;
//
//    public ListAdapterStallholders(Context context, List<Stallholder> stallholderList) {
//        this.context = context;
//        this.stallholders = stallholderList;
//    }
//
//    @Override
//    public int getCount() {
//        return stallholders.size();
//    }
//
//    @Override
//    public Object getItem(int i) {
//        return i;
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder viewHolder;
//
//        final View result;
//
//        if (convertView == null) {
//
//            viewHolder = new ListAdapterStallholders.ViewHolder();
//            LayoutInflater inflater = LayoutInflater.from(context);
//            convertView = inflater.inflate(R.layout.listview_row_stallholders, parent, false);
//            viewHolder.txtName = (TextView) convertView.findViewById(R.id.stallholder_name);
//            viewHolder.icon = (ImageView) convertView.findViewById(R.id.stallholder_image);
//
//            result = convertView;
//
//            convertView.setTag(viewHolder);
//        } else {
//            viewHolder = (ListAdapterStallholders.ViewHolder) convertView.getTag();
//            result = convertView;
//        }
//
//        viewHolder.txtName.setText(stallholders.get(position).companyName);
//        viewHolder.icon.setImageBitmap(stallholders.get(position).stallholderImage);
//
//        return convertView;
//    }
//
//    private static class ViewHolder {
//
//        TextView txtName;
//        ImageView icon;
//
//    }
//}
