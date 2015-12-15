package com.hapticnavigation.shirtnavigo;


import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TabHost;
import android.widget.TextView;
/**
 * Sets Three tab buttons that triggers three different Intents:
 * <ul>
 * 	<li>A static Texas A&M tour </li>
 *  <li>A Google Maps driving direction page </li>
 *  <li>A directory access to the maps folder that is unique for our application</li>
 * </ul>
 * @authors Essa Haddad, Alex Reynolds. Texas A&M
 *
 */

public class MenuPage extends TabActivity {

/*
 * (non-Javadoc)
 * Once this Activity gets called, the tabs will be created with names that describes the intents that
 * they start once selected.
 * @see android.app.ActivityGroup#onCreate(android.os.Bundle)
 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		Resources res = getResources();
		TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  
	    //View tabView = LayoutInflater.from(this).inflate(R.layout.tabs_bg, null);
	    //TextView tabText = (TextView) tabView.findViewById(R.id.tabsText);
	    
	    // Tour list tab
	    View tabView1 = LayoutInflater.from(this).inflate(R.layout.tabs_bg, null);
	    TextView tabText1 = (TextView) tabView1.findViewById(R.id.tabsText);
	    tabText1.setText("Tours");
	    ImageView tabIcon1 = (ImageView) tabView1.findViewById(R.id.tabsIcon);
	    tabIcon1.setImageDrawable(res.getDrawable(R.drawable.ti_map));
	    intent = new Intent("com.hapticnavigation.shirtnavigo.LISTTOUR");
	    spec = tabHost.newTabSpec("tours").setIndicator(tabView1).setContent(intent);
	    tabHost.addTab(spec);
	    
	    // Driving directions tab
	    View tabView2 = LayoutInflater.from(this).inflate(R.layout.tabs_bg, null);
	    TextView tabText2 = (TextView) tabView2.findViewById(R.id.tabsText);
	    tabText2.setText("Directions");
	    ImageView tabIcon2 = (ImageView) tabView2.findViewById(R.id.tabsIcon);
	    tabIcon2.setImageDrawable(res.getDrawable(R.drawable.ti_car));
	    intent = new Intent("com.hapticnavigation.shirtnavigo.GETNEWDIRECTIONS");
	    spec = tabHost.newTabSpec("drivingdirecs").setIndicator(tabView2).setContent(intent);
	    tabHost.addTab(spec);
	  
	    // About tab
	    View tabView3 = LayoutInflater.from(this).inflate(R.layout.tabs_bg, null);
	    TextView tabText3 = (TextView) tabView3.findViewById(R.id.tabsText);
	    //tabText3.setText("Directory");
	    tabText3.setText("About");
	    ImageView tabIcon3 = (ImageView) tabView3.findViewById(R.id.tabsIcon);
	    tabIcon3.setImageDrawable(res.getDrawable(R.drawable.ti_about));
	    //intent = new Intent("com.hapticnavigation.shirtnavigo.GETFILES");
	    intent = new Intent("com.hapticnavigation.shirtnavigo.ABOUT");
	    spec = tabHost.newTabSpec("about").setIndicator(tabView3).setContent(intent);
	    tabHost.addTab(spec);
	    
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainmenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
	    switch (item.getItemId()) {
	    // Exits HaptiGo
	    case R.id.quit:
	    	System.runFinalizersOnExit(true);
			finish();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}
