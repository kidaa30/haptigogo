package com.hapticnavigation.shirtnavigo;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

public class AboutHaptiGo extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	
		View aboutView = LayoutInflater.from(this).inflate(R.layout.about, null);
		setContentView(aboutView);
		
	}
}
