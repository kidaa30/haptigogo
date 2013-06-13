package com.hapticnavigation.shirtnavigo;

import hapticVibroPackage.DirectionCalc;
import hapticVibroPackage.LocationLoader;
import hapticVibroPackage.VibroClass;
import hapticVibroPackage.sendingClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.hapticnavigation.parser.RouteSaxParser;
import com.hapticnavigation.shirtnavigo.ShirtNavigation;
import com.hapticnavigation.shirtnavigo.TourPage.ArduinoReceiver;

/**
 * This class receives the coordinates of the driving directions in order to retrieve the driving directions from Google Map's website.
 * The rest of the operations in this class is similar to TamTourPage() and TourPage() where the directions are represented graphically on the Android's screen and the operations to process
 * waypoints and heading are orchestrated by this class.
 * 
 * @author Essa Haddad, Kate Boxer Sketch Recognition Lab, Texas A&M. 
 *
 */
public class GoogleMapsUriProcessor extends MapActivity {
	
	private final static boolean DRIVING_DIRECTIONS = false;

	private StringBuilder m_urlString;
	private RouteSaxParser m_routeSaxParser;

	private MapView m_mapView;
	private MapController m_mapController;
	private ShirtNavigation m_shirtNavigation;

	private Bundle m_extrasFromToDirections;
	
	LocationManager locationManager;
	LocationListener locationListener;
	Location currentLocation;
	Location nextDestination;
	
	private SensorManager mSensorManager;
    private Sensor mSensor;
    float[] mValues;
    Queue<Location> wayPoints;
    
    String DEVICE_ADDRESS =  "00:06:66:42:22:7C";
	
    // User must be in 20 meters of the waypoint for it to be recognized (driving radius)
	double waypointRadius = 20;
	double distToNext;
	int currBearing;
	double nextBearing;
	boolean rightTurn;
	double turnAngle;
  
	Timer timer = new Timer();
	VibroClass vibro = new VibroClass();
	ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	DirectionCalc direc;
	sendingClass sender;
	List<Double> headings;
	int indexHeadings = 0;

	/*
	 * (non-Javadoc)
	 *  initializes private members and starts the process of retrieving a KML file, parsinng, and the tokenizing it.
	 *  After getting the data with the right data type. onCreate will call drawlayout in order to display all 
	 *  the necessary information as described in the fuction's comments.
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);

		m_extrasFromToDirections = getIntent().getExtras();

		m_shirtNavigation = new ShirtNavigation();
		setContentView(R.layout.googlemap);
		m_mapView = (MapView) findViewById(R.id.mapView);
		m_mapView.setBuiltInZoomControls(true);
		m_mapController = m_mapView.getController();
		m_mapController.setZoom(18);
// initialize a URI string with "from" and "to" coordinates that are passed to this intent from NewDirectionsMenuPage().
		m_urlString = new StringBuilder();
		m_urlString.append("http://maps.google.com/maps?f=d&hl=en");
		m_urlString.append("&saddr=");// from
		m_urlString.append(Double.toString(m_extrasFromToDirections
				.getDouble("Latitude1")));
		m_urlString.append(",");
		m_urlString.append(Double.toString(m_extrasFromToDirections
				.getDouble("Longitude1")));
		m_urlString.append("&daddr=");// to
		m_urlString.append(Double.toString(m_extrasFromToDirections
				.getDouble("Latitude2")));
		m_urlString.append(",");
		m_urlString.append(Double.toString(m_extrasFromToDirections
				.getDouble("Longitude2")));
		m_urlString.append("&ie=UTF8&0&om=0&output=kml");

		InputStream inputStream = null;

		locationListener = new LocListener();
	  	startListening();
	  	
	 		direc = new DirectionCalc(this, DEVICE_ADDRESS);
	  		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
	  		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		   	mSensorManager.registerListener(mListener, mSensor,1);
		    
		   	headings = m_shirtNavigation.getHeading();
	// connect to the Internet and use the URI to retrieve a KML file for the driving directions, parse the file, and tokenize the returned values in order to give them the right data type.
			try {
				URL url = new URL(m_urlString.toString());
				inputStream = url.openStream();
				m_routeSaxParser = new RouteSaxParser(inputStream);
				m_shirtNavigation.setPathData(m_routeSaxParser.executeSaxParsing(), DRIVING_DIRECTIONS);
			} catch (MalformedURLException e) {
				Toast.makeText(getApplicationContext(), "URL is malformed. Please contact developer.",50000).show();
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "KML map file couldn't be processed! Please contact developer.",50000).show();
			} catch (SAXException e) {
				Toast.makeText(getApplicationContext(), "SAX parser failed to initialize.",50000).show();
			} catch (ParserConfigurationException e) {
				Toast.makeText(getApplicationContext(), "File could not be parsed.",50000).show();
			}finally {
			try {
				if (inputStream != null)
					inputStream.close();	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(getApplicationContext(),"Directions input Stream failed to close!", 50000).show();
				}
			}
			
			sender = new sendingClass(this , DEVICE_ADDRESS);
				
				Amarino.connect(this, DEVICE_ADDRESS);
				registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
				
			drawRoute();
			timerFunction();
	}
	@Override
	protected void onDestroy() {
		locationManager.removeUpdates(locationListener);
		this.finish();
		super.onDestroy();
	}


	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		locationManager.removeUpdates(locationListener);
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); 
		super.onResume();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
/*
 * This function is responsible for retrieving coordinates, drawing a line to represent the path with a start and finish pushpins, as well as pushpins to represent the landmarks that designates 
 * major decision point in the driving directions. 
 */
	private void drawRoute() {
		List<GeoPoint> coordinates = m_shirtNavigation.getGeoPoints();
		List<Overlay> mapsOverlays = m_mapView.getOverlays();
		OverlayItem overlayItem; // used frequently in order to set up the pushpins in the itemized overlay
		RouteOverlay routeOverlay;// used to draw the line that represents the route
		//The six four declarations are used to calibrate the zoom feature in order to show the whole path. 
		int minLatitude = Integer.MAX_VALUE;
		int maxLatitude = Integer.MIN_VALUE;
		int minLongitude = Integer.MAX_VALUE;
		int maxLongitude = Integer.MIN_VALUE;
		int latitude;
		int longitude;
		/*
		 *  itemized overlay is used solely because it already did the job of calibrating the draw method in order to have the pins perfectly laid on the GeoPoints.
		 *  You can use an overlay to substitute this but you have to create your own class to override the draw method. 
		 *  
		 *  A different MapItemized Overlay is used because we are using a different Drawable. calling .makeIcon() for the overlay does not work because you need to override
		 *  the draw method independently
		 */
		Drawable startDrawable = this.getResources().getDrawable(R.drawable.pushpinstart);
		MapItemizedOverlay mapItemizedOverlay2 = new MapItemizedOverlay(startDrawable, this);
		Drawable finishDrawable = this.getResources().getDrawable(R.drawable.pushpinend);
		MapItemizedOverlay mapItemizedOverlay3 = new MapItemizedOverlay(finishDrawable, this);
		
		/*
		 * Access the coordinates and pass each pair to route overlay to draw the line. We are also calculating the min/max longitute/latitude for the zoom control.
		 */
		for (int index = 1; index < coordinates.size(); index++) {
			if(index == 1){
				//set the information that should be shown in the dialog box and show the start pushpin on the screen
				overlayItem = new OverlayItem(coordinates.get(index - 1), m_extrasFromToDirections.getString("fromAddress"), "Start");
				mapItemizedOverlay2.addOverlay(overlayItem);
				mapsOverlays.add(mapItemizedOverlay2);
				
				latitude = coordinates.get(index - 1).getLatitudeE6();
				longitude = coordinates.get(index - 1).getLongitudeE6();
				maxLatitude = Math.max(latitude, maxLatitude);
				minLatitude = Math.min(latitude, maxLatitude);
				maxLongitude = Math.max(longitude, maxLongitude);
				minLongitude = Math.min(longitude, minLongitude);
				}
			latitude = coordinates.get(index).getLatitudeE6();
			longitude = coordinates.get(index).getLongitudeE6();
			maxLatitude = Math.max(latitude, maxLatitude);
			minLatitude = Math.min(latitude, maxLatitude);
			maxLongitude = Math.max(longitude, maxLongitude);
			minLongitude = Math.min(longitude, minLongitude);
			//set the information that should be shown in the dialog box and show the finish pushpin on the screen
			if(index == (coordinates.size() - 1)){
				overlayItem = new OverlayItem(coordinates.get(index), m_extrasFromToDirections.getString("toAddress"), "Finish");
				mapItemizedOverlay3.addOverlay(overlayItem);
				mapsOverlays.add(mapItemizedOverlay3);	
			}
			// pass each two coordinates to routeOverlay in order to draw a line between them
			routeOverlay = new RouteOverlay(coordinates.get(index - 1),
					coordinates.get(index), Color.rgb(97, 9, 239)); // Draws a route line
			mapsOverlays.add(routeOverlay);
		}
// set the pushpin for the rest of the landmarks
		Drawable drawable = this.getResources().getDrawable(R.drawable.map_pushpin);
		MapItemizedOverlay mapItemizedOverlay = new MapItemizedOverlay(
				drawable, this);

		List<GeoPoint> landmarks = m_shirtNavigation.getLandMarks();
		// retrieve landmarks and add them to their itemized Overlay.
		for (int index = 0; index < landmarks.size(); index++) {
			overlayItem = new OverlayItem(landmarks.get(index),"Major Location","No Description");
			mapItemizedOverlay.addOverlay(overlayItem);
		}
      // display itemized Overlay on the screen
		mapsOverlays.add(mapItemizedOverlay);
      // display the whole route based on the previously calculated min/max coordinates 
		m_mapController.zoomToSpan(Math.abs(maxLatitude - minLatitude), Math.abs(maxLongitude - minLongitude));
		GeoPoint geoPoint = new GeoPoint((maxLatitude + minLatitude)/2, (maxLongitude + minLongitude)/2);
		m_mapController.animateTo(geoPoint);
		
		wayPoints = new LocationLoader(landmarks).getWaypoints();
		nextDestination = wayPoints.remove();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		String btaddress = "";
		
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.exit:
	    	stopListening();
	        return true;
	    case R.id.bluetooth:
	    	// Opens submenu to select Bluetooth device address
	        return true;
	    // Selection of first Bluetooth device address
	    case R.id.bt1:
	    	if (item.isChecked()) item.setChecked(false);
	        else item.setChecked(true);
	    		btaddress = getString(R.string.bt1);
	    		setBluetooth(btaddress);
	    	return true;
	    // Selection of second Bluetooth device address
	    case R.id.bt2:
	    	if (item.isChecked()) item.setChecked(false);
	        else item.setChecked(true);
		    	btaddress = getString(R.string.bt2);
	    		setBluetooth(btaddress);
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	/*
	 * ---------------------------------------------------------------------------------
	 */
	
	public void timerFunction()
	{
        try{
			
			 timer.scheduleAtFixedRate(new TimerTask() 
			 {
				
				 public void run()
				 {
				
					 
					 if (currentLocation != null)
					 {
						 sender.getBearing();
					 }
				 } }
			  , 3000,3000); 
		
			 }
			
		catch (Exception e)
		{
		
		}
		
	}
	
	public void setBluetooth(String blueToothAddress)
	{
		DEVICE_ADDRESS = blueToothAddress;
		Amarino.connect(this, DEVICE_ADDRESS);
	}
	
	  protected void startListening()
	   {
	   		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	   		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); 
	   }
	   
