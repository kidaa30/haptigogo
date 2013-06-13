package com.hapticnavigation.shirtnavigo;

import java.io.File;
import java.io.IOException;
import android.media.MediaPlayer;
import android.os.Environment;

/**
 * This class accesses the external storage on the phone. The function Play takes on the string ;the name of the file to be
 * played, from the SD card placed on the Audio folder manually created by the user.It
 */


public class PlayAudioAtLandmarks {
	
	String TourName;
	 private MediaPlayer m_mediaPlayer = new MediaPlayer();
	// Access the external storage directory
	private File externalStorage =  Environment.getExternalStorageDirectory();
	String path = externalStorage.getPath();
	
	//Get access to the audio files in the external storage
	private File Tours = new File(path, "Audio");
	
	String[] FileList = Tours.list();
	String finalPath = Tours.getPath();
	File m_audioFile= null;
	
	
 void Play(String name){
		m_mediaPlayer.reset();
			//m_audioFile =  new File(finalPath,FileList[0]);
		m_audioFile =  new File(finalPath,name+".mp3");
		
			try {
				m_mediaPlayer.setDataSource(m_audioFile.getPath());
				 
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				getMediaPlayer().prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			m_mediaPlayer.start();
			
		}


/**
 * @param mediaPlayer the mediaPlayer to set
 */
public void setMediaPlayer(MediaPlayer mediaPlayer) {
	this.m_mediaPlayer = mediaPlayer;
}


/**
 * @return the mediaPlayer
 */

public MediaPlayer getMediaPlayer() {
	// TODO Auto-generated method stub
	return m_mediaPlayer;
}
}
