package com.example.smartwellness;

import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class MemberAdd extends Fragment {
	public EditText name;
	public EditText phone;
	public EditText age;
	public RadioGroup radio;
	public RadioButton radiobutton;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_member_add, container, false);
		
		Button add = (Button)rootView.findViewById(R.id.member_add_btn);		
		add.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				name = (EditText)getActivity().findViewById(R.id.add_edit_name);
				phone = (EditText)getActivity().findViewById(R.id.add_edit_phone);
				age = (EditText)getActivity().findViewById(R.id.add_edit_age);
				radio = (RadioGroup)getActivity().findViewById(R.id.add_radio_sex);
				radiobutton = (RadioButton)getActivity().findViewById(radio.getCheckedRadioButtonId());
				
				(new MemberAddTask()).execute(name.getText().toString(), phone.getText().toString() , age.getText().toString());
			}
		});
		return rootView;
	}
	
	public class MemberAddTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			if(!params[0].isEmpty() && !params[1].isEmpty() && !params[2].isEmpty() && !radiobutton.getText().toString().isEmpty()){
				HashMap<String,String> missionData = new HashMap<String,String>();
				missionData.put("table", "member");
				missionData.put("field", "trainer_uid,name,phone,sex,age,date");
				missionData.put("data", ToKeepVar.getInstance().getData().toString() + ",\"" + params[0] 
						+ "\"," +params[1] + ",\"" + radiobutton.getText().toString() + "\"," + params[2] + ",now()");
				
				JsonRequestPost missionInsert = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.INSERT);
				
				missionInsert.requestPost();
				
				return true;
			}
			else
				return false;
		}
		
		@Override
		protected void onPostExecute(final Boolean input) {
			if(!input.booleanValue())
				Toast.makeText(getActivity(), "데이터를 입력하세요", Toast.LENGTH_SHORT).show();
			else{
				Toast.makeText(getActivity(), "완료되었습니다", Toast.LENGTH_SHORT).show();
				name.setText("");
				phone.setText("");
				age.setText("");
				age.setText("");
				radio.clearCheck();
			}
		}
	}
}