package com.example.sw_nonmember;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.example.smartwellness.R;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class Chatting_UT extends Fragment implements ReadData{
	List<Chatitem> chats = new ArrayList<Chatitem>();
	LA adapter = new LA();
	int check = 0;
	int type = 0; // User = 0 Trainer = 1;
	Button bt;
	EditText et;
	String member_uid;
	String trainer_uid;
	int latest_chatting_uid = 0;
	ListView lv;
	View mChatMainStatus;
	View mChatMainForm;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.chat_main, container, false);		
		mChatMainStatus = (View)rootView.findViewById(R.id.chat_main_status);
		mChatMainForm = (View)rootView.findViewById(R.id.chat_main_form);
		showProgress(true);

		//Setting.		
		member_uid = getArguments().getString("member_uid");
		trainer_uid = getArguments().getString("trainer_uid");
		String types = getArguments().getString("Type");
		if(types.compareTo("User") == 0){
			type = 0;
		}
		else{
			type = 1;
		}
		
		lv = (ListView)rootView.findViewById(R.id.Chat);
		lv.setDivider(null);
		lv.setAdapter(adapter);
		
		et = (EditText)rootView.findViewById(R.id.Chat_Text);
		et.setLines(1);
		bt = (Button)rootView.findViewById(R.id.Chat_B);
		bt.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				
				String text = et.getText().toString();
				et.setText("");
				et.setLines(1);
				
				// input;
				HashMap<String,String> a = new HashMap<String,String>();
				List<String> Wantgap = new ArrayList<String>();
				a.put("table", "chatting");
				a.put("field", "member_uid,trainer_uid,message,m_type");
				a.put("data",member_uid+","+trainer_uid+","+"\""+ text +"\","+ String.valueOf(type));
				AsyncUseJson_nonmember ab = new AsyncUseJson_nonmember(Chatting_UT.this,ReadData.Insert,Wantgap ,0, false);
				check = 1;
				ab.execute(a);	
			}
			
		});
		et.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER){
					et.setLines(et.getLineCount()+1);
				}
				return false;
			}
		});
		
		getItems gg = new getItems();
		gg.execute();


		return rootView;
	}
	class Chatitem{
		Calendar time;
		String talk;
		int type;
		String item_role;
	}
	public class LA extends BaseAdapter{
		@Override
		public int getCount() {
			return chats.size();
		}
		@Override
		public Chatitem getItem(int position) {
			return chats.get(position);
		}
		@Override
		public long getItemId(int position) {
			return position;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			if(type == 0){
				if(chats.get(position).type == 0){
					LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.list_chatting, parent, false);
					convertView = settingView(convertView,position);
				}
				else{
					LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.list_chatting2, parent, false);
					convertView = settingView(convertView,position);
				}
			}
			else{
				if(chats.get(position).type == 0){
					LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.list_chatting2, parent, false);
					convertView = settingView(convertView,position);
				}
				else{
					LayoutInflater inflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					convertView = inflater.inflate(R.layout.list_chatting, parent, false);
					convertView = settingView(convertView,position);
				}
			}
			return convertView;
		}
	}
	public View settingView(View view,int position){
		// View 모양 변경 
		if(chats.get(position).item_role.compareTo("Open") == 0){
			TextView Text = (TextView)view.findViewById(R.id.list_chat_text);
			Text.setBackgroundResource(R.drawable.mal_top);
		}
		else if(chats.get(position).item_role.compareTo("Close") == 0){
			TextView Text = (TextView)view.findViewById(R.id.list_chat_text);
			Text.setBackgroundResource(R.drawable.mal_bot);
		}
		else if(chats.get(position).item_role.compareTo("Main") == 0){
			float h_dp = 20;
			Resources r = getResources();
			float h_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, h_dp, r.getDisplayMetrics());
			
			LayoutParams params = view.getLayoutParams();
			params.height = (int) h_px;
			params.width = LayoutParams.MATCH_PARENT;
			view.setLayoutParams(params);
			
			TextView Text = (TextView)view.findViewById(R.id.list_chat_text);
			TextView Use = (TextView)view.findViewById(R.id.list_chat_use);
			Text.setBackgroundResource(R.drawable.mal_mid);
			Text.setText(chats.get(position).talk);
			//Use.setText(String.valueOf(chats.get(position).type));
		}
		return view;
	}
	class getItems extends AsyncTask<Void,Void,Void>{
		public Chatting_UT mTempContext;
		protected Void doInBackground(Void... params) {
			boolean alpha = true;
			mTempContext = Chatting_UT.this;
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(check == 0){
				HashMap<String,String> a = new HashMap<String,String>();
				List<String> Wantgap = new ArrayList<String>();
				a.put("table", "chatting");
				a.put("field", "uid,member_uid,trainer_uid,message,date,m_type");
				a.put("condition", "member_uid="+member_uid+"&& trainer_uid="+trainer_uid+"&& uid >" + String.valueOf(latest_chatting_uid));//+" && trainer_uid="+get_uid);
				
				Wantgap.add("uid");
				Wantgap.add("member_uid");
				Wantgap.add("trainer_uid");
				Wantgap.add("message");
				Wantgap.add("date");
				Wantgap.add("m_type");
				
				AsyncUseJson_nonmember ab = new AsyncUseJson_nonmember(mTempContext,ReadData.Select,Wantgap , 1, false);
				check = 1;
				ab.execute(a);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if(isAdded())
				showProgress(false);
		}
	}
	
	void check(){
		getActivity().runOnUiThread(new Runnable() {
	        public void run() {
	             adapter.notifyDataSetChanged();
	        }
	    });
		scrollMyListViewToBottom();
	}
	
	private void scrollMyListViewToBottom() {
	    lv.post(new Runnable() {
	        @Override
	        public void run() {
	        	lv.setSelection(adapter.getCount() - 1);
	        }
	    });
	}
	
	String[] print = new String[100];
	@Override
	public void setData(List<HashMap<String, String>> HashList, int requestCode) {
		check = 0;
		int cnt = 0;
		for(HashMap<String,String> i : HashList){
			if(latest_chatting_uid < Integer.parseInt(i.get("uid"))){
				latest_chatting_uid = Integer.parseInt(i.get("uid"));
			}
			char[] talk = i.get("message").toCharArray();
			int now = 0,count = 0;
			print[0] = "";
			
			for(char g : talk){
				if(g == '\n'){
					now ++;
					print[now] = "";
					count = 0;
					continue;
				}
				else if(count > 15){
					now ++;
					print[now] = "";
					count = 0;
				}
				print[now] = print[now] + String.valueOf(g);
				count ++;	
			}
			
			Chatitem itemo = new Chatitem();
			itemo.talk = "";
			itemo.type = Integer.parseInt(i.get("m_type"));
			itemo.time = Calendar.getInstance();
			itemo.item_role = "Open";
			chats.add(itemo);
			
			for(int j=0;j<=now;j++){
				Chatitem item = new Chatitem();
				item.talk = print[j];
				item.type = Integer.parseInt(i.get("m_type"));
				item.time = Calendar.getInstance();
				item.item_role = "Main";
				chats.add(item);
			}
			
			Chatitem itemc = new Chatitem();
			itemc.talk = "";
			itemc.type = Integer.parseInt(i.get("m_type"));
			itemc.time = Calendar.getInstance();
			itemc.item_role = "Close";
			chats.add(itemc);
			
			cnt = 1;
		}
		if(cnt == 1){
			check();
		}
		getItems gg = new getItems();
		gg.execute();
	}
	@Override
	public void NullData(int requestCode){
		
		// insert 완료 or 업데이트 되는 내용이 없을때.
		
		check = 0;
		getItems gg = new getItems();
		gg.execute();
	}
	
	@Override
	public void setQuitSignal(int requestCode){
		
	}
	
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mChatMainStatus.setVisibility(View.VISIBLE);
			mChatMainStatus.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mChatMainStatus.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mChatMainForm.setVisibility(View.VISIBLE);
			mChatMainForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mChatMainForm.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mChatMainStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			mChatMainForm.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
