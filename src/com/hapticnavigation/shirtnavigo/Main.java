/*
 * 
 */
package com.hapticnavigation.shirtnavigo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
/**
 * provides the applicaton startup background that allows our application to work smoothly and load all its component for user interaction.
 * The class will start a new intent that connects to the Tabbed Menu screen. 
 * @author Essa Haddad
 *
 */
public class Main extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);

		Thread logoTimer = new Thread() {
			public void run() {
				try {
					short logoTimer = 0;
					while (logoTimer < 3000) {
						sleep(100);
						logoTimer += 100;
					}
					startActivity(new Intent(
							"com.hapticnavigation.shirtnavigo.MENUSCREEN"));
				} catch (InterruptedException e) {
					Toast.makeText(getApplicationContext(),
							"Application failed to launch! please try again.",
							50000).show();
				} finally {
					finish();
				}
			}
		};
		logoTimer.start();
	}
}