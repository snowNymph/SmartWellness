package customgooglecalendar;

import java.io.IOException;

import android.os.AsyncTask;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;

abstract class CalendarAsyncTask extends AsyncTask<Void, Void, Boolean> {
	RockCalendar activity;
	CalendarModel model;
	com.google.api.services.calendar.Calendar client;
	public CalendarAsyncTask(RockCalendar activity){
		this.activity = activity;
		this.model = activity.model;
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
	protected void onPostExecute(Boolean result){
		super.onPostExecute(result);
	    if(!result){
	    	activity.showerror();
	    }else{
	    	activity.selectCalendar();
	    }
	}
	abstract protected void doInBackground() throws IOException;
}
