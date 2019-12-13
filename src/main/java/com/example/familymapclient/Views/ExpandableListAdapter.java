package com.example.familymapclient.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.familymapclient.R;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> data;
    private HashMap<String, List<String>> dataDetail;

    ExpandableListAdapter(Context context, List<String> data,
                                 HashMap<String, List<String>> dataDetail) {
        this.context = context;
        this.data = data;
        this.dataDetail = dataDetail;
    }
    @Override
    public int getGroupCount() {
        return this.data.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return this.dataDetail.get(this.data.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return this.data.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return this.dataDetail.get(this.data.get(i)).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String headerTitle = (String) getGroup(i);
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_group_header, null);
        }

        TextView header = view.findViewById(R.id.listGroupHeader);
        header.setTypeface(null, Typeface.BOLD);
        header.setText(headerTitle);

        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) { //NOT DONE
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.expandable_list_child, null);
        }
        String top = getChild(i, i1).toString();

        ImageView imageView = view.findViewById(R.id.expandable_list_imageView);

        if (getChild(i, i1).toString().contains("Spouse") ||getChild(i, i1).toString().contains("Child")) {
            imageView.setImageDrawable(imageView.getResources().getDrawable(R.drawable.ic_android_white));
        }
        else {
            imageView.setImageDrawable(imageView.getResources().getDrawable(R.drawable.ic_pin));
        }

        TextView topText = view.findViewById(R.id.expandable_top_textView);
        topText.setText(top);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }
}
