package com.hapticnavigation.shirtnavigo;

import hapticVibroPackage.DirectionCalc;
import hapticVibroPackage.LocationLoader;
import hapticVibroPackage.VibroClass;
import hapticVibroPackage.sendingClass;




import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.hapticnavigation.parser.RouteSaxParser;
import com.hapticnavigation.shirtnavigo.ShirtNavigation;

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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import at.abraxas.amarino.*;
import android.media.MediaPlayer;

/**
 * Accesses the mapsView page, adds overlays that draw the route, and sets up the itemized overlays to point out the landmarks for the tour.
 * <p>
 * TamuTourPage class handles identifying the KML file resource, parse its data and process the data to represent its content, and then 
 * matches the landmarks with their descriptions and draws a line to represent the route.
 * </p>
 * @author Essa Haddad, Kate Boxer and Sarin Regmi. Texas A&M.
 *
 */

public class TamuTourPage extends MapActivity {

	private PlayAudioAtLandmarks m_audio = new PlayAudioAtLandmarks();
	private final static boolean TOUR = true;
	private static final String TAG = "TAMU_TOUR";

	private RouteSaxParser m_routeSaxParser;
	
	private String m_tourTitle = null;
	private List<LocationDetails>  m_landmarksNames = new ArrayList<LocationDetails>();
	
	private MapView m_mapView;
	private MapController m_mapController;
	private ShirtNavigation m_shirtNavigation;
	
	LocationManager locationManager;
	LocationListener locationListener;
	Location currentLocation;
	Location nextDestination;
	Location nextLandmark;
	int positionOfLandmarks = 0;
	
	private SensorManager mSensorManager;
    private Sensor mSensor;
    float[] mValues;
    Queue<Location> wayPoints;
    Queue<Location> m_landmarks;
    String DEVICE_ADDRESS =  "00:06:66:42:22:7C";
	
	// User must be in 5 meters of the waypoint for it to be recognized
	double waypointRadius = 5;
	double distToNext;
	int currBearing;
	double nextBearing;
	boolean rightTurn;
	double turnAngle;
	int counter = 1;
  
