package customgooglecalendar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;


public class EventAdd extends EventAsyncTask{
	public EventAdd(RockCalendar activity) {
		super(activity);
	}
	protected void doInBackground() throws IOException {
		Event event = new Event();
		event.setSummary("PT¿¹¾à : " + activity.userName+"´Ô");
		event.setLocation("Fitness Center");
		
		ArrayList<EventAttendee> attendees = new ArrayList<EventAttendee>();
		attendees.add(new EventAttendee().setEmail("jrj325@hanmir.com"));
		event.setAttendees(attendees);
		

		Date startDate = activity.date_start;
		Date endDate = activity.date_end;
		DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
		event.setStart(new EventDateTime().setDateTime(start));
		DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
		event.setEnd(new EventDateTime().setDateTime(end));
		
		client.events().insert(CalendarId, event).execute();
	}
    static void run(RockCalendar activity) {
    	new EventAdd(activity).execute();
	}
}
