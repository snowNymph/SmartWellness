package com.example.smartwellness;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MissionSend extends Activity{
	String kind,tool,desc,name;
	int number,set;
	Button Add,Cancel;
	EditText Set,Number;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mission_send_view);
		Intent intent = this.getIntent();
		name = intent.getStringExtra("name");
		kind = intent.getStringExtra("kind");
		tool = intent.getStringExtra("tool");
		desc = intent.getStringExtra("desc");
		number = intent.getIntExtra("number", 0);
		set = intent.getIntExtra("set", 0);
		
		Set = (EditText)findViewById(R.id.mission_send_set);
		Number = (EditText)findViewById(R.id.mission_send_number);
		Set.setText(String.valueOf(set));
		Number.setText(String.valueOf(number));
		Add = (Button)findViewById(R.id.mission_send_add);
		Cancel = (Button)findViewById(R.id.mission_send_cancel);
		Add.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				number = Integer.parseInt(Number.getText().toString());
				set = Integer.parseInt(Set.getText().toString());
				Intent data = new Intent();
				data.putExtra("name", name);
				data.putExtra("kind", kind);
				data.putExtra("tool", tool);
				data.putExtra("desc", desc);
				data.putExtra("number", number);
				data.putExtra("set", set);
				setResult(Activity.RESULT_OK,data);
				finish();
			}
		});
		Cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
	}
}
