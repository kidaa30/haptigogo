package com.hapticnavigation.shirtnavigo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
/**
 * 
 * @author Essa Haddad. Texas A&M. 
 *
 */
public class RouteOverlay extends Overlay {

	private GeoPoint m_geoPoint1;
	private GeoPoint m_geoPoint2;
	private int m_color;

	public RouteOverlay(GeoPoint geoPoint1, GeoPoint geoPoint2, int color) {
		m_geoPoint1 = geoPoint1;
		m_geoPoint2 = geoPoint2;
		m_color = color;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas,
	 * com.google.android.maps.MapView, boolean)
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		// TODO Auto-generated method stub
		super.draw(canvas, mapView, shadow);
		Projection projection = mapView.getProjection();
		Paint paint = new Paint();
		Point point = new Point();
		projection.toPixels(m_geoPoint1, point);
		paint.setColor(m_color);
		Point point2 = new Point();
		projection.toPixels(m_geoPoint2, point2);
		paint.setStrokeWidth(5);
		paint.setAlpha(120);
		canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
	}

}
