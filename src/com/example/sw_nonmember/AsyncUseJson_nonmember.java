package com.example.sw_nonmember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class AsyncUseJson_nonmember extends AsyncTask<HashMap<String,String>, Void , Void>{
	ReadData listener;
	Fragment mContext;
	List<HashMap<String,String>> HashList = new ArrayList<HashMap<String,String>>();
	int what;
	int requestCode;
	List<String> wantGap = new ArrayList<String>();
	boolean mProgressFlag;
	/*
	public AsyncUseJson_nonmember(Context listener,int what,List<String> wantGap,int requestCode){
		this.what = what;
		this.mContext = listener;
		this.listener = (ReadData)listener;
		this.wantGap = wantGap;
		this.requestCode = requestCode;
	}
	*/
	public AsyncUseJson_nonmember(Fragment listener,int what,List<String> wantGap,int requestCode, boolean progress_flag){
		this.what = what;
		this.mContext = listener;
		this.listener = (ReadData)listener;
		this.wantGap = wantGap;
		this.requestCode = requestCode;
		this.mProgressFlag = progress_flag;
	}
	
	@Override
	protected Void doInBackground(HashMap<String, String>... params) {
		String C = null;
		switch(what){
		case ReadData.Insert:
			C = "insert";
			break;
		case ReadData.Select:
			C = "select";
			break;
		case ReadData.Update:
			C = "update";
			break;
		}
		
		HashMap<String, String> send = new HashMap<String,String>();
		send = params[0];
		JsonRequestPost client = new JsonRequestPost(send,"http://61.43.139.69/non_member/"+C+".php");
		client.requestPost();
		HashList = client.jsonParse(wantGap);
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		if(what == ReadData.Select){
			if(HashList.get(0).get("error") != null){
				//Toast.makeText(mContext, "접속이 끊겼습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
			}
			else if(HashList.get(0).get("null")!= null){ 
				listener.NullData(requestCode);
			}else{
				listener.setData(HashList,requestCode);
			}
		}
		else if(what== ReadData.Insert){
			if(HashList.size()!=0)
			{
				if(HashList.get(0).get("error") != null){
					//Toast.makeText(mContext, "접속이 끊겼습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
				}
				else{
					listener.NullData(requestCode);
				}
			}
			else{
				listener.NullData(requestCode);
			}
		}
		else{
			listener.setData(HashList, requestCode);
		}
		if(mProgressFlag == true)
			listener.setQuitSignal(requestCode);
	}
}
