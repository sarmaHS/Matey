package com.mateyinc.marko.matey.helpers;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.mateyinc.marko.matey.AskHelp;
import com.mateyinc.marko.matey.R;

@SuppressLint("NewApi")
public class MotherActivity extends AppCompatActivity {

	protected String device_id = "";
	protected Toolbar toolbar;
	
	public void setStatusBarColor () {
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = this.getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(this.getResources().getColor(R.color.statusBar));
		}
		
	}
	
	public void setCustomToolbar () {
		
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_makenew) {
			Intent makeNew = new Intent (this, AskHelp.class);
			startActivity(makeNew);
			
			return true;
		}
		if (id == R.id.action_settings) {
			return true;
		}
		return true;
	}
	
}
