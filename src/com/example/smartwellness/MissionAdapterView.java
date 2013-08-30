package com.example.smartwellness;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("ViewConstructor")
public class MissionAdapterView extends LinearLayout{
	TextView name,set,text,tools,number,point,type;
	Context mContext;
	public MissionAdapterView(Context context , HashMap<String,String> input) {
		super(context);
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.mission_add_view, this, true);	
		type = (TextView)findViewById(R.id.mission_type);
		type.setText(input.get("kind"));
		name = (TextView)findViewById(R.id.mission_name);
		name.setText(input.get("name"));
		number =(TextView)findViewById(R.id.mission_number);
		number.setText(input.get("number")+"»∏");
		set = (TextView)findViewById(R.id.mission_set);	
		set.setText(input.get("set")+"ºº∆Æ");	
		text = (TextView)findViewById(R.id.mission_text);
		text.setText(input.get("desc"));		
		tools = (TextView)findViewById(R.id.mission_tools);
		tools.setText(input.get("tool"));
	}
}
