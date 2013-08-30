package customgooglecalendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smartwellness.ConstantVar;
import com.example.smartwellness.JsonRequestPost;
import com.example.smartwellness.R;
import com.example.smartwellness.ToKeepVar;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;

public class RockCalendar extends Fragment {

	GoogleAccountCredential credential;
	com.google.api.services.calendar.Calendar client;
	final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	final JsonFactory jsonFactory = new GsonFactory();
	CalendarInfo info;
	CalendarModel model = new CalendarModel();
	
	// add
	String calendarId;
	String userName;
	Button changeCalendar;
	Button addEvent;
	Button reserve;
	Button reserveSubmit;
	Date date_start,date_end;
	ListView listview;
	WebView webview;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    credential = GoogleAccountCredential.usingOAuth2(this.getActivity(), Collections.singleton(CalendarScopes.CALENDAR));
	    SharedPreferences settings = this.getActivity().getPreferences(this.getActivity().MODE_PRIVATE);
	    credential.setSelectedAccountName(settings.getString("accountName", null));
	    calendarId = settings.getString("CalendarId", null);
	    client = new com.google.api.services.calendar.Calendar.Builder(transport,jsonFactory,credential).setApplicationName("RockCalendar").build();
	    (new CalendarSearchTask()).execute();
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_rock_calendar, container, false);
	    changeCalendar = (Button)rootView.findViewById(R.id.changeCalendar);
	    reserve = (Button)rootView.findViewById(R.id.reserve);
	    listview = (ListView)rootView.findViewById(R.id.calendar_reserve_listview);
	    reserveSubmit = (Button)rootView.findViewById(R.id.reserve_submit);
	    setButton();
		return rootView;
	}
	
	public void setButton(){	    
	    changeCalendar.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				calendarId = null;
				credential.setSelectedAccountName(null);
			    if(checkGooglePlayServicesAvailable()){
			    	haveGooglePlayServices();
			    }
			}
	    });
	    reserve.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				webview.loadUrl( "javascript:window.location.reload( true )" );
			}
		});
	} 
	
	public void selectCalendar(){
		Intent intent = new Intent(this.getActivity(), SelectCalendarDialog.class);
		intent.putExtra("model",(new Gson()).toJson(model));
		startActivityForResult(intent, 2);
	}
	
	public void addEvent(){
		if(credential.getSelectedAccountName() == null ||
				calendarId == null){
			Toast.makeText(this.getActivity(), "캘린더를 먼저 설정해주세요", Toast.LENGTH_SHORT).show();
		}
		else{
			EventAdd.run(this);
		}
	}
	
	private void haveGooglePlayServices() {
		if(credential.getSelectedAccountName() == null){
			chooseAccount();
		}
		else{
			AsyncLoad.run(this);
		}
	}

	private void chooseAccount() {
	    startActivityForResult(credential.newChooseAccountIntent(), 0);
	}

	private boolean checkGooglePlayServicesAvailable(){
    	final int connectionCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
    	if(GooglePlayServicesUtil.isUserRecoverableError(connectionCode)){
    		Toast.makeText(this.getActivity(), "구글서비스가 없습니다.", Toast.LENGTH_SHORT).show();
    		return false;
    	}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode,int resultCode,Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case 0:
	        if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
		        String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
		        if (accountName != null) {
				    credential.setSelectedAccountName(accountName);
				    SharedPreferences settings = this.getActivity().getPreferences(Context.MODE_PRIVATE);
				    SharedPreferences.Editor editor = settings.edit();
				    editor.putString("accountName", accountName);
				    editor.commit();
				    AsyncLoad.run(this);
		        }
			}
	        break;
			case 1:
	        if (resultCode == Activity.RESULT_OK) {
	        	AsyncLoad.run(this);
	        } else {
	        	chooseAccount();
	        }
	        break;
			case 2:
			if(resultCode == Activity.RESULT_OK){
				
				calendarId = data.getStringExtra("CalendarId");
				(new CalendarAddTask()).execute(calendarId);
				toasts("캘린더가 선택되었습니다.");
				webview = (WebView)this.getActivity().findViewById(R.id.calendar_webview);
				WebSettings settings = webview.getSettings();
		        settings.setJavaScriptEnabled(true);
				webview.setWebViewClient(new WebViewClient(){
					@Override
					public boolean shouldOverrideUrlLoading(WebView view, String url) {
						view.loadUrl(url);
						return true;
					}
				});
				webview.loadUrl("https://www.google.com/calendar/embed?src=" + calendarId + "&ctz=Asia/Seoul");
				//트레이너의 calendarId를 DB에 insert함. 
			}
			break;
		}
	}
	
	@SuppressLint("ShowToast")
	IOException error;
	void geterror(IOException e){
		error = e;
	}
	void showerror(){
		Toast.makeText(this.getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
	}
	void toasts(String s){
		Toast.makeText(this.getActivity(), s, Toast.LENGTH_SHORT).show();
	}
	
	public class reserveList
	{
		private String mName;
		private String mStartDate;
		private String mEndDate;
		public reserveList(String name, String date_start, String date_end)
		{
			mName = name;
			mStartDate = date_start;
			mEndDate = date_end;
		}
		public String getName()
		{
			return mName;
		}
		public String getStartDate()
		{
			return mStartDate;
		}
		public String getEndDate()
		{
			return mEndDate;
		}
	}
	
	private class ReserveItemAdapter extends ArrayAdapter<reserveList>{
		private ArrayList<reserveList> items;
		private int layout;
		
		public ReserveItemAdapter(Context context, int id, ArrayList<reserveList> data){
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
			reserveList e = items.get(position);
			if(e != null){
				final TextView list_start_date = (TextView)v.findViewById(R.id.reserve_start_date);
				final TextView list_end_date = (TextView)v.findViewById(R.id.reserve_end_date);
				final TextView list_name = (TextView)v.findViewById(R.id.reserve_name);
				list_start_date.setText(e.getStartDate());
				list_end_date.setText(e.getEndDate());
				list_name.setText(e.getName());

				((Button)v.findViewById(R.id.reserve_submit)).setOnClickListener(new View.OnClickListener() {					
					@Override
					public void onClick(View v) {
						userName = list_name.getText().toString();
						date_start = parseDate(list_start_date.getText().toString());
						date_end = parseDate(list_end_date.getText().toString());
						addEvent();
					}
				});
			}
			return v;
		}
	}
	
	public class CalendarAddTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			HashMap<String,String> missionData = new HashMap<String,String>();
			missionData.put("table", "trainer");
			missionData.put("field", "calendar_id");
			missionData.put("data", params[0]);
			
			JsonRequestPost missionInsert = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.INSERT);
			
			missionInsert.requestPost();
			
			return null;
		}
		
		@Override
		protected void onPostExecute(final String input) {
			Toast.makeText(getActivity(), "Finish", Toast.LENGTH_SHORT).show();
		}
	}
	
	public class CalendarSearchTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			HashMap<String,String> missionData = new HashMap<String,String>();
			missionData.put("table", "reserve");
			missionData.put("field", "(select name from member where uid= any(select member_uid from reserve where trainer_uid=" + ToKeepVar.getInstance().getData() 
					+ ")), start_date, end_date");
			missionData.put("condition", "trainer_uid=" + ToKeepVar.getInstance().getData());
			
			JsonRequestPost missionInsert = new JsonRequestPost(missionData, ConstantVar.URL + ConstantVar.SELECT);
			
			missionInsert.requestPost();
			
			return missionInsert.getStr();
		}
		
		@Override
		protected void onPostExecute(final String input) {
			String replaceStr = input.replace("(select name from member where uid= any(select member_uid from reserve where trainer_uid=3))", "member_uid");
			setList(JsonRequestPost.jsonParse(Arrays.asList("member_uid", "start_date", "end_date"), replaceStr));
		}
	}

	public void setList(List<HashMap<String,String>> input)
	{
		ArrayList<reserveList> list = new ArrayList<reserveList>();
		for(int i =0; i<input.size(); i++)
			list.add(new reserveList(input.get(i).get("member_uid"),input.get(i).get("start_date"), input.get(i).get("end_date")));
		ReserveItemAdapter adapter = new ReserveItemAdapter(getActivity(), R.layout.dialog_reserve_listview, list);
		listview.setAdapter(adapter);
	}

	public Date parseDate(String date)
	{
		String[] dateTime;
		String[] resYMD;
		String[] resTMS;
		dateTime = date.split(" ");
		resYMD = dateTime[0].split("-");
		resTMS = dateTime[1].split(":");
		Date retDate = new Date(Integer.parseInt(resYMD[0]), Integer.parseInt(resYMD[1]), Integer.parseInt(resYMD[2]), Integer.parseInt(resTMS[0]), Integer.parseInt(resTMS[1]), Integer.parseInt(resTMS[2]));

		return retDate;
	}
}
