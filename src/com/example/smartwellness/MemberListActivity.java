package com.example.smartwellness;

//import CustomGoogleCalendar.RockCalendar;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import customgooglecalendar.RockCalendar;

/**
 * An activity representing a list of Members. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link MemberDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MemberListFragment} and the item details (if present) is a
 * {@link MemberDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link MemberListFragment.Callbacks} interface to listen for item selections.
 */
public class MemberListActivity extends FragmentActivity implements
		MemberListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_member_list);
		ColorDrawable colorDrawable = new ColorDrawable();
		
        ActionBar actionBar = getActionBar();
        colorDrawable.setColor(0xff992c26);
        actionBar.setBackgroundDrawable(colorDrawable);
        //actionBar.setIcon(R.drawable.);//api level 14
//        actionBar.set
		if (findViewById(R.id.member_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((MemberListFragment) getSupportFragmentManager().findFragmentById(
					R.id.member_list)).setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link MemberListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(MemberDetailFragment.ARG_ITEM_ID, id);
			MemberDetailFragment fragment = new MemberDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.member_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, MemberDetailActivity.class);
			detailIntent.putExtra(MemberDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.title, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String text = null;
		
		switch(item.getItemId()){
		case android.R.id.home:
			text = "Application icon";
			break;
			
		case R.id.item1:
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.member_detail_container, new MemberAdd());
			ft.addToBackStack(null);
			ft.commit();
			break;
		}
			
		case R.id.item2:
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.member_detail_container, new RockCalendar());
			ft.addToBackStack(null);
			ft.commit();
			break;
		}
		
		
		case R.id.item3:
		{
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.member_detail_container, new TrainerSetting());
			ft.addToBackStack(null);
			ft.commit();
			break;
		}
		default:
			return false;
		}
		return true;
	}
}
