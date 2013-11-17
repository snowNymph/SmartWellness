package com.example.smartwellness;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.RequestContent;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.smartwellness.dummy.DummyContent;

public class MemberDetailFragment extends Fragment {
	public static final String ARG_ITEM_ID = "item_id";
	public static final int CAMERA_REQUEST = 10;
	public Uri capturedImageUri;
	private DummyContent.DummyItem mItem;

	private String memberData;

	public View mStatus;
	public View mForm;
	
	public Bitmap bm;
    private ImageView memberImage;
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
		
		mStatus = (View)rootView.findViewById(R.id.member_status);
		mForm = (View)rootView.findViewById(R.id.member_detail);
		showProgress(true);
		(new MemberDetail()).execute();
		(new GetMemberImage()).execute();

		tabs = (TabHost)rootView.findViewById(android.R.id.tabhost);
		setTab();

        memberImage = (ImageView)rootView.findViewById(R.id.member_image);
        memberImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		        startActivityForResult(intent, CAMERA_REQUEST);
			}
		});

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
	
	public String getRealPathFromURI(Uri contentURI) {
	    Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
	    if (cursor == null) { // Source is Dropbox or other similar local file path
	        return contentURI.getPath();
	    } else { 
	        cursor.moveToFirst(); 
	        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	        return cursor.getString(idx); 
	    }
	}
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	    InputStream inputStream = null;
	    if ( (requestCode == CAMERA_REQUEST) && resultCode == getActivity().RESULT_OK) {
	        capturedImageUri = data.getData();
	        try {
	            inputStream = getActivity().getContentResolver().openInputStream(capturedImageUri);
	            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
	            memberImage.setImageBitmap(bitmap);
	            String[] uriParams = new String[10];
	            uriParams[0] = getRealPathFromURI(capturedImageUri);
	            (new doUploadImage()).execute(uriParams);
	        } catch (NullPointerException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        memberImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	        memberImage.setAdjustViewBounds(true);
	    }
	}
	
	public void setTab()
	{
		TabHost.TabSpec ts;
		tabs.setup();
		
		ts = tabs.newTabSpec("vital");
		ts.setContent(R.id.main_tab1_webview);
		ts.setIndicator("�ǰ�");
		tabs.addTab(ts);

		ts = tabs.newTabSpec("exercise");
		ts.setContent(R.id.main_tab2_webview);
		ts.setIndicator("�");
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
			if(input != null){
				HashMap<String,String> parseRet = new HashMap<String,String>();
				parseRet = JsonRequestPost.jsonParse(Arrays.asList("name","pt_cnt","age","sex","phone") , input).get(0);
				/* last, pt, reserve �ʿ�*/
				((TextView)getView().findViewById(R.id.member_name)).setText(parseRet.get("name"));
				((TextView)getView().findViewById(R.id.member_pt_cnt)).setText(parseRet.get("pt_cnt"));
				((TextView)getView().findViewById(R.id.member_phone)).setText(parseRet.get("phone"));
				((TextView)getView().findViewById(R.id.member_age)).setText(parseRet.get("age"));
				((TextView)getView().findViewById(R.id.member_sex)).setText(parseRet.get("sex"));
			}
		}
	}
    
    public class GetMemberImage extends AsyncTask<Void, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                URL url = new URL(ConstantVar.URL + "member_image/" + getArguments().getString(ARG_ITEM_ID) + ".jpg");
                bm = BitmapFactory.decodeStream(url.openConnection().getInputStream());   
                return true;
            }catch (IOException ex){
            	return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
        	super.onPostExecute(result);
        	if(result == true) memberImage.setImageBitmap(Bitmap.createScaledBitmap(bm, memberImage.getWidth(), memberImage.getHeight(), true));
        	else memberImage.setImageResource(R.drawable.question_mark);
        	showProgress(false);
        }
    }
    
	public class doUploadImage extends AsyncTask<String, Void, Void>
	{
		@Override
		protected Void doInBackground(String... params) {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost postRequest = new HttpPost("http://61.43.139.69/image.php");

                MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
                reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

                reqEntity.addPart("uploadedfile", new org.apache.http.entity.mime.content.FileBody(new File(params[0])));
                reqEntity.addTextBody("id", getArguments().getString(ARG_ITEM_ID));
                reqEntity.setCharset(Charset.forName("UTF-8"));

                postRequest.setEntity(reqEntity.build());

                HttpResponse response = httpClient.execute(postRequest);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                String sResponse;
                StringBuilder s = new StringBuilder();

                while ((sResponse = reader.readLine()) != null) {
                    s = s.append(sResponse);
                }
                Log.e("error", s.toString());
                System.out.println("Response: " + s);
                httpClient.getConnectionManager().shutdown();
            } catch (Exception e) {
                Log.e(e.getClass().getName(), e.getMessage());
            }
	        return null;
	    }
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
	
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mStatus.setVisibility(View.VISIBLE);
			mStatus.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mStatus.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mForm.setVisibility(View.VISIBLE);
			mForm.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mForm.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mStatus.setVisibility(show ? View.VISIBLE : View.GONE);
			mForm.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
