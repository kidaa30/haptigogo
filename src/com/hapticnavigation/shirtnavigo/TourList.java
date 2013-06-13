package com.hapticnavigation.shirtnavigo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.hapticnavigation.parser.RouteSaxParser;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Creates a ListView of the static tours available as part of the application. Each item in the list, when
 * clicked, starts intent that begins the selected tour. This will start GPS updates, display a map using Google Maps,
 * and cause the haptic signals to begin.
 * 
 * @author Alex Reynolds.
 */

public class TourList extends ListActivity{

	private RouteSaxParser m_routeSaxParser;

	// Access the external storage directory and get its path
	private File externalStorage = Environment.getExternalStorageDirectory();
	String path = externalStorage.getPath();

	// Get access to the user generated folder called maps and list the files in
	// an array
	private File maps = new File(path, "maps");
	String[] FileList = maps.list();

	// Input stream to load the desired file
	private InputStream inputStream = null;
	private File KmlFile = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  // The built in tours list
	  String[] statictours = getResources().getStringArray(R.array.tours_array);
	  
	  // Keepts track of the length of the built in tours list
	  int stattourcount = statictours.length;
	  
	  // Combines the built in tours and the imported tours to one list
	  String[] tours = concat(statictours, FileList);
	  
	  // The position of the imported tour in the imported tour file list
	  //   * For use after the strings have been concatenated *
	  final int importfilepos = tours.length - stattourcount - 1;
	  
	  setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, tours));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {

	    	//TAMU Tour
	    	if (position == 0){
	    		Intent tamutour = new Intent("com.hapticnavigation.shirtnavigo.TAMUTOUR");
	    		tamutour.putExtra("filename", "tamutour");
	    		startActivity(tamutour);
	    	}
	    	//Greek Lawn Tour
	    	else if (position == 1){
	    		Intent lawntour = new Intent("com.hapticnavigation.shirtnavigo.TAMUTOUR");
	    		lawntour.putExtra("filename", "awareness");
	    		startActivity(lawntour);
	    	}
	    	//Tradpad Parking Tour
	    	else if (position == 2){
	    		Intent parkingtour = new Intent("com.hapticnavigation.shirtnavigo.TAMUTOUR");
	    		parkingtour.putExtra("filename", "parkingtour");
	    		startActivity(parkingtour);
	    	}
	    	// Northgate block tour
	    	else if (position == 3){
	    		Intent blocktour = new Intent("com.hapticnavigation.shirtnavigo.TAMUTOUR");
	    		blocktour.putExtra("filename", "tradblock");
	    		startActivity(blocktour);
	    	}
	    	else if (position == 4){
	    		Intent blocktour = new Intent("com.hapticnavigation.shirtnavigo.TAMUTOUR");
	    		blocktour.putExtra("filename", "lawntour");
	    		startActivity(blocktour);
	    	}
	    	// Deals with files imported from SD card 
	    	else {
	    		String finalPath = maps.getPath();
				
				// access the file being clicked
				KmlFile = new File(finalPath, FileList[importfilepos]);

				// Error checking and response
				try {
					// throw the file to the input stream
					inputStream = new BufferedInputStream(new FileInputStream(
							KmlFile));
					Toast.makeText(getApplicationContext(),
							"File Upload Completed." + KmlFile.getName(), 50000)
							.show();
					m_routeSaxParser = new RouteSaxParser(inputStream);

					Intent tourIntent = new Intent(
							"com.hapticnavigation.shirtnavigo.TOURPAGE");
					tourIntent.putStringArrayListExtra("kmlData",
							(ArrayList<String>) m_routeSaxParser
									.executeSaxParsing());
					startActivity(tourIntent);

				} catch (FileNotFoundException e) {
					Toast.makeText(getApplicationContext(),
							"File not found!", 50000).show();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(getApplicationContext(),
							"File not found!", 50000).show();
				} catch (SAXException e) {
					Toast.makeText(
							getApplicationContext(),
							"HaptiGo couldn't process the file. Please try a different file.",
							50000).show();
					// TODO Auto-generated catch block
				} catch (ParserConfigurationException e) {
					Toast.makeText(
							getApplicationContext(),
							"HaptiGo couldn't parse the file. Please try a different file.",
							50000).show();
					// TODO Auto-generated catch block
				}
	    	}
	    }
	  });
	  
	}
	
	// Concatenates the two string arrays (static tours and imported tours)
	public String[] concat(String[] first, String[] second) {
	    List<String> both = new ArrayList<String>(first.length + second.length);
	    Collections.addAll(both, first);
	    Collections.addAll(both, second);
	    return both.toArray(new String[] {});
	}
	
}
	

