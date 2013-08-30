package customgooglecalendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.smartwellness.R;
import com.google.gson.Gson;

public class SelectCalendarDialog extends Activity{
	CalendarModel model;
	Intent intent;
	ArrayAdapter<CalendarInfo> adapter;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Select Calendar");
		setContentView(R.layout.selectdialog);
		intent = this.getIntent();
		model = (new Gson()).fromJson(intent.getStringExtra("model"),CalendarModel.class);
		
		adapter = new ArrayAdapter<CalendarInfo>(this, android.R.layout.simple_list_item_1, model.toSortedArray()) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				// by default it uses toString; override to use summary instead
				TextView view = (TextView) super.getView(position, convertView, parent);
				CalendarInfo calendarInfo = getItem(position);
				view.setText(calendarInfo.summary);
				return view;
			}
		};		
		ListView list = (ListView)findViewById(R.id.List);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
				
				CalendarInfo info = adapter.getItem(position);
				String calendarId;
				calendarId = info.id;
				SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
			    SharedPreferences.Editor editor = settings.edit();
			    editor.putString("CalendarId", calendarId);
			    editor.commit();
			    
				Intent retent = new Intent();
				retent.putExtra("CalendarId", calendarId);
				setResult(Activity.RESULT_OK, retent);
				finish();
			}
		});
	}
}
