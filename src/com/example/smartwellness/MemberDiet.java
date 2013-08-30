package com.example.smartwellness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class MemberDiet extends Fragment {

	protected static final String ARG_ITEM_ID = "item_id";
	public ListView diet_archive, break_list, lunch_list, dinner_list;
	public TabHost tabs;
	public String sendItem;
	public String resAmount;
	public String meal;
	public ArrayList<DietList> break_tablist = new ArrayList<DietList>();
	public ArrayList<DietList> lunch_tablist = new ArrayList<DietList>();
	public ArrayList<DietList> dinner_tablist = new ArrayList<DietList>();
	
	public DietItemAdapter break_adapter;
	public DietItemAdapter lunch_adapter;
	public DietItemAdapter dinner_adapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getArguments().containsKey(ARG_ITEM_ID);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_member_diet, container, false);
		
		diet_archive = (ListView)rootView.findViewById(R.id.diet_archive);
		break_list = (ListView)rootView.findViewById(R.id.diet_frame_listview1);
		lunch_list = (ListView)rootView.findViewById(R.id.diet_frame_listview2);
		dinner_list = (ListView)rootView.findViewById(R.id.diet_frame_listview3);
		tabs = (TabHost)rootView.findViewById(android.R.id.tabhost);
		int currentItemNumber =0;
		for(int i =0; i<ToKeepVar.getInstance().getJson().size(); i++)
			if(ToKeepVar.getInstance().getJson().get(i).get("uid").equals(getArguments().getString(ARG_ITEM_ID)))
				currentItemNumber = i;
		
		((TextView)rootView.findViewById(R.id.diet_title)).setText(ToKeepVar.getInstance().getJson().get(currentItemNumber).get("name") + "의 식단");
		setArchiveList();
		setTab();
		Button back_btn = (Button)rootView.findViewById(R.id.diet_back_btn);
		back_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		
		Button submit_btn = (Button)rootView.findViewById(R.id.diet_submit_btn);
		submit_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
					(new DietInsertTask()).execute(); //input var of string type from listview
			}
		});
		return rootView;
	}
	
	public void setTab()
	{
		TabHost.TabSpec ts;
		tabs.setup();
		
		ts = tabs.newTabSpec("breakfast");
		ts.setContent(break_list.getId());
		ts.setIndicator("아침");
		tabs.addTab(ts);

		ts = tabs.newTabSpec("lunch");
		ts.setContent(lunch_list.getId());
		ts.setIndicator("점심");
		tabs.addTab(ts);
		
		ts = tabs.newTabSpec("dinner");
		ts.setContent(dinner_list.getId());
		ts.setIndicator("저녁");
		tabs.addTab(ts);
	}
	
	public void setTabList()
	{
		if(meal.equals("아침"))
		{
			break_tablist.add(new DietList(sendItem, resAmount));
			break_adapter = new DietItemAdapter(getActivity().getBaseContext(), R.layout.diet_listview_items, break_tablist);
			break_list.setAdapter(break_adapter);
		}
		else if(meal.equals("점심"))
		{
			lunch_tablist.add(new DietList(sendItem, resAmount));
			lunch_adapter = new DietItemAdapter(getActivity().getBaseContext(), R.layout.diet_listview_items, lunch_tablist);
			lunch_list.setAdapter(lunch_adapter);
		}
		else
		{
			dinner_tablist.add(new DietList(sendItem, resAmount));
			dinner_adapter = new DietItemAdapter(getActivity().getBaseContext(), R.layout.diet_listview_items, dinner_tablist);
			dinner_list.setAdapter(dinner_adapter);
		}
	}
	
	public void setArchiveList()
	{
		List<String> list = new ArrayList<String>();
		list.add("test");
		list.add("test2");
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity().getBaseContext(),android.R.layout.simple_list_item_1, list);
		diet_archive.setAdapter(adapter);
		
		diet_archive.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
				sendItem = diet_archive.getItemAtPosition(position).toString();
				DietDialog2 dialog = new DietDialog2(view.getContext());
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
				//dialog.setCancelable(false);
				dialog.show();
			}
		});
	}
	
	public class DietDialog2 extends Dialog {
		private EditText mAmount;
		private Button btn_ok;
		private Button btn_cancel;
		private Spinner spinner;
		public DietDialog2(Context context) {
			super(context);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_diet_dialog);
			
			((TextView)findViewById(R.id.diet_kind_textview)).setText(sendItem);
			mAmount = (EditText) findViewById(R.id.diet_edit_amount);
			btn_ok = (Button)findViewById(R.id.diet_ok_btn);
			btn_cancel = (Button)findViewById(R.id.diet_cancel_btn);
			spinner = (Spinner)findViewById(R.id.diet_spinner);
			spinner.setPrompt("끼니를 선택하세요");
			final ArrayAdapter<CharSequence> adspin = ArrayAdapter.createFromResource(this.getContext(), R.array.selected, android.R.layout.simple_spinner_dropdown_item);
			
	        adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adspin);
	        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
	            	meal = adspin.getItem(position).toString();
	            }
	            public void onNothingSelected(AdapterView<?>  parent) {
	            }
	        });

			btn_cancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {	
					dismiss();
				}
			});
			
			btn_ok.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					resAmount = mAmount.getText().toString();
					setTabList();
					dismiss();
				}
			});
		}
	}

	public class DietInsertTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			HashMap<String,String> missionData = new HashMap<String,String>();
			missionData.put("table", "diet");
			HashMap<String,String> dietData = new HashMap<String,String>();
			ListAdapter breakData = break_list.getAdapter();
			ListAdapter lunchData = lunch_list.getAdapter();
			ListAdapter dinnerData = dinner_list.getAdapter();
			
			if(breakData != null){
				String breakString = "";
				for(int i =0; i<breakData.getCount(); i++)
				{
					DietList inputdata1 = (DietList)breakData.getItem(i);
					breakString += inputdata1.getName() + " ";
					if(breakData.getCount() -1 == i)
						breakString += inputdata1.getMeasure();
					else
						breakString += inputdata1.getMeasure() + "/";
				}
				dietData.put("breakfast",breakString);
			}
			
			if(lunchData != null){
				String lunchString = "";
				for(int i =0; i<lunchData.getCount(); i++)
				{
					DietList inputdata2 = (DietList)lunchData.getItem(i);
					lunchString += inputdata2.getName() + " ";
					if(lunchData.getCount() -1 == i)
						lunchString += inputdata2.getMeasure();
					else
						lunchString += inputdata2.getMeasure() + "/";
				}
				dietData.put("lunch",lunchString);
			}
			
			if(dinnerData != null){
				String dinnerString = "";
				for(int i =0; i<dinnerData.getCount(); i++)
				{
					DietList inputdata3 = (DietList)dinnerData.getItem(i);
					dinnerString += inputdata3.getName() + " ";
					if(dinnerData.getCount() -1 == i)
						dinnerString += inputdata3.getMeasure();
					else
						dinnerString += inputdata3.getMeasure() + "/";
				}
				dietData.put("dinner",dinnerString);
			}
			
			if(!dietData.keySet().isEmpty()){
				Object[] keyset = dietData.keySet().toArray();
				String key = "", data = "";
				for(int i =0; i<keyset.length; i++){
					if(keyset.length -1 == i){
						key += keyset[i];
						data += ("\"" + dietData.get(keyset[i]) + "\"");
					}
					else{
						key += keyset[i] + ",";
						data += ("\"" + dietData.get(keyset[i]) + "\",");
					}
				}
				
				
				missionData.put("field", "member_uid, trainer_uid," + key + ", date");
				missionData.put("data", getArguments().getString(ARG_ITEM_ID) + "," + ToKeepVar.getInstance().getData() + "," + data + ",now()");
				
				JsonRequestPost missionInsert = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.INSERT);
				
				missionInsert.requestPost();
				
				return true;
			}
			else
				return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			if(!result.booleanValue())
				Toast.makeText(getActivity(), "데이터를 입력하세요", Toast.LENGTH_SHORT).show();
			else{
				Toast.makeText(getActivity(), "전송되었습니다", Toast.LENGTH_SHORT).show();
				if(break_adapter != null){
					break_adapter.clear();
					break_adapter.notifyDataSetChanged();
				}
				if(lunch_adapter != null){
					lunch_adapter.clear();
					lunch_adapter.notifyDataSetChanged();
				}
				if(dinner_adapter != null){
					dinner_adapter.clear();
					dinner_adapter.notifyDataSetChanged();
				}
			
			}
				
		}
	}
	
	private class DietList
	{
		private String mName;
		private String mMeasure;
		public DietList(String name, String measure)
		{
			mName = name;
			mMeasure = measure;
		}
		
		public String getName()
		{
			return mName;
		}
		
		public String getMeasure()
		{
			return mMeasure;
		}
	}
	
	private class DietItemAdapter extends ArrayAdapter<DietList>{
		private ArrayList<DietList> items;
		private int layout;
		
		public DietItemAdapter(Context context, int id, ArrayList<DietList> data){
			super(context, id, data);
			this.items = data;
			layout = id;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			View v = convertView;
			if(v == null){
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //error?
				v = li.inflate(layout, null);
			}
			DietList e = items.get(position);
			if(e != null){
				((TextView)v.findViewById(R.id.diet_listview_items_name)).setText("Kind: " + e.getName());
				((TextView)v.findViewById(R.id.diet_listview_items_measure)).setText("Number: " + String.valueOf(e.getMeasure()));
			}
			return v;
		}
	}
}