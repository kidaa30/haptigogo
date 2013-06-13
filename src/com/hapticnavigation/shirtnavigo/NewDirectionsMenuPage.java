package com.hapticnavigation.shirtnavigo;

import java.util.List;
import java.util.Locale;

import com.google.android.maps.MapActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
/**
 * Repsonsible for inflating the Directions' editable text boxes and processing the user input in 
 * the text boxes and translating that information into coordinates and pass it to GoogleMapsUriProcessor class.
 * @author Essa Haddad, Sketch Recognition Lab, Texas A&M.
 *
 */
public class NewDirectionsMenuPage extends MapActivity {

	private static final int MAX_MATCHES = 5; // Five similar street address
												// matches
	private EditText m_fromTextField;
	private EditText m_toTextField;
	private Button m_submitButton;
	private Geocoder m_geoCoder;
	
	private String m_fromAddress;
	private String m_toAddress;

	private List<Address> m_fromStreetAddressMatches;
	private List<Address> m_toStreetAddressMatches;
	private Thread searchAddresses;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
/*
 * (non-Javadoc)
 * sets a geocoder to translate string addresses into longitudes and latitudes and pass it to GoogleMapsUriProcessor. It also checks for Internet connectivity.
 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		if(isOnline())
		{
		setContentView(R.layout.mapsnewdirections);

		m_geoCoder = new Geocoder(this, Locale.getDefault());

		m_fromTextField = (EditText) findViewById(R.id.from);

		m_toTextField = (EditText) findViewById(R.id.to);

		m_submitButton = (Button) findViewById(R.id.submit);
		m_submitButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				searchAddresses = new Thread() {
					public void run() {
						 m_fromAddress = (m_fromTextField.getText())
								.toString();
						m_toAddress = (m_toTextField.getText()).toString();

						try {
							m_fromStreetAddressMatches = m_geoCoder
									.getFromLocationName(m_fromAddress,
											MAX_MATCHES);
							m_toStreetAddressMatches = m_geoCoder
									.getFromLocationName(m_toAddress, MAX_MATCHES);
						} catch (Exception e) {
							Log.v("ERROR", "I did not process geotags");
							e.printStackTrace();
						}
						showAddressResults.sendEmptyMessage(0);
					}
				};
				searchAddresses.start();
			}
		});
		}else{
			Toast.makeText(this, "No internet connectivity! please check your internet settings.", 60000).show();
		}
	}
	/*
	 * 
	 */
	public boolean isOnline() { 
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
		NetworkInfo netInfo = cm.getActiveNetworkInfo();    
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {        
			return true;    
			}    
		return false;
		}

	private Handler showAddressResults = new Handler() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		double latitude1, latitude2;
		double longitude1, longitude2;
		boolean validFromDestination = false;
		boolean validtoDestination = false;

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			// m_progressDialog.dismiss();

			if (m_fromStreetAddressMatches.size() == 0) {
				Toast.makeText(getApplicationContext(),"Could not determine from location. Please type in a valid locations!!",50000).show();
			} else {
				for (int i = 0; i < m_fromStreetAddressMatches.size(); ++i) {
					Address from = m_fromStreetAddressMatches.get(i);
					latitude1 = from.getLatitude();
					longitude1 = from.getLongitude();
					validFromDestination = true;
				}
			}
			if (m_toStreetAddressMatches.size() == 0) {
				Toast.makeText(
						getApplicationContext(),"Could not determine \"To\" location. Please type in a valid locations!!",50000).show();
			} else {
				for (int i = 0; i < m_toStreetAddressMatches.size(); ++i) {
					Address from = m_toStreetAddressMatches.get(i);
					latitude2 = from.getLatitude();
					longitude2 = from.getLongitude();
					validtoDestination = true;
				}
			}

			if (validFromDestination && validtoDestination) {
				Intent mapsDirectionsIntent = new Intent(
						"com.hapticnavigation.shirtnavigo.MAPDIRECTIONS");
				mapsDirectionsIntent.putExtra("Latitude1", latitude1);
				mapsDirectionsIntent.putExtra("Longitude1", longitude1);
				mapsDirectionsIntent.putExtra("Latitude2", latitude2);
				mapsDirectionsIntent.putExtra("Longitude2", longitude2);
				mapsDirectionsIntent.putExtra("fromAddress", m_fromAddress);
				mapsDirectionsIntent.putExtra("toAddress", m_toAddress);
				startActivity(mapsDirectionsIntent);
			}
		}
	};
}
