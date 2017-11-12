package com.enavamaratha.enavamaratha.adapter;

/**
 * Created by ABmra on 07-06-2016.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
 
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.activity.Child;
import com.enavamaratha.enavamaratha.activity.Group;

public class ExpandableListAdapter extends BaseExpandableListAdapter
{

    private Context _context;

    private ArrayList<Group> ExpListItems;

    private ArrayList<Group> groups;




    public ExpandableListAdapter(Context context, ArrayList<Group> groups) {
        this._context = context;
        this.groups = groups;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<Child> chList = groups.get(groupPosition)
                .getItems();
        return chList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Child child = (Child) getChild(groupPosition,
                childPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater)_context
                    .getSystemService(_context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView tv1 = (TextView) convertView.findViewById(R.id.lblListItem1);


        tv.setText(child.getName().toString());
       tv1.setText(child.getNumber().toString());

        // tv.setText(child.getName().toString()+"::"+child.getTag());
        // tv.setTag(child.getTag());
        // TODO Auto-generated method stub
        return convertView;

    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<Child> chList = groups.get(groupPosition)
                .getItems();

        return chList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // TODO Auto-generated method stub
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        // TODO Auto-generated method stub
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        // TODO Auto-generated method stub
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Group group = (Group) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inf = (LayoutInflater)_context
                    .getSystemService(_context.LAYOUT_INFLATER_SERVICE);
            convertView = inf.inflate(R.layout.list_group, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.lblListHeader);
        tv.setText(group.getName());
        // TODO Auto-generated method stub
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // TODO Auto-generated method stub
        return true;
    }

}
