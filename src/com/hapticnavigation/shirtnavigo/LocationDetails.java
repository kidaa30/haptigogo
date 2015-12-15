package com.hapticnavigation.shirtnavigo;
/**
 * Sets and gets landmark name and descriptions. This class groups the name and description of a landmark together.
 * @author Essa Haddad. Texas A&M.
 *
 */
public class LocationDetails {
	
	private String m_landmarkName;
	private String m_landmarkDescription;

	
	public String getLandmarkName(){
		return m_landmarkName;
	}
	
	public String getLandmarkDescription(){
		return m_landmarkDescription;
	}
	
	public void setLandmarkName(String landmarkName){
		m_landmarkName = landmarkName;
	}
	public void setLandmarkDescription(String landmarkDescription){
		m_landmarkDescription = landmarkDescription;
	}
}
