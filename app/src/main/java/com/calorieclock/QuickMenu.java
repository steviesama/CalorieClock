package com.calorieclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class QuickMenu {

	private static AlertDialog alert = null;
	
	private QuickMenu() {/*no instantiation*/}
	
	public static void pause() {
		if(alert != null)
			alert.dismiss();
	}
	
	public static void resume() {
		if(alert != null) {			
			alert.show();
		}
	}
	
	public static void alert(Context context, String title, String msg) {
		alert = new AlertDialog.Builder(context).create();
		
		alert.setTitle(title);
		alert.setMessage(msg);
		alert.setCancelable(false);		
		alert.setButton(AlertDialog.BUTTON_POSITIVE, "OK", 
		new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//deference alert
				QuickMenu.alert = null;
			}
		});
		
		alert.show();
	}

}