	   // Ends GPS updates
	   protected void stopListening()
	   {
		   if (locationManager != null)
			   locationManager.removeUpdates(locationListener);
	   }
	   
	   private class LocListener implements LocationListener {
		   	
		   	public void onLocationChanged(Location loc)
		   	{
		   		//DirectionCalc direc = new DirectionCalc();
		   		//sendingClass sender = new sendingClass();
		   		
		   		currentLocation = loc;
		   		direc.updateLoc(currentLocation);
		   		//Toast.makeText(getApplicationContext(), "Location changes", Toast.LENGTH_SHORT).show();
		   		
		   		direc.updateLoc(loc);
		   		
		   		
		   	}

			public void onProviderDisabled(String provider)
			{
				//Toast.makeText(getApplicationContext(), "GPS is disabled", Toast.LENGTH_SHORT).show();
			}

			public void onProviderEnabled(String provider)
			{
				//Toast.makeText(getApplicationContext(), "GPS is enabled", Toast.LENGTH_SHORT).show();
			}

			public void onStatusChanged(String provider, int status, Bundle extras) 
			{
				// TODO Auto-generated method stub
			}
		} 
	   
	   private final SensorEventListener mListener = new SensorEventListener() {
	        public void onSensorChanged(SensorEvent event) {
	           
	            mValues = event.values;
	        	//Toast.makeText(getApplicationContext(), mValues[0] + "", Toast.LENGTH_SHORT).show();
	         //   looker = looker + mValues[0] + "\n";
	           // counter++;
	            //Toast.makeText(getApplicationContext(), Toast.LENGTH_SHORT).show();
	        
	        }

	        public void onAccuracyChanged(Sensor sensor, int accuracy) {
	        }
	    };
	    
	   

