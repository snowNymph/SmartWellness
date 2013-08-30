package com.example.smartwellness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemberMission extends Fragment {
	public ListView history_listview;
	public MissionItemAdapter mAdapter;
	public ArrayList<MissionList> list;
	public ListView listview;
	public MissionAdapter left_Adapter;
	protected static final String ARG_ITEM_ID = "item_id";
	Calendar send_date;
	Context mContext;
	DatePickerDialog.OnDateSetListener DateDialogListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			send_date = Calendar.getInstance();
			send_date.set(year, monthOfYear, dayOfMonth);			
			(new MissionInsertTask()).execute(); //input var of string type from listview
		}
	}; 
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getArguments().containsKey(ARG_ITEM_ID);

		mContext = getActivity();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_mission, container, false);
		(new MissionSearchTask()).execute();
		
		Button back_btn = (Button)rootView.findViewById(R.id.mission_back_btn);
		back_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});
		
		Button submit_btn = (Button)rootView.findViewById(R.id.mission_submit_btn);
		submit_btn.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				Calendar c = Calendar.getInstance();
				(new DatePickerDialog(mContext, DateDialogListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))).show();
			}
		});
		
		Button add_btn = (Button)rootView.findViewById(R.id.mission_add_btn);
		add_btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),MissionAdd.class);
				startActivityForResult(intent,1);
			}
		});
		listview = (ListView)rootView.findViewById(R.id.mission_listView);
		left_Adapter = new MissionAdapter(rootView.getContext());
		listview.setAdapter(left_Adapter);
		TextView tv = (TextView)rootView.findViewById(R.id.mission_title);
		String member_uid = getArguments().getString(ARG_ITEM_ID);
		
		for(HashMap<String,String> i : ToKeepVar.getInstance().getJson()){
			if( i.get("uid").compareTo(member_uid) == 0){
				tv.setText(i.get("name"));
			}
		}		
		return rootView;
	}
	
	class MissionList
	{
		private String mKind,mTool,mDesc,mName;
		private int mNum;
		private int mSet;
		
		public MissionList(String kind, int num, int set,String Tool,String Desc, String name){
			mKind = kind;
			mNum = num;
			mSet = set;
			mTool = Tool;
			mDesc = Desc;
			mName = name;
		}
		public String getKind(){return mKind;}
		public int getNum(){return mNum;}
		public int getSet(){return mSet;}
		public String getTool(){return mTool;}
		public String getDesc(){return mDesc;}
		public String getName(){return mName;}
	}

	public class MissionInsertTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {

			
			for(int i=0;i<left_Adapter.getCount();i++){
				HashMap<String,String> ab = new HashMap<String,String>();
				ab = left_Adapter.getItem(i);
				String data = new String();
				
				
				String member_uid = getArguments().getString(ARG_ITEM_ID);
				String trainer_uid = ToKeepVar.getInstance().getData();
				
				data = String.valueOf(trainer_uid)+",";
				data += String.valueOf(member_uid)+",";
				data += "\""+ab.get("kind")+"\",";
				data += "\""+ab.get("tool")+"\",";
				data += ab.get("number")+",";
				data += ab.get("set")+",";
				data += "\""+ab.get("desc")+"\",";
				data += "\""+String.valueOf(send_date.get(Calendar.YEAR))+"-"+String.valueOf(send_date.get(Calendar.MONTH)+1)+"-"+String.valueOf(send_date.get(Calendar.DAY_OF_MONTH))+"\",";
				data += "\""+ab.get("name")+"\"";
				
				HashMap<String,String> missionData = new HashMap<String,String>();
				missionData.put("table", "mission_comms");
				missionData.put("field", "train_uid,member_uid,exercise_kind,exercise_tool,exercise_number,exercise_set,exercise_des,date,exercise_name");
				missionData.put("data", data);
				
				JsonRequestPost missionInsert = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.INSERT);
				missionInsert.requestPost();
				
			}
			
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(!result.booleanValue()){
				Toast.makeText(getActivity(), "입력되었습니다", Toast.LENGTH_SHORT).show();
				left_Adapter = new MissionAdapter(mContext);
				left_Adapter.notifyDataSetChanged();
			}
		}
	}
	
	public class MissionSearchTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			
			HashMap<String,String> missionData = new HashMap<String,String>();
			missionData.put("table", "missionArchive");
			missionData.put("field", "exercise_kind, exercise_number, exercise_set, exercise_tool, exercise_desc, exercise_name");
			missionData.put("condition", "trainer_uid=" + ToKeepVar.getInstance().getData());
			
			JsonRequestPost missionSearch = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.SELECT);
			missionSearch.requestPost();
			
			return missionSearch.getStr();
		}
		
		@Override
		protected void onPostExecute(final String input) {
			super.onPostExecute(input);
			if(input != null && input.compareTo("null") != 0){
				List<HashMap<String,String>> outputData = JsonRequestPost.jsonParse(Arrays.asList("exercise_kind", "exercise_number", "exercise_set","exercise_tool","exercise_desc","exercise_name"), input);
				list = new ArrayList<MissionList>();
				for(int i =0; i<outputData.size(); i++)
				{
					String ww = outputData.get(i).get("exercise_kind");
					int ee = Integer.parseInt(outputData.get(i).get("exercise_set"));
					int rr = Integer.parseInt(outputData.get(i).get("exercise_number"));
					String qq = outputData.get(i).get("exercise_tool");
					String pp = outputData.get(i).get("exercise_desc");
					String tt = outputData.get(i).get("exercise_name");
					MissionList eachItem = new MissionList(ww, ee, rr, qq, pp, tt);
					list.add(eachItem);
				}
				mAdapter = new MissionItemAdapter(getActivity(),R.layout.mission_history_listview_items, list);
				ListView history_view = (ListView)getActivity().findViewById(R.id.mission_history_listview);
				history_view.setAdapter(mAdapter);
			}
		}
	}
	
	
	private class MissionItemAdapter extends ArrayAdapter<MissionList>{
		private ArrayList<MissionList> items;
		private int layout;
		
		public MissionItemAdapter(Context context, int id, ArrayList<MissionList> data){
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
			MissionList e = items.get(position);
			if(e != null){
				((TextView)v.findViewById(R.id.mission_history_textlist_items1)).setText("Kind: " + e.getKind());
				((TextView)v.findViewById(R.id.mission_history_textlist_items2)).setText("Number: " + String.valueOf(e.getNum()));
				((TextView)v.findViewById(R.id.mission_history_textlist_items3)).setText("Set: " + String.valueOf(e.getSet()));
			}
			v.findViewById(R.id.mission_history_button1).setOnClickListener(new OnClickListener(){
				public void onClick(View vt) {
					int x = Integer.parseInt(vt.getTag().toString());  
					Intent intent = new Intent(getActivity(),MissionSend.class);
					//positionø° «ÿ¥Á«œ¥¬ øÓµø ¡æ∏Ò, º≥∏Ì, »∏∂˚ º¬∆Æ,
					intent.putExtra("kind", list.get(x).mKind);
					intent.putExtra("tool", list.get(x).mTool);
					intent.putExtra("number", list.get(x).mNum);
					intent.putExtra("set", list.get(x).mSet);
					intent.putExtra("desc", list.get(x).mDesc);
					intent.putExtra("name", list.get(x).mName);
					startActivityForResult(intent,0);
				}
			});
			v.findViewById(R.id.mission_history_button1).setTag(position);
			return v;
		}
	}
	public void onActivityResult(int requestCode,int resultCode,Intent Data){
		if(requestCode == 0 &&resultCode == Activity.RESULT_OK){
			String desc,tool,kind,name;
			int number,set;
			name = Data.getStringExtra("name");
			kind = Data.getStringExtra("kind");
			tool = Data.getStringExtra("tool");
			desc = Data.getStringExtra("desc");
			number = Data.getIntExtra("number", 0);
			set = Data.getIntExtra("set", 0);
			HashMap<String,String> plus = new HashMap<String,String>();
			plus.put("name", name);
			plus.put("kind", kind);
			plus.put("tool", tool);
			plus.put("desc", desc);
			plus.put("number",String.valueOf(number));
			plus.put("set", String.valueOf(set));
			left_Adapter.addItem(plus);
			left_Adapter.notifyDataSetChanged();
		}
		else if(requestCode == 1){
			// resultCode -> ¿˙¿Â(-1), µÓ∑œ(1), √Îº“(0)
			if(resultCode == -1){
				String desc,tool,kind,name;
				int number,set;
				name = Data.getStringExtra("name");
				kind = Data.getStringExtra("kind");
				tool = Data.getStringExtra("tool");
				desc = Data.getStringExtra("desc");
				number = Data.getIntExtra("number", 0);
				set = Data.getIntExtra("set", 0);
				HashMap<String,String> plus = new HashMap<String,String>();
				plus.put("name", name);
				plus.put("kind", kind);
				plus.put("tool", tool);
				plus.put("desc", desc);
				plus.put("number",String.valueOf(number));
				plus.put("set", String.valueOf(set));
				left_Adapter.addItem(plus);
				left_Adapter.notifyDataSetChanged();
			}
			else if(resultCode == 1){
				//Archive ¿˙¿Â.
				//ToKeepVar.getInstance().getData() : trainer_uid
				(new MissionSearchTask()).execute();
			}
		}
	}
}
