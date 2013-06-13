package hapticVibroPackage;

import android.location.Location;
import android.app.Activity;
import android.app.Application;
import android.content.*;
import android.widget.Toast;
import at.abraxas.amarino.AmarinoIntent;



public class DirectionCalc extends Activity{
	

	Location currentLocation = new Location("current");
	Location nextDestination = new Location("next");
	//double currBearing;
	double nextBearing;
	//ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	VibroClass vibro = new VibroClass();
	sendingClass sender1;
	 String rightleft = "";
	
	Context con;
	String DEVICE_ADDRESS;
	
	public DirectionCalc (Context context, String blueToothAddress) {
		con = context;
		  DEVICE_ADDRESS = blueToothAddress;
		  sender1 = new sendingClass(con, DEVICE_ADDRESS);
		
		 // registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
	}
	
	// Updates the user's current location
	public void updateLoc(Location loc)
	{
		currentLocation = loc;
	}
	
	// Called while the user is not yet at the waypoint

	public void directionUpdate(Location next2,  double currBearing)
	{
		nextDestination = next2;
		
		// Acquires the bearing for the shortest path to the next location
		nextBearing = currentLocation.bearingTo(nextDestination);
		//nextBearing = -90;
		
		// Toast.makeText(con, nextBearing + "", Toast.LENGTH_LONG).show();
		
		// Converts next bearing to [0, 360)
		if (nextBearing < 0)
			nextBearing = nextBearing + 360;

		//Toast.makeText(con, nextBearing + "", Toast.LENGTH_LONG).show();
		//Converts the current bearing to [0, 360)
		  if (currBearing < 0)
		  {
			  currBearing = currBearing + 360;
		  }
		  
		  // Calculates the data to send to Arduino
		 int turninfo = vibro.rightLeft(currBearing, nextBearing);
		 int frequency = vibro.vibFreqInc(currBearing, nextBearing);
	
	   				
	   	
		 
		 // Sends the data to Arduino
		sender1.sendInt(255);
		 sender1.sendDir(turninfo);
		
		 sender1.sendFreq(frequency);
		 //sender1.sendVib();
		 
	}
	
	public void directionUpdateDriving(Location next2, double currBearing)
	{
		nextDestination = next2;
		double distance = currentLocation.distanceTo(nextDestination);
		double speed = currentLocation.getSpeed();
		
		// Acquires the bearing for the shortest path to the next location
		nextBearing = currentLocation.bearingTo(nextDestination);
		//nextBearing = -90;
		
		// Toast.makeText(con, nextBearing + "", Toast.LENGTH_LONG).show();
		
		// Converts next bearing to [0, 360)
		if (nextBearing < 0)
			nextBearing = nextBearing + 360;

		//Toast.makeText(con, nextBearing + "", Toast.LENGTH_LONG).show();
		//Converts the current bearing to [0, 360)
		  if (currBearing < 0)
		  {
			  currBearing = currBearing + 360;
		  }
		  
		  // Calculates the data to send to Arduino
		 int turninfo = vibro.rightLeft(currBearing, nextBearing);
		 int frequency = vibro.drivingFreq(distance, speed);
	
	   				
	   	
		 
		 // Sends the data to Arduino
		sender1.sendInt(255);
		 sender1.sendDir(turninfo);
		
		 sender1.sendFreq(frequency);
		 //sender1.sendVib();
		 
	}
	
}