	    public class ArduinoReceiver extends BroadcastReceiver {
			

			
	    	public void onReceive(Context context, Intent intent) {
	    		String data = null;
	    		VibroClass vibro = new VibroClass();
	    		
	    		// the device address from which the data was sent, we don't need it here but to demonstrate how you retrieve it
	    		final String address = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
	    		
	    		// the type of data which is added to the intent
	    		final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
	    		
	    		// we only expect String data though, but it is better to check if really string was sent
	    		// later Amarino will support differnt data types, so far data comes always as string and
	    		// you have to parse the data to the type you have sent from Arduino, like it is shown below
	    		if (dataType == AmarinoIntent.STRING_EXTRA){
	    			data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
	    			if (data != null){
	    				//mValueTV.setText(data);
	    				try {
	    					// since we know that our string value is an int number we can parse it to an integer
	    				 currBearing = Integer.parseInt(data);
	    				// Toast.makeText(getApplicationContext(),currBearing + "", Toast.LENGTH_SHORT).show();
	    				
	    			
	    					//mGraph.addDataPoint(sensorReading);
	    				} 
	    				catch (NumberFormatException e) {
	    				 // oh data was not an integer 
	    				
	    				}
	    			} 
	    		}
	    		
	    		double distToNext = (double) currentLocation.distanceTo(nextDestination);
	    		Toast.makeText(context , distToNext + "" , Toast.LENGTH_SHORT).show();
	    		//Toast.makeText(context ,"angle is " + mValues[0] + "", Toast.LENGTH_SHORT).show();
	       		
	       		// If they are not within the radius
	       		if (distToNext > waypointRadius) 
	       		{
	       			direc.directionUpdateDriving(nextDestination, headings.get(indexHeadings));
	       		
	       			//Toast.makeText(getApplicationContext(), "trying to reach waypoint", 50000).show();
	       		}
	       		else 
	       		{	
	       			if (!wayPoints.isEmpty()) 
	       				
	       			{
	       			
	       				nextDestination = wayPoints.remove();
	       				//mpWayPoint.start();
	   
	       				indexHeadings++;
	       				sender.sendAtWaypoint();
	       				//Toast.makeText(getApplicationContext(), "at waypoint!", Toast.LENGTH_SHORT).show();
	       			}
	       			else 
	       			{
	       				//Toast.makeText(getApplicationContext(), "at FINAL DESTINATION", Toast.LENGTH_SHORT).show();
	       				sender.sendAtDestination();
	       			}
	       		}
	    	}


	    	}
}
