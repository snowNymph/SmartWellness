package com.example.smartwellness;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MissionAdd extends Activity{
	protected static final String InsertAsync = null;
	Spinner Kind,Tool;
	EditText Name,Number,Set,Desc;
	
	String name,kind,tool,desc;
	int number,set;
	Button Add,Cancel,Save;

	List<String> lkind;
	@SuppressWarnings("rawtypes")
	ArrayAdapter kadapter;
	@SuppressWarnings("rawtypes")
	ArrayAdapter tadapter;
	List<String> ltool;
	Intent intent;
	int work = 0; // ¡æ∑·¡ﬂ ? 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mission_new_view);
		Kind = (Spinner)findViewById(R.id.mission_new_kind);
		Tool = (Spinner)findViewById(R.id.mission_new_tool);
		Name = (EditText)findViewById(R.id.mission_new_name);
		Number = (EditText)findViewById(R.id.mission_new_number);
		Set = (EditText)findViewById(R.id.mission_new_set);
		Desc = (EditText)findViewById(R.id.mission_new_desc);
		Add = (Button)findViewById(R.id.mission_new_add);
		Cancel = (Button)findViewById(R.id.mission_new_cancel);
		Save = (Button)findViewById(R.id.mission_new_save);
		SpinnerSet();
		Add.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(work == 0){
					intent = new Intent();
					if(setReturn()){
						setResult(-1,intent);
						finish();
					}
				}
			}
		});
		Cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(work == 0){
					setResult(0);
					finish();
				}
			}
		});
		Save.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(work == 0){
					intent = new Intent();
					if(setReturn()){
						setResult(1,intent);
						work = 1;
						InsertAsync a = new InsertAsync();
						a.execute("");
					}
				}
			}
		});
	}
	public class InsertAsync extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			String data = new String();
			data = ToKeepVar.getInstance().getData() + ",";
			data += "\""+ kind + "\",";
			data += "\""+ tool + "\",";
			data += String.valueOf(number)+",";
			data += String.valueOf(set)+",";
			data += "\""+ desc + "\",";
			data += "\""+ name + "\"";
			HashMap<String,String> missionData = new HashMap<String,String>();
			missionData.put("table", "missionArchive");
			missionData.put("field", "trainer_uid,exercise_kind,exercise_tool,exercise_number,exercise_set,exercise_desc,exercise_name");
			missionData.put("data", data);
			
			JsonRequestPost missionInsert = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.INSERT);
			missionInsert.requestPost();
			return null;
		}
		protected void onPostExecute(final String input) {
			finish();
		}
	}
	
	boolean setReturn(){
		name = Name.getText().toString();
		desc = Desc.getText().toString();
		
		if(Kind.getSelectedItemPosition() != 0 && 
				Tool.getSelectedItemPosition() != 0 && name != null && desc != null && Set.getText().toString() != null && Number.getText().toString() != null){
			
			kind = Kind.getSelectedItem().toString();
			tool = Tool.getSelectedItem().toString();
			name = Name.getText().toString();
			desc = Desc.getText().toString();
			set = Integer.parseInt(Set.getText().toString());
			number = Integer.parseInt(Number.getText().toString());

			intent.putExtra("kind", kind);
			intent.putExtra("tool", tool);
			intent.putExtra("name", name);
			intent.putExtra("desc", desc);
			intent.putExtra("set", set);
			intent.putExtra("number", number);
			
			return true;
		}
		else{
			Toast.makeText(this, "∏µŒ º≥¡§«ÿ¡÷ººø‰.", Toast.LENGTH_SHORT).show();
			return false;
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void SpinnerSet(){
		lkind = new ArrayList<String>();
		ltool = new ArrayList<String>();
		lkind.add("가슴");
		lkind.add("배");
		lkind.add("다리");
		lkind.add("등");
		lkind.add("팔");
		//lkind.add("");
		
		ltool.add("øÓµø±‚±∏");
		ltool.add("∫•ƒ°«¡∑πΩ∫");
		ltool.add("Ω∫πÃΩ∫ ∏”Ω≈");
		ltool.add("µˆΩ∫ ∏”Ω≈");
		ltool.add("Ω√∆ºµÂ ∑ŒøÏ ∏”Ω≈");
		kadapter = new ArrayAdapter(this,android.R.layout.simple_dropdown_item_1line,lkind);
		tadapter = new ArrayAdapter(this,android.R.layout.simple_dropdown_item_1line,ltool);
		Kind.setAdapter(kadapter);
		Tool.setAdapter(tadapter);
	}
}
