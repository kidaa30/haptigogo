package hapticVibroPackage;

import at.abraxas.amarino.Amarino;
import android.content.*;
import android.view.View;
import android.widget.Toast;
import android.app.*;


public class sendingClass extends Application{
	 Context con;
	 String DEVICE_ADDRESS;
	 
	 public sendingClass (Context context, String BlueToothAddress)
	 {
		 con = context;
		 DEVICE_ADDRESS = BlueToothAddress;
	 }
	 
	 

	
	/*
	 * SENDS WHICH VIBRATOR IS VIBRATING
	 */

	public void sendDir(int vib)

	{
	
		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 'd', vib);
	}
	
	/*
	 * SENDS INTENSITY
	 */
	
	/*
	 * INPUTS: CURRENT ANGLE BEARING, NEXT DESTINATION  ANGLE BEARING, TURNING ANGLE
	 * RETURNS: INT 
	 * 			1 = left
	 * 			3 = right
	 * 			2 = straight
	 */
	public void sendInt(int intensity)
	{

		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 'i', intensity);
	}
	
	/*
	 * SEND FREQ. INBETWEEN -- "DELAY"
	 */
	public void sendFreq(int freq)
	{
		
		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 'f', freq);
	}
	
	/*
	 * SENDS SIGNAL TO AMARINO TO TELL THAT WE ARE AT WAYPOINT
	 */
	public void sendAtWaypoint()
	{
	
		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 'w', 0);
	}
	
	/*
	 * SENDS SIGNAL TO TELL THAT WE HAVE MADE IT TO FINAL DESTINATION!
	 */
	public void sendAtDestination()
	{
	
		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 'e', 0);
	}
	
	/* 
	 * SIGNAL THAT TELLS ALL THE VIBRATORS TO STOP
	 */
	public void sendStop()

	{
		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 's', 0);
	}
	
	public void sendVib()
	{
	
		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 'm', 0);
		//Toast.makeText(con , "Send Vibrations", Toast.LENGTH_SHORT).show();

	}
	
	public void getBearing()
	{
		Amarino.sendDataToArduino(con, DEVICE_ADDRESS, 'b', 0);
	}
}
