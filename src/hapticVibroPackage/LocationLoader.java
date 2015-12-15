package hapticVibroPackage;

import android.location.Location;
import java.util.*;

import com.google.android.maps.GeoPoint;

public class LocationLoader {
	
	Queue<Location> wayPoints;
	
	public LocationLoader(List<GeoPoint> geo)
	{
		wayPoints = new LinkedList<Location>();
		
		for (int i = 0; i < geo.size(); i++)
		{
			GeoPoint geoHolder = geo.get(i);
			Location adder = new Location("adder");
			adder.setLatitude(geoHolder.getLatitudeE6()/1E6);
			adder.setLongitude(geoHolder.getLongitudeE6()/1E6);
			
			wayPoints.add(adder);
			
		}
		
	}
	
	public Queue<Location> getWaypoints()
	{
		return wayPoints;
	}

}
