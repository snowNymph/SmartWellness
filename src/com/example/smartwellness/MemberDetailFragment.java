package com.example.smartwellness;

import java.util.Arrays;
import java.util.HashMap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.smartwellness.dummy.DummyContent;

public class MemberDetailFragment extends Fragment {
	public static final String ARG_ITEM_ID = "item_id";
	private DummyContent.DummyItem mItem;

	private String memberData;
	
	public TabHost tabs;
	public MemberDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_member_detail, container, false);
		(new MemberDetail()).execute();
		tabs = (TabHost)rootView.findViewById(android.R.id.tabhost);
		setTab();
		WebView vital_view = (WebView)rootView.findViewById(R.id.main_tab1_webview);
		WebView exercise_view = (WebView)rootView.findViewById(R.id.main_tab2_webview);
		WebSettings vital_settings = vital_view.getSettings();
		vital_settings.setUseWideViewPort(true);
		vital_settings.setJavaScriptEnabled(true);
		WebSettings exercise_settings = exercise_view.getSettings();
		exercise_settings.setUseWideViewPort(true);
		exercise_settings.setJavaScriptEnabled(true);
		vital_view.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		exercise_view.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
		});
		vital_view.loadUrl(ConstantVar.URL + "chart/chart_show_weight.php?uid=" + getArguments().getString(ARG_ITEM_ID));
		exercise_view.loadUrl(ConstantVar.URL + "chart/chart_exercise.php?uid=" + getArguments().getString(ARG_ITEM_ID));
		Button button1 = (Button)rootView.findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle arguments = new Bundle();
				arguments.putString(MemberMission.ARG_ITEM_ID, getArguments().getString(ARG_ITEM_ID));
				MemberMission fragment = new MemberMission();
				fragment.setArguments(arguments);
				getFragmentManager().beginTransaction()
						.replace(R.id.member_detail_container, fragment).addToBackStack(null).commit();
			}
		});
		Button button2 = (Button)rootView.findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {	
				Bundle arguments = new Bundle();
				arguments.putString(MemberDiet.ARG_ITEM_ID, getArguments().getString(ARG_ITEM_ID));
				MemberDiet fragment = new MemberDiet();
				fragment.setArguments(arguments);
				getFragmentManager().beginTransaction()
						.replace(R.id.member_detail_container, fragment).addToBackStack(null).commit();
			}
		});
		Button button3 = (Button)rootView.findViewById(R.id.button3);
		button3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {		
				Bundle arguments = new Bundle();
				arguments.putString(MemberAdvice.ARG_ITEM_ID, getArguments().getString(ARG_ITEM_ID));
				MemberAdvice fragment = new MemberAdvice();
				fragment.setArguments(arguments);
				getFragmentManager().beginTransaction()
						.replace(R.id.member_detail_container, fragment).addToBackStack(null).commit();
			}
		});
		// Show the dummy content as text in a TextView.
		if (mItem != null) {
		}

		return rootView;
	}
	
	public void setTab()
	{
		TabHost.TabSpec ts;
		tabs.setup();
		
		ts = tabs.newTabSpec("vital");
		ts.setContent(R.id.main_tab1_webview);
		ts.setIndicator("건강");
		tabs.addTab(ts);

		ts = tabs.newTabSpec("exercise");
		ts.setContent(R.id.main_tab2_webview);
		ts.setIndicator("운동");
		tabs.addTab(ts);
	}
	
	public class MemberDetail extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			// TODO: attempt authentication against a network service.
			HashMap<String,String> loginData = new HashMap<String,String>();
			loginData.put("table", "member");
			loginData.put("field", "name,pt_cnt,age,sex,phone");
			loginData.put("condition", "uid=" + getArguments().getString(ARG_ITEM_ID));
			JsonRequestPost loginPost = new JsonRequestPost(loginData, ConstantVar.URL + ConstantVar.SELECT);
			
			loginPost.requestPost();
			if( loginPost.getStr() == null )
				return null;
			else
			{
				memberData = loginPost.getStr();
			}
			return loginPost.getStr();
		}
		
		@Override
		protected void onPostExecute(final String input) {
			taskFunc(input);
		}
	}
	
	public void taskFunc(String input)
	{
		if(input != null){
			HashMap<String,String> parseRet = new HashMap<String,String>();
			parseRet = JsonRequestPost.jsonParse(Arrays.asList("name","pt_cnt","age","sex","phone") , input).get(0);
			/* last, pt, reserve 필요*/
			((TextView)getView().findViewById(R.id.member_name)).setText(parseRet.get("name"));
			((TextView)getView().findViewById(R.id.member_pt_cnt)).setText(parseRet.get("pt_cnt"));
			((TextView)getView().findViewById(R.id.member_phone)).setText(parseRet.get("phone"));
			((TextView)getView().findViewById(R.id.member_age)).setText(parseRet.get("age"));
			((TextView)getView().findViewById(R.id.member_sex)).setText(parseRet.get("sex"));
		}
	}
}
