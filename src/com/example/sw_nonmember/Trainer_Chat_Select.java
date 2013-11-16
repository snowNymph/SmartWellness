package com.example.sw_nonmember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.smartwellness.MemberMission;
import com.example.smartwellness.R;
import com.example.smartwellness.ToKeepVar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Trainer_Chat_Select extends Fragment implements ReadData{
	
	String member_uid = "1";
	String[] list;
	String trainer_uid;
	room_adapter adapter = new room_adapter();
	List<String> Room = new ArrayList<String>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.chat_select, container, false);
		// input();
		ListView Lv = (ListView)rootView.findViewById(R.id.Chat_select);
		Lv.setAdapter(adapter);
		Lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				trainer_uid = list[arg2];
				roomlist_re_arrange(trainer_uid);
			}
		});
		
		HashMap<String,String> a = new HashMap<String,String>();
		List<String> Wantgap = new ArrayList<String>();
		a.put("table", "trainer");
		a.put("field", "chat_p");
		a.put("condition","uid=" + String.valueOf(ToKeepVar.getInstance().getTrainerUID()));
		Wantgap.add("chat_p");
		AsyncUseJson_nonmember ab = new AsyncUseJson_nonmember(Trainer_Chat_Select.this,ReadData.Select,Wantgap,-1);
		ab.execute(a);
		
		return rootView;
	}
	
	public void roomlist_re_arrange(String select){
		String add = select;
		for(int i=0;i<list.length;i++){
			if(list[i].compareTo(select) != 0){
				add = add + "-" + list[i];
			}
		}
		HashMap<String,String> a = new HashMap<String,String>();
		List<String> Wantgap = new ArrayList<String>();
		a.put("table", "member");
		a.put("field", "chat_p");
		a.put("data", "\""+add+"\"");
		a.put("condition","uid="+member_uid);
		a.put("overlap", "0");
		AsyncUseJson_nonmember ab = new AsyncUseJson_nonmember(Trainer_Chat_Select.this,ReadData.Update,Wantgap,-2);
		ab.execute(a);
	}
	
	@Override
	public void setData(List<HashMap<String, String>> HashList,int requestCode) {
		// requestCode == -2 �϶��� roomlist_re_arrange();
		if(requestCode == -1){
			if(HashList.get(0).get("chat_p") == null){
				Toast.makeText(getActivity().getBaseContext(), "�����Ͻ� ���� �����ϴ�.", Toast.LENGTH_SHORT).show();
			}
			else{
				list = HashList.get(0).get("chat_p").toString().split("-");
				(new send()).execute(0);
			}
		}
		else if(requestCode == -2){
			Bundle arguments = new Bundle();
			arguments.putString("member_uid", member_uid);
			arguments.putString("trainer_uid", String.valueOf(ToKeepVar.getInstance().getTrainerUID()));
			arguments.putString("Type", "Trainer");
			Chatting_UT fragment = new Chatting_UT();
			fragment.setArguments(arguments);
			getFragmentManager().beginTransaction().replace(R.id.member_detail_container, fragment).addToBackStack(null).commit();
		}
		else {
			Room.add(HashList.get(0).get("phone")+"��ȸ������ ��ȭ");
			(new send()).execute(requestCode+1);
		}
	}
	
	public class send extends AsyncTask<Integer,Void,Void> {
		@Override
		protected Void doInBackground(Integer... params) {
			if(params[0] < list.length){		
				HashMap<String,String> a = new HashMap<String,String>();
				List<String> Wantgap = new ArrayList<String>();
				a.put("table", "member");
				a.put("field", "phone");
				a.put("condition","uid="+list[params[0]]);
				Wantgap.add("phone");
				AsyncUseJson_nonmember ab = new AsyncUseJson_nonmember(Trainer_Chat_Select.this,ReadData.Select,Wantgap,params[0]);
				ab.execute(a);
			}
			else{
				setList();
			}
			return null;
		}
	}
	
	public void setList(){
		getActivity().runOnUiThread(new Runnable() {
	        public void run() {
	             adapter.notifyDataSetChanged();
	        }
		});
	}
	
	public class room_adapter extends BaseAdapter{
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.list_select, parent, false);
			TextView tv = (TextView)view.findViewById(R.id.list_room_name);
			tv.setText(Room.get(position));
			return view;
		}
		
		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public Object getItem(int position) {
			return Room.get(position);
		}
		
		@Override
		public int getCount() {
			return Room.size();
		}
	}
	
	@Override
	public void NullData(int requestCode){
		
	}
}