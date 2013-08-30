package customgooglecalendar;

import java.io.IOException;

import com.google.api.services.calendar.model.CalendarList;

public class AsyncLoad extends CalendarAsyncTask{

	public AsyncLoad(RockCalendar activity) {
		super(activity);
	}
	protected void doInBackground() throws IOException {
		CalendarList feed = client.calendarList().list().setFields(CalendarInfo.FEED_FIELDS).execute();
    	model.reset(feed.getItems());
	}
    static void run(RockCalendar activity) {
    	new AsyncLoad(activity).execute();
	}
}
