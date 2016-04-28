package com.mateyinc.marko.matey.activity.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.slidingtabs.PagerAdapter;
import com.mateyinc.marko.matey.slidingtabs.SlidingTabLayout;

@SuppressLint("NewApi")
// ActionBarActivity
public class Home extends MotherActivity {
	
	// variables for tabs functionality
	ViewPager viewPager;
	SlidingTabLayout tabs;
	PagerAdapter adapter;
	CharSequence Titles[] = {"Home","All friends","Groups"};
    int Numboftabs = 3;

	public void setLife(){}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		setContentView(R.layout.home_container_layout);
		super.setStatusBarColor();
		super.setCustomToolbar();
		
		// finding ViewPager in res folder by id
		// and setting the adapter for it
		viewPager = (ViewPager) findViewById(R.id.pager);
		adapter = new PagerAdapter(getSupportFragmentManager(), Titles, Numboftabs);
		viewPager.setAdapter(adapter);
		
		// finding tabs view
		tabs = (SlidingTabLayout) findViewById(R.id.tabs);
		tabs.setDistributeEvenly(true);
		
		// settings for tabs design
		tabs.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
            @Override
            public int getIndicatorColor(int position) {
                return getResources().getColor(R.color.whiteColor);
            }
            
        });
 
        // Setting the ViewPager for the tabs
        tabs.setViewPager(viewPager);
   
	}
	
}
