package com.hapticnavigation.shirtnavigo;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.util.Log;

import com.google.android.maps.GeoPoint;

/**
 * Retrieves and tokenizes data in order to generate the right datatypes that should represent the 
 * information passed to it as a List of Strings. 
 *@author Essa Haddad
 * 
 *@version %I%, %G%
 *
 @since 1.0
 */
public class ShirtNavigation {
	
	private static final int OBJECT_COORDINATES = 0;
	private static final int OBJECT_HEADINGS = 1;
	private static final int OBJECT_LANDMARKS = 2;
	private static final int OBJECT_LANDMARKS_INFO = 3;
	
	private List<GeoPoint> m_geoPoints;
	private List<Double> m_headings;
	private List<GeoPoint> m_landmarks;
	private List<LocationDetails> m_namesAndDescriptions;
	private StringTokenizer m_stringTokenizer;
	private static final String TAG = "ShirtNAV";
	/**
	 * Initializes all the necessary private class members.
	 */
	
	public ShirtNavigation(){
		m_geoPoints = new ArrayList<GeoPoint>();
		m_headings = new ArrayList<Double>();
		m_landmarks = new ArrayList<GeoPoint>();
		m_namesAndDescriptions = new ArrayList<LocationDetails>();
		
	}
	/**
	 * 
	 * @return Route Coordinates that represents a tour or driving directions.
	 */
	
	public List<GeoPoint> getGeoPoints(){
		return m_geoPoints;
	}
	/**
	 * @param geoPointIndex selects a specific route coordinate in a route
	 * @return Single GeoPoint
	 */
	
	public GeoPoint getGeoPoint(int geoPointIndex){
		return m_geoPoints.get(geoPointIndex);
		
	}
	/**
	 * 
	 * @return List of all the landmarks.
	 */

	public List<GeoPoint> getLandMarks() {
		
		 return m_landmarks;
	}
	/**
	 * 
	 * @return List of landmark names and descriptions. 
	 */
	
	public List<LocationDetails> getLocationsDetails(){
		return m_namesAndDescriptions;
	}
	
	public List<Double> getHeading()
	{
		return m_headings;
	}
	
	public void setPathData(List<String> parsedKmlData, boolean tour){
		/*
		 * Start extracting route coordinates
		 */
		 String extractedList = (parsedKmlData.get(OBJECT_COORDINATES)).toString();
		m_stringTokenizer = new StringTokenizer(extractedList, "\n, ");
		int i = 0;  // This is a temporary counter meant to strip out the altitude variable location. HaptiGo does not use altitude for its functionality
		double x = 0,y = 0; // x stores the latitude, and y stores the longitude; they are represented as long, lat in the KML file.
		while(m_stringTokenizer.hasMoreTokens()){
			++i;
			if(i == 1){
				y = Double.parseDouble(m_stringTokenizer.nextToken());
			}
			else if(i == 2){
				x = Double.parseDouble(m_stringTokenizer.nextToken());
				Log.v(TAG, String.valueOf(x) + " , " + String.valueOf(y));
				m_geoPoints.add(new GeoPoint((int) (x * 1E6), (int) (y * 1E6)));
			}
			else if(i == 3){ // disregard the altitude
				m_stringTokenizer.nextToken();
				i = 0;
			}
		}
		/*
		 * Starts extracting the heading values 
		 */
		extractedList = (parsedKmlData.get(OBJECT_HEADINGS)).toString();
	    m_stringTokenizer = new StringTokenizer(extractedList, "\n, ");
		i = 0;
		while(m_stringTokenizer.hasMoreTokens()){
			m_headings.add(Double.parseDouble(m_stringTokenizer.nextToken()));
			Log.v(TAG, "Heading: " + m_headings.get(i));
			++i;
		}
		/*
		 * starts extracting the landmarks coordinates. This part is very similar the route coordinate extraction at
		 * the beginning of this method.
		 */
		extractedList = (parsedKmlData.get(OBJECT_LANDMARKS)).toString();
		m_stringTokenizer = new StringTokenizer(extractedList, ", ");
		i = 0;// reset the temporary counter meant to strip out the altitude variable location. HaptiGo does not use altitude for its functionality
		// x stores the latitude, and y stores the longitude; they are represented as long, lat in the KML file.
		x = 0;
		y = 0;
		while(m_stringTokenizer.hasMoreTokens()){
			++i;
			if(i == 1){
				y = Double.parseDouble(m_stringTokenizer.nextToken());
			}
			else if(i == 2){
				x = Double.parseDouble(m_stringTokenizer.nextToken());
				Log.v(TAG,"landmark: " + String.valueOf(x) + " , " + String.valueOf(y));
				m_landmarks.add(new GeoPoint((int) (x * 1E6), (int) (y * 1E6)));
			}
			else if(i == 3){  
				m_stringTokenizer.nextToken();
				i = 0;
			}
		}
		/*
		 * This section is responsible for extracting the name of the landmark and its description from the list<String>.
		 * The CoordinateExtractor class utilizes a tab to separate landmark information and for each landmark it provides a 
		 * landmark name and description. Reference the Coordinate extractor for more information.
		 * 
		 * NOTE: It's important to note that at the current moment the user is not permitted to use special characters like
		 * {', _, etc} since the KML file will change them to HTML tags. Make sure that no special characters were used in the name and
		 * description tags in the KML file
		 * 
		 */
		if(tour){
			extractedList = (parsedKmlData.get(OBJECT_LANDMARKS_INFO)).toString();
			m_stringTokenizer = new StringTokenizer(extractedList, "\t");
			/*
			 * LocationDetails is used to combine each location's name with it's description.
			 */
			LocationDetails locationDetails;
		
			while(m_stringTokenizer.hasMoreTokens()){
				locationDetails = new LocationDetails();
				StringTokenizer stringTokenizer1 = new StringTokenizer(m_stringTokenizer.nextToken() , "|");

				while(stringTokenizer1.hasMoreTokens()){
					locationDetails.setLandmarkName(stringTokenizer1.nextToken());
					locationDetails.setLandmarkDescription(stringTokenizer1.nextToken());
					m_namesAndDescriptions.add(locationDetails);
				}
			}
			for(LocationDetails ld : m_namesAndDescriptions){
				Log.v(TAG,"LOOP: " + ld.getLandmarkName());
				Log.v(TAG,"LOOP: " + ld.getLandmarkDescription());
			}
		}
	}
}
