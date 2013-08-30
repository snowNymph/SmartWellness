package com.example.smartwellness.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.smartwellness.JsonRequestPost;
import com.example.smartwellness.ToKeepVar;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();
	public static List<String> name = new ArrayList<String>(){{
		add("id");
		add("name");
	}};
	
	public static List<HashMap<String,String>> memberData = new ArrayList<HashMap<String,String>>();
	public static String UID = ToKeepVar.getInstance().getData();
	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

	static {
		memberData = ToKeepVar.getInstance().getJson();
		for(int i =0; i<memberData.size(); i++)
			addItem(new DummyItem(memberData.get(i).get("uid"), memberData.get(i).get("name")));
	}

	private static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DummyItem {
		public String id;
		public String content;

		public DummyItem(String id, String content) {
			this.id = id;
			this.content = content;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
