package hapticVibroPackage;

import at.abraxas.amarino.*;

import android.app.Activity;
import android.content.*;
import android.app.*;
import android.widget.*;
import android.location.Location;

public class VibroClass{
	
	
	// Calculates the turn angle
	 public double getTurnAngle(double nextBearing, double currBearing)
	   {
		 double difference;
		 double turnAngle;
		   
		   difference = nextBearing - currBearing;
		   difference = Math.abs(difference);
		   
		   if (difference > 180)
			   turnAngle = 360 - difference;
		   else
			   turnAngle = difference;
		  
		   return turnAngle;
		} 
	 
	/*
	 * INPUTS: CURRENT ANGLE BEARING, NEXT DESTINATION  ANGLE BEARING, TURNING ANGLE
	 * RETURNS: INT 
	 * 			1 = left
	 * 			3 = right
	 * 			2 = straight
	 */

	public  int rightLeft(double currBearing, double nextBearing)
	   {
		double turnAngle = getTurnAngle(currBearing, nextBearing);
		double absTurnAngle = Math.abs(turnAngle);
		int rightLeft;
		
		// Gives a 10 degrees margin of error for turn angle
		if (absTurnAngle < 20)
		{
			// Go straight
			rightLeft = 2;
		}
		else if (nextBearing > currBearing)
		{
	   	// If a 180 degree right turn will not bring the user to/pas the desired bearing
	   		if (currBearing + 180 < nextBearing)
	   			// Turn left
	   			rightLeft = 1;
	   		else
	   			// Turn right
	   			rightLeft = 3;
	   		
	   	// If a 180 degree left turn will not bring the user to/past the desired bearing
	   	}
		else if (currBearing - 180 > nextBearing)
		{
	   		// Turn right
	   		rightLeft = 3;	
	   	}
		else
		{
	   		// Turn left
	   		rightLeft = 1;
	   	}
		
	   	return rightLeft;
		}
	
	/* Vibration Frequency (Increasing)
	 * Sets a frequency of vibrations based on the desired turn angle
	 * The closer the user gets to the desired angle, the MORE frequent the vibrations
	 * (Vibrations will cease once desired bearing range is reached)
	 */
	public int vibFreqInc (double currBearing, double nextBearing)
	{
		/* Delay between vibrations in milliseconds
		   Smaller freq = more frequent vibrations */
		double turnAngle = getTurnAngle(currBearing, nextBearing);
		turnAngle = Math.abs(turnAngle);
		int freq;
		
		// **Could address turnAngle <= 10 in place where method is called
		if (turnAngle <= 20)
			freq = 1000;
		else if (turnAngle > 20 && turnAngle <= 30)
			freq = 200;
		else if (turnAngle > 30 && turnAngle <= 60)
			freq = 350;
		else if (turnAngle > 60 && turnAngle <= 90)
			freq = 500;
		else if (turnAngle > 90 && turnAngle <= 135)
			freq = 750;
		else
			freq = 1000;
		
		return freq;
		
	}
	
	public int drivingFreq (double distance, double speed)
	{
		/* Delay between vibrations in milliseconds
		   Smaller freq = more frequent vibrations */
		
		double secondRatio = distance / speed;
		int freq;
		
		if (secondRatio <= 15)
		{
			freq = 1000;
		}
		else if (secondRatio <= 30)
		{
			freq = 750;
		}
		else if (secondRatio <= 45)
		{
			freq = 500;
		}
		else if (secondRatio <= 60)
		{
			freq = 350;
		}
		else
		{
			freq = 0;
		}
		
		return freq;
		
	}

}
