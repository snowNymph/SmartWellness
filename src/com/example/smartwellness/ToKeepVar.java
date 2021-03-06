package com.example.smartwellness;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ToKeepVar{
	private String data;
	private String json;
	private boolean mStatus;
	private int mDeviceType;
	private int mTrainerUid;
	
	public int getTrainerUID(){
		return mTrainerUid;
	}
	
	public void setTrainerUID(int uid){
		mTrainerUid = uid;
	}
	
	public String getData()
	{
		return data;
	}
	
	public void setNetworkStatus(boolean connection)
	{
		this.mStatus = connection;
	}
	
	public void setData(String data)
	{
		this.data = data;
	}
	
	public void settest(String sss)
	{
		this.json = sss;
	}
	
	public boolean getNetworkStatus()
	{
		return mStatus;
	}
	
	private static ToKeepVar instance = null;
	
	public static synchronized ToKeepVar getInstance(){
		if( null == instance ){
			instance = new ToKeepVar();
		}
		return instance;
	}

	public List<HashMap<String,String>> getJson()
	{
		return JsonRequestPost.jsonParse(Arrays.asList("uid","name"), json);
	}

	public void setType(int deviceType)
	{
		this.mDeviceType = deviceType;
	}
	public int getType() {
		// TODO Auto-generated method stub
		return mDeviceType;
	}
}