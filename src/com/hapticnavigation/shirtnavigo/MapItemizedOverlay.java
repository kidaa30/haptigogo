package com.hapticnavigation.shirtnavigo;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
/**
 * This class is responsible for collecting overlays that have a common drawable in order to display them 
 * all at once when needed. It provides an easy way to manage overlays.
 * @author Essa Haddad, Sketch Recognition Lab, Texas A&M.
 *
 */
public class MapItemizedOverlay extends ItemizedOverlay {

	private List<OverlayItem> m_overlays = new ArrayList<OverlayItem>();
	private Context m_context;

	public MapItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		m_context = context;
		// TODO Auto-generated constructor stub
	}

	public void addOverlay(OverlayItem overlay) {
		m_overlays.add(overlay);
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		// TODO Auto-generated method stub
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return m_overlays.size();
	}

	/*
	 * displays landmark name in the title and a description for the landmark at the bottom. An Ok button closes the dialog box.
	 * 
	 * @see com.google.android.maps.ItemizedOverlay#onTap(int)
	 */
	@Override
	protected boolean onTap(int index) {
		// TODO Auto-generated method stub
		OverlayItem item = m_overlays.get(index);
		AlertDialog.Builder dialog = new AlertDialog.Builder(m_context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.setNegativeButton("Continue", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		dialog.show();
		return true;
	}

}
