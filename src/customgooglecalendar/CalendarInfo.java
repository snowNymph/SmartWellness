package customgooglecalendar;

import com.google.api.client.util.Objects;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;

class CalendarInfo implements Comparable<CalendarInfo>, Cloneable {

	  static final String FIELDS = "id,summary";
	  static final String FEED_FIELDS = "items(" + FIELDS + ")";

	  String id;
	  String summary;

	  CalendarInfo(String id, String summary) {
	    this.id = id;
	    this.summary = summary;
	  }

	  CalendarInfo(Calendar calendar) {
	    update(calendar);
	  }

	  CalendarInfo(CalendarListEntry calendar) {
	    update(calendar);
	  }

	  @Override
	  public String toString() {
	    return Objects.toStringHelper(CalendarInfo.class).add("id", id).add("summary", summary)
	        .toString();
	  }

	  public int compareTo(CalendarInfo other) {
	    return summary.compareTo(other.summary);
	  }

	  @Override
	  public CalendarInfo clone() {
	    try {
	      return (CalendarInfo) super.clone();
	    } catch (CloneNotSupportedException exception) {
	      // should not happen
	      throw new RuntimeException(exception);
	    }
	  }

	  void update(Calendar calendar) {
	    id = calendar.getId();
	    summary = calendar.getSummary();
	  }

	  void update(CalendarListEntry calendar) {
	    id = calendar.getId();
	    summary = calendar.getSummary();
	  }
	}
