package com.example.smartwellness;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ToKeepVar{
	private String data;
	private String json;
	private boolean mStatus;
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
}