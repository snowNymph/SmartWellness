package customgooglecalendar;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

abstract class EventAsyncTask extends AsyncTask<Void, Void, Boolean> {
	RockCalendar activity;
	String CalendarId;
	com.google.api.services.calendar.Calendar client;
	public EventAsyncTask(RockCalendar activity){
		this.activity = activity;
		this.CalendarId = activity.calendarId;
		this.client = activity.client;
	}
	@Override
	protected Boolean doInBackground(Void... params) {
		try{
			doInBackground();
			return true;
		}catch (UserRecoverableAuthIOException userRecoverableException) {
			activity.startActivityForResult(userRecoverableException.getIntent(), 1);
        }catch(IOException e){
			activity.geterror(e);
		}
		return false;
	}
	@SuppressLint("ShowToast")
	protected void onPostExecute(Boolean result){
		super.onPostExecute(result);
	    if (result) {
	    	activity.toasts("저장되었습니다");
        }
	    else{
	    	activity.showerror();
	    }
	}
	abstract protected void doInBackground() throws IOException;
}