	Timer timer = new Timer();
	VibroClass vibro = new VibroClass();
	ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	String working = "waiting";
	DirectionCalc direc;
	sendingClass sender;
	Location locer;
	String looker = "";
	String filename= "";
	
	
	
	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	 
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.googlemap);
		
		m_shirtNavigation = new ShirtNavigation();
		m_mapView = (MapView) findViewById(R.id.mapView);
		m_mapView.setBuiltInZoomControls(true);
		m_mapController = m_mapView.getController();
		//m_mapController.setZoom(16);

		  locationListener = new LocListener();
		  startListening();
		  direc = new DirectionCalc(this, DEVICE_ADDRESS);
		  mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		    mSensorManager.registerListener(mListener, mSensor,1);
		    
		/*
	     * In this section we are retrieving the KML route file's id in order to get an InputSream feed.
	     * Then we pass the input stream to the SAX Parser
	     */
		
		Bundle tourinfo = getIntent().getExtras();
		filename = tourinfo.getString("filename");
		String routeID = "com.hapticnavigation.shirtnavigo:raw/" + filename;
		
		
		int m_xmlRouteFileId = getResources().getIdentifier(routeID, null,null);
		
		InputStream inputstream = getResources().openRawResource(m_xmlRouteFileId);
			
		m_routeSaxParser = new RouteSaxParser(inputstream); 
		
		try {	
			m_shirtNavigation.setPathData(m_routeSaxParser.executeSaxParsing(), TOUR);
			if(inputstream != null)
				inputstream.close();
			}
		catch (Exception e){				// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(), "Tour file experienced an error while retrieving data! Please try Later", 50000).show();
			}
		
		getLandmarkInfo();
		drawOverlays();
		
		
		
	 	 sender = new sendingClass(this , DEVICE_ADDRESS);
    	 looker = "";
			
		Amarino.connect(this, DEVICE_ADDRESS);
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
		
		
		timerFunction();
	}
	
	@Override
	protected void onDestroy() {
		locationManager.removeUpdates(locationListener);
		this.finish();
		m_audio.getMediaPlayer().stop();
		Amarino.disconnect(this, DEVICE_ADDRESS);
		super.onDestroy();
	}


	/* (non-Javadoc)
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		locationManager.removeUpdates(locationListener);
		Amarino.disconnect(this, DEVICE_ADDRESS);
		m_audio.getMediaPlayer().pause();
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener); 
		Amarino.connect(this, DEVICE_ADDRESS);
		super.onResume();
	}
	
	public void setBluetooth(String blueToothAddress)
	{
		DEVICE_ADDRESS = blueToothAddress;
		Amarino.connect(this, DEVICE_ADDRESS);
		
	}
	


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	/*
	 * drawOverlays method is responsible for assigning Landmark names and descriptions to their landmark coordinates.
	 * The if else statement provides a safety net whenever the user violates the User manual rules.
	 */
	
	private void drawOverlays(){
		
		List<Overlay> mapsOverlays = m_mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.pushpin);
		MapItemizedOverlay mapItemizedOverlay = new MapItemizedOverlay(drawable, this);
		List<GeoPoint> coordinates = m_shirtNavigation.getGeoPoints();
		// Loads in waypoints to be used by the timerFunction, and gets first nextDestination
		wayPoints = new LocationLoader(coordinates).getWaypoints();
		nextDestination = wayPoints.remove();
		
		
		List<GeoPoint> landmarks = m_shirtNavigation.getLandMarks();
		OverlayItem overlayItem;
		if (landmarks.size() != 0){
		
		for(int index = 0; index < landmarks.size(); index++){
			
				if(landmarks.size() == m_landmarksNames.size())
				{
					overlayItem = new OverlayItem(landmarks.get(index), (m_landmarksNames.get(index)).getLandmarkName(),(m_landmarksNames.get(index)).getLandmarkDescription());
				}
				else{
					Toast.makeText(getApplicationContext(), "Landmark Titles are not equal to the number of landmarks available", 50000).show();
					if(index < m_landmarksNames.size())
						overlayItem = new OverlayItem(landmarks.get(index),(m_landmarksNames.get(index)).getLandmarkDescription(),(m_landmarksNames.get(index)).getLandmarkName());
					else
						overlayItem = new OverlayItem(landmarks.get(index),"No Description", m_tourTitle);
				}
			
				mapItemizedOverlay.addOverlay(overlayItem);
				mapsOverlays.add(mapItemizedOverlay);
				
				// Loads in waypoints to be used by the timerFunction, and gets first nextDestination
				
				m_landmarks = new LocationLoader(landmarks).getWaypoints();
				nextLandmark = m_landmarks.remove();
			}
		}else{
			Toast.makeText(this, "No landmarks assigned!", 60000).show();
		}
		
		RouteOverlay routeOverlay;
		
		Drawable startDrawable = this.getResources().getDrawable(R.drawable.pushpinstart);
		MapItemizedOverlay mapItemizedOverlay2 = new MapItemizedOverlay(startDrawable, this);
		
		Drawable finishDrawable = this.getResources().getDrawable(R.drawable.pushpinend);
		MapItemizedOverlay mapItemizedOverlay3 = new MapItemizedOverlay(finishDrawable, this);
		
		int minLatitude = Integer.MAX_VALUE;
		int maxLatitude = Integer.MIN_VALUE;
		int minLongitude = Integer.MAX_VALUE;
		int maxLongitude = Integer.MIN_VALUE;
		int latitude;
		int longitude;
		for(int index = 1; index < coordinates.size(); index++){
			if(index == 1){
				overlayItem = new OverlayItem(coordinates.get(index - 1), "Starting Point", "Start");
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
			
			if(index == (coordinates.size() - 1)){
				overlayItem = new OverlayItem(coordinates.get(index), "finishing Point", "Finish");
				mapItemizedOverlay3.addOverlay(overlayItem);
				mapsOverlays.add(mapItemizedOverlay3);	
			}
	
			routeOverlay = new RouteOverlay(coordinates.get(index - 1), coordinates.get(index), Color.rgb(128, 0, 128)); // Draws a purple line
			mapsOverlays.add(routeOverlay);
		}
		m_mapController.zoomToSpan(Math.abs(maxLatitude - minLatitude), Math.abs(maxLongitude - minLongitude));
		GeoPoint geoPoint = new GeoPoint((maxLatitude + minLatitude)/2, (maxLongitude + minLongitude)/2);
		m_mapController.animateTo(geoPoint);	
	}
	
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
			 working = " not working";
		}
		
	}
	/*
	 * 
	 */
	
	private void getLandmarkInfo(){

		m_landmarksNames = null;
		m_landmarksNames = m_shirtNavigation.getLocationsDetails();
		
		if (m_landmarksNames.size() != 0) {
		/*
		 * The last item in m_landmarksNames should be the name of the route based on user manual. Google Maps specify the route and its
		 * name at the end. We will also enforce this practice when creating custom routes using Google Earth; i.e. identify your landmarks
		 * then draw your route.
		 *NOTE:  violations to this practice will lead to false representation.
		 */	
			m_tourTitle = (m_landmarksNames.get((m_landmarksNames.size() - 1))).getLandmarkName();
			Log.d(TAG, "ROOT name: " + m_tourTitle);
		
			m_landmarksNames.remove((m_landmarksNames.size() - 1));	
			}
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
	 * ***************************************************************************
	 */
	
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
	    		//Toast.makeText(context ,"angle is " + mValues[0] + "", Toast.LENGTH_SHORT).show();
	    		Toast.makeText(context ,"Distance to next: " + distToNext + "\nWaypoint " + counter, Toast.LENGTH_SHORT).show();
	    	
	    		
	    		
	       		// If they are not within the radius
	       		if (distToNext > waypointRadius) 
	       		{
	       			double degree = mValues[0];
	       			direc.directionUpdate(nextDestination, degree);
	       			looker = looker + mValues[0] + " ";
	       			//Toast.makeText(getApplicationContext(), "trying to reach waypoint", 50000).show();
	       		}
	       		else 
	       		{
	       			
	       			if (!wayPoints.isEmpty()) 
	       			{
	       			// Audio Implementation for Tamu Tour
	    	    		if (filename.equals("tamutour")){
	    	    			double distToLandmark = (double) nextDestination.distanceTo(nextLandmark);
	    	    			Toast.makeText(context ,"Distance to next: " + distToLandmark + "\nWaypoint " + positionOfLandmarks, Toast.LENGTH_SHORT).show();
	    	    		if (distToLandmark<15 && (!m_landmarks.isEmpty()))
	       				{
	    	    			Toast.makeText(getApplicationContext(), m_landmarksNames.get(positionOfLandmarks).getLandmarkName()+" reached", 50000).show();
	    	    			m_audio.Play(m_landmarksNames.get(positionOfLandmarks).getLandmarkName());
	       					positionOfLandmarks++;
	       					nextLandmark = m_landmarks.remove();
	       				}
	    	    		}
	       				
	       				nextDestination = wayPoints.remove();
	       				//mpWayPoint.start();
	       				counter++;
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

	
