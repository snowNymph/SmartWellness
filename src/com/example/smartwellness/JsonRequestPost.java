package com.example.smartwellness;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonRequestPost {
	private HashMap<String,String> mData;
	private String mUrl;
	private String mRetStr;
	private int statusCode;
	
	//HashMap<key,value>
	public JsonRequestPost(HashMap<String,String> data, String url)
	{
		mData = data;
		mUrl = url;
		statusCode = 0;
		mRetStr = null;
	}

	public void requestPost()
	{
		Set<Entry<String,String>> set = mData.entrySet();
		Iterator<Entry<String,String>> iter = set.iterator();
		
		HttpClient httpClient = new DefaultHttpClient();
	    try {
	        HttpPost request = new HttpPost(mUrl);
	        List<NameValuePair> nvp = new ArrayList<NameValuePair>();
	        
	        while(iter.hasNext())
	        {
	        	Map.Entry<String, String> e = iter.next();
	        	nvp.add(new BasicNameValuePair(e.getKey(), URLEncoder.encode(e.getValue(), "UTF-8")));
	        }
	        request.setEntity(new UrlEncodedFormEntity(nvp));
	        HttpResponse response2 = httpClient.execute(request);

	        mRetStr = EntityUtils.toString(response2.getEntity());
	        statusCode = response2.getStatusLine().getStatusCode();
	    }catch (Exception ex) {
	    	System.out.println("error");
	    } finally {
	        httpClient.getConnectionManager().shutdown();
	    }
	}

	public static List<HashMap<String,String>> jsonParse(List<String> name, String jsonStr)
	{
		if(jsonStr == null)
			return null;
		HashMap<String,String> inputData = new HashMap<String,String>();
		List<HashMap<String,String>> retVal = new ArrayList<HashMap<String,String>>();
		try {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonStr);
			JSONArray jsonArr = (JSONArray) obj;
			Iterator iter = jsonArr.iterator();
			while(iter.hasNext())
			{
				JSONObject objw = (JSONObject) iter.next();
				inputData.clear();	
				for(int i =0; i<name.size(); i++)
				{
					if(objw.get(name.get(i)) != null)
						inputData.put(name.get(i), (String)objw.get(name.get(i)));
				}
				retVal.add((HashMap<String, String>) inputData.clone());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return retVal;
	}
	
	public int getCode()
	{
		return statusCode;
	}
	
	public String getStr()
	{
		return mRetStr;
	}
}
