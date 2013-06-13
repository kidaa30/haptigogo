package com.hapticnavigation.parser;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
/**
 * This class is the Content Handler class for
 * {@link RouteSaxParser}. 
 * <p>The overridden methods in this class are tailored to extracting 4 major components from a KML generated file from Google Earth
 * and/or Goole Maps:
 * <ul>
 * 	<li> Route Coordinates</li>
 * 	<li> Headings</li>
 *  <li> Landmarks</li>
 *  <li> Landmarks descriptions</li>
 * </ul>
 * </p>
 *@class RouteSaxParser 
 * @author Essa Haddad
 * 
 */
public class CoordinatesExtractor extends DefaultHandler{
	
	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#ignorableWhitespace(char[], int, int)
	 */
	private final static String TAG = "EXTRACTOR";
	private final static String NO_DESCRIPTION = "No Description";
	
	private StringBuffer m_stringBuffer = null;
	private String m_routeCoordinates = null;
	private StringBuffer m_landmarkBuffer = null;
	private String m_landmarkCoordinates = null;
	private StringBuffer m_stringHeading = null;
	private String m_routeHeadings = null;
	private StringBuffer m_landmarkNameBuffer = null;
	private String m_landmarkName = null;
	private List<String> m_parsedKmlData = null;
	private boolean m_placemark = false;
	private boolean m_name = false;
	private boolean m_description = false;
	private boolean m_point = false;
	private boolean m_coordinates = false;
	private boolean m_heading = false;
	private boolean m_routePoints = false;
	private boolean m_descriptionExists = false;
	
	
	
	/**
	 * 
	 * @return <code>List<String></code> that holds a <code>String</code> of route coordinates, headings, landmarks, and finally landmark names and their descriptions.
	 */
	public List<String> getParsedData(){
		return m_parsedKmlData;
	}
	
	@Override
	public void characters(char[] text, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		
		if(m_coordinates && m_point){
			m_landmarkBuffer.append(text, start, length);
			m_landmarkBuffer.append(" ");
		}
		else if(m_heading){
			m_stringHeading.append(text, start, length);
			m_stringHeading.append(",");
		}
		else if (m_coordinates && m_routePoints){
			m_stringBuffer.append(text, start, length);
		}
		else if(m_placemark && (m_name || m_description)){
			if(m_name){
				if(length == 1 && (text[0] == '\'' || text[0] == '&' || text[0] == '"')){
					Log.d(TAG, "VVV: " + text[0]);
					m_landmarkNameBuffer.deleteCharAt(m_landmarkNameBuffer.length() - 1);
					m_landmarkNameBuffer.append(text, start, length);
					
				}else{
					m_landmarkNameBuffer.append(text, start, length);
					m_landmarkNameBuffer.append("|");
				}
				
			}
			else if(m_description){
				if(length == 1 && (text[0] == '\'' || text[0] == '&' || text[0] == '"')){
					Log.d(TAG, "VVV: " + text[0]);
					m_landmarkNameBuffer.deleteCharAt(m_landmarkNameBuffer.length() - 1);
					m_landmarkNameBuffer.append(text, start, length);
					}
				else{
					m_landmarkNameBuffer.append(text, start, length);
					m_landmarkNameBuffer.append("\t");
				}
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		m_routeCoordinates = m_stringBuffer.toString().trim();
		m_landmarkCoordinates = m_landmarkBuffer.toString().trim();
		m_landmarkName = m_landmarkNameBuffer.toString().trim();
		m_routeHeadings = m_stringHeading.toString().trim(); 
		Log.v(TAG, m_landmarkName);
		
		m_parsedKmlData.add(m_routeCoordinates);
		m_parsedKmlData.add(m_routeHeadings);
		m_parsedKmlData.add(m_landmarkCoordinates);
		m_parsedKmlData.add(m_landmarkName);

	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		if (localName.equals("coordinates")){
			m_coordinates = false;
		}
		else if(localName.equals("Point")){
			m_point = false;
		}
		else if(localName.equals("heading")){
			m_heading = false;
		}
		else if(localName.equals("LineString")){
			m_routePoints = false;
		}
		else if(localName.equals("Placemark")){
			m_placemark = false;
			if(!m_descriptionExists){
				m_landmarkNameBuffer.append(NO_DESCRIPTION);
				m_landmarkNameBuffer.append("\t");
				
			}
			else{
				m_descriptionExists = false;
			}
				
		}
		else if(localName.equals("name")){
			m_name= false;
		}
		else if(localName.equals("description")){
			m_description= false;
		}
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		m_stringBuffer = new StringBuffer();
		m_landmarkBuffer = new StringBuffer();
		m_stringHeading = new StringBuffer();
		m_landmarkNameBuffer = new StringBuffer();
		m_parsedKmlData = new ArrayList<String>();
	}
	

	@Override
	public void startElement(String namespaceURI, String localName, String qName,
			Attributes atts) throws SAXException {
		// TODO Auto-generated method stub
		if(localName.equals("heading")){
			m_heading = true;
		}
		else if(localName.equals("Point")){
		    m_point = true;
		}
		else if(localName.equals("coordinates")){
			m_coordinates = true;
		}
		else if(localName.equals("LineString")){
			m_routePoints = true;
		}
		else if(localName.equals("Placemark")){
			m_placemark = true;
		}
		else if(localName.equals("name")){
			m_name= true;
		}
		else if(localName.equals("description")){
			m_description = true;
			m_descriptionExists = true;
		}
	}
	
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.ignorableWhitespace(ch, start, length);
	}


	
}
