package com.example.smartwellness;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemberAdvice extends Fragment {

	protected static final String ARG_ITEM_ID = "item_id";
	public EditText item1;
	public EditText item2;
	public EditText comment_item;
	public ListView listview;
	public List<HashMap<String,String>> numData;
	public HashMap<String,String> qwer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getArguments().containsKey(ARG_ITEM_ID);
		numData = new ArrayList<HashMap<String,String>>();
		(new AdviceSearchNum()).execute();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_member_advice, container, false);
		qwer = new HashMap<String,String>();
		item1 = (EditText)rootView.findViewById(R.id.advice_edit_weight);
		item2 = (EditText)rootView.findViewById(R.id.advice_edit_pressure);
		comment_item = (EditText)rootView.findViewById(R.id.advice_edit_comment);
		listview = (ListView)rootView.findViewById(R.id.advice_numpicker_listview);
		int currentItemNumber =0;
		for(int i =0; i<ToKeepVar.getInstance().getJson().size(); i++)
			if(ToKeepVar.getInstance().getJson().get(i).get("uid").equals(getArguments().getString(ARG_ITEM_ID)))
				currentItemNumber = i;
		
		((TextView)rootView.findViewById(R.id.advice_title_text)).setText(ToKeepVar.getInstance().getJson().get(currentItemNumber).get("name") + "의 상담기록");
		Button back_btn = (Button)rootView.findViewById(R.id.advice_back_btn);
		back_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getFragmentManager().popBackStack();
			}
		});

		Button vital_submit = (Button)rootView.findViewById(R.id.advice_vital_submit);
		vital_submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				(new AdviceVitalInsert()).execute();
			}
		});
		
		Button comment_submit = (Button)rootView.findViewById(R.id.advice_comment_submit);
		comment_submit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				(new AdviceCommentInsert()).execute();
			}
		});
		
		Button num_btn = (Button)rootView.findViewById(R.id.advice_num_btn);
		num_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				(new AdviceNumInsert()).execute();
			}
		});
		return rootView;
	}
	
	public class AdviceVitalInsert extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			if(!item1.getText().toString().isEmpty() && !item2.getText().toString().isEmpty()){
				HashMap<String,String> vitalData = new HashMap<String,String>();
				vitalData.put("table", "vital_info");
				vitalData.put("field", "member_uid, weight, pressure, date");
				vitalData.put("data", getArguments().getString(ARG_ITEM_ID) + "," + item1.getText().toString() + "," + item2.getText().toString() + ",now()");
				
				JsonRequestPost vitalInsert = new JsonRequestPost(vitalData, ConstantVar.URL + ConstantVar.INSERT);
				
				vitalInsert.requestPost();
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
				Toast.makeText(getActivity(), "데이터를 입력하세요", Toast.LENGTH_SHORT).show();
				item1.setText("");
				item2.setText("");
			}
				
		}
	}
	
	public class AdviceCommentInsert extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			if(!comment_item.getText().toString().isEmpty()){
				HashMap<String,String> commentData = new HashMap<String,String>();
				commentData.put("table", "comment");
				commentData.put("field", "trainer_uid, member_uid, contents, date");
				commentData.put("data", ToKeepVar.getInstance().getData() + "," + getArguments().getString(ARG_ITEM_ID)
						+ ",\"" + comment_item.getText().toString() + "\",now()");
				
				JsonRequestPost commentInsert = new JsonRequestPost(commentData, ConstantVar.URL + ConstantVar.INSERT);
				
				commentInsert.requestPost();
				return true;
			}
			else
				return false;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if(!result.booleanValue())
				Toast.makeText(getActivity(), "코멘트를 입력하세요", Toast.LENGTH_SHORT).show();
			else{
				Toast.makeText(getActivity(), "입력되었습니다", Toast.LENGTH_SHORT).show();
				comment_item.setText("");
			}
		}
	}

	public void setList()
	{
		ArrayList<AdviceList> list = new ArrayList<AdviceList>();
		list.add(new AdviceList("back"));
		list.add(new AdviceList("chest"));
		list.add(new AdviceList("arm"));
		list.add(new AdviceList("stomach"));
		list.add(new AdviceList("leg"));
		AdviceItemAdapter adapter = new AdviceItemAdapter(getActivity(), R.layout.advice_listview_items, list);
		listview.setAdapter(adapter);
		qwer = (HashMap<String, String>) numData.get(0).clone();
	}
	
	public class AdviceList
	{
		private String mName;
		public AdviceList(String name)
		{
			mName = name;
		}
		
		public String getName()
		{
			return mName;
		}
		
		public String getNum()
		{
			if(mName.equals("back"))
				return numData.get(0).get("back");
			else if(mName.equals("chest"))
				return numData.get(0).get("chest");
			else if(mName.equals("arm"))
				return numData.get(0).get("arm");
			else if(mName.equals("stomach"))
				return numData.get(0).get("stomach");
			else
				return numData.get(0).get("leg");
		}
	}
	
	private class AdviceItemAdapter extends ArrayAdapter<AdviceList>{
		private ArrayList<AdviceList> items;
		private int layout;
		
		public AdviceItemAdapter(Context context, int id, ArrayList<AdviceList> data){
			super(context, id, data);
			this.items = data;
			layout = id;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent){
			View v = convertView;
			if(v == null){
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE); //error?
				v = li.inflate(layout, null);
			}
			AdviceList e = items.get(position);
			if(e != null){
				final TextView edit_listview = ((TextView)v.findViewById(R.id.advice_listview_edit));
				final TextView text_listview = ((TextView)v.findViewById(R.id.advice_listview_text));
				if(edit_listview.getText() == "")
					edit_listview.setText(e.getNum());
				text_listview.setText(e.getName());
				((Button)v.findViewById(R.id.advice_listview_plus)).setOnClickListener(new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						edit_listview.setText(String.valueOf((Integer.parseInt(edit_listview.getText().toString())+1)));
						qwer.remove(text_listview.getText().toString());
						qwer.put(text_listview.getText().toString(), edit_listview.getText().toString());
					}
				});
				((Button)v.findViewById(R.id.advice_listview_minus)).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						edit_listview.setText(String.valueOf((Integer.parseInt(edit_listview.getText().toString())-1)));
						qwer.remove(text_listview.getText().toString());
						qwer.put(text_listview.getText().toString(), edit_listview.getText().toString());
					}
				});
			}
			return v;
		}
	}
	
	public class AdviceSearchNum extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			HashMap<String,String> exerciseData = new HashMap<String,String>();
			exerciseData.put("table", "exercise_info");
			exerciseData.put("field", "back, chest, arm, stomach, leg");
			exerciseData.put("condition", "member_uid=" + getArguments().getString(ARG_ITEM_ID));
			
			JsonRequestPost exerciseSearch = new JsonRequestPost(exerciseData, ConstantVar.URL + ConstantVar.SELECT);
			
			exerciseSearch.requestPost();

			return exerciseSearch.getStr();
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result.equals("null"))
			{
				HashMap<String,String> zeroData = new HashMap<String,String>();
				zeroData.put("back", "0");
				zeroData.put("chest", "0");
				zeroData.put("arm", "0");
				zeroData.put("stomach", "0");
				zeroData.put("leg", "0");
				numData.add(zeroData);
			}
			else
			{
				numData = JsonRequestPost.jsonParse(Arrays.asList("back", "chest", "arm", "stomach", "leg"), result);
			}
			setList();
		}
	}
	
	public class AdviceNumInsert extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			HashMap<String,String> numberData = new HashMap<String,String>();
			numberData.put("table", "exercise_info");
			numberData.put("field", "member_uid, back, chest, arm, stomach, leg, date");
			numberData.put("data", getArguments().getString(ARG_ITEM_ID) + "," + qwer.get("back") +"," + qwer.get("chest") + "," + qwer.get("arm") + "," + qwer.get("stomach")
					+ "," + qwer.get("leg") + ",now()");
			numberData.put("condition", "member_uid=" + getArguments().getString(ARG_ITEM_ID));
			numberData.put("overlap", "1");
			JsonRequestPost insertNum = new JsonRequestPost(numberData, ConstantVar.URL + ConstantVar.UPDATE);
			insertNum.requestPost();

			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Toast.makeText(getActivity(), "입력되었습니다", Toast.LENGTH_SHORT).show();
		}
	}

}