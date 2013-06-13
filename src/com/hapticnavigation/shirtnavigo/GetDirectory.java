package com.hapticnavigation.shirtnavigo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
import android.widget.Toast;

/**
 * Creates a list view of the KML files stored in the user created maps folder in the external storage device.
 * Allows user to choose the file from the GUI to upload, and then sends it to the input stream to be read by the parser
 * 
 * ListActivity extended to create a UI with list of files that permits the click interface.
 * @author Sarin Regmi.
 */

public class GetDirectory extends ListActivity {

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
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		try {
			setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,FileList));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			Toast.makeText(getApplicationContext(), "Unable to access file list.",50000).show();
		}

		// Show the list of the files in the maps directory
		ListView list = getListView();
		list.setTextFilterEnabled(true);

		// Enable the click listener
		list.setOnItemClickListener(new OnItemClickListener() {

			// Work with the file being clicked
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				String finalPath = maps.getPath();
				
				// access the file being clicked
				KmlFile = new File(finalPath, FileList[arg2]);

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
		});

	}

}