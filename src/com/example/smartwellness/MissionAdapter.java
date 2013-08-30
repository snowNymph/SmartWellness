package com.example.smartwellness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MissionAdapter extends BaseAdapter{

	List<HashMap<String, String>> Item = new ArrayList<HashMap<String,String>>();
	Context mContext;
	public MissionAdapter(Context context){
		mContext = context;
	}
	@Override
	public int getCount() {
		return Item.size();
	}

	public void addItem(HashMap<String, String> adItem){
		Item.add(adItem);
	}
	
	@Override
	public HashMap<String, String> getItem(int position) {
		return Item.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MissionAdapterView MAV;
		MAV = new MissionAdapterView(mContext, Item.get(position));
		return MAV;
	}
}
