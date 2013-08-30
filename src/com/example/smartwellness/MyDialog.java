package com.example.smartwellness;

import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MyDialog extends Dialog{

	public MyDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_dialog_mission);
		
		Button btn_ok = (Button)findViewById(R.id.exercise_ok);
		btn_ok.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				(new MissionAddTask()).execute();
			}
		});
	}
	
	public class MissionAddTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			HashMap<String,String> missionData = new HashMap<String,String>();
			missionData.put("table", "mission_comms");
			missionData.put("field", "trainer_uid,name,phone,sex,age,date");
			missionData.put("data", ToKeepVar.getInstance().getData().toString() + ",\"" + params[0] 
					+ "\"," +params[1] + ",\"" + params[2] + "\"," + params[3] + ",now()");
			
			JsonRequestPost missionInsert = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.INSERT);
			
			missionInsert.requestPost();
			
			return null;
		}
		
		@Override
		protected void onPostExecute(final String input) {
		}
	}
}
