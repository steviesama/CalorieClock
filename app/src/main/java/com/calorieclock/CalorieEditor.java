package com.calorieclock;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import com.androidquery.AQuery;
import com.calorieclock.datamodel.FoodEntry;

public abstract class CalorieEditor extends Dialog
{
	private FoodEntry foodEntry;
	private SharedPreferences sharedPrefs;
	private SharedPreferences.Editor prefsEditor;
    private AQuery aq;
	
    //---
    private TimePicker entryTimePicker;
    
    public CalorieEditor(Context context)
    {
    	super(context);
        setTitle("Calorie Entry");
        View v = getLayoutInflater().inflate(R.layout.calorie_entry, null);
        setContentView(v);
        setCancelable(false);
        
        aq = new AQuery(v);
		
        //--- initialize     
        sharedPrefs = getContext().getSharedPreferences("CALORIE_CLOCK_PREFERENCES", Context.MODE_PRIVATE);
        prefsEditor = sharedPrefs.edit();
		
        //---get controls
        entryTimePicker = (TimePicker)findViewById(R.id.foodEntryTimePicker);        
        
    	//later will default to disabled until valid data is completely entered
    	//saveButton.setEnabled(false);
		
        aq.id(R.id.cancelButton).clicked(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				dismiss();
				//reflect visibility persistence in shared preferences
				prefsEditor.putBoolean("CALORIE_EDITOR_SHOWING", false);
				prefsEditor.commit();
			}
		});
        
        aq.id(R.id.saveButton).clicked(new View.OnClickListener() {			
			@Override
		    public void onClick(View v) {
		    	if(!isValidInput()) return;
		    	
		    	storeFoodEntry();
		    	
		    	//close dialog
		        dismiss();
		        //reflect visibility persistence in shared preferences
				prefsEditor.putBoolean("CALORIE_EDITOR_SHOWING", false);
				prefsEditor.commit();
		        //pass profile to calling object
		        onSave(foodEntry);
		    }
		});
        
    } //End ProfileEditor(Context)
        
    public boolean isValidInput() {
    	if(aq.id(R.id.calorieEntryEditText).getText().length() == 0) {
    		QuickMenu.alert(getContext(), "Error", "You must enter calories.");
    		return false;
    	}
    	return true;
    }
    
    private void storeFoodEntry() {
    	foodEntry = new FoodEntry();
    	foodEntry.setCalories(Integer.parseInt(aq.id(R.id.calorieEntryEditText).getText().toString()));
    	Time time = new Time();
    	time.setToNow();
    	time.set(0, entryTimePicker.getCurrentMinute(), entryTimePicker.getCurrentHour(),
    			 time.monthDay, time.month, time.year);
    	foodEntry.setEntryTime(time);
    	foodEntry.setNote(aq.id(R.id.foodEntryNoteEditText).getText().toString());
    }
    
    public void saveState() {
    	QuickMenu.pause();
    	prefsEditor.putInt("CALORIE_ENTRY", saveInt(aq.id(R.id.calorieEntryEditText).getEditText()));
    	prefsEditor.putInt("ENTRY_HOUR", entryTimePicker.getCurrentHour());
    	prefsEditor.putInt("ENTRY_MINUTE", entryTimePicker.getCurrentMinute());
    	prefsEditor.putString("ENTRY_NOTE", aq.id(R.id.foodEntryNoteEditText).getText().toString());
    	prefsEditor.commit();
    }
        
    public void loadState() {
 
		QuickMenu.resume();    	
    	aq.id(R.id.calorieEntryEditText).text(loadIntString("CALORIE_ENTRY"));
    	entryTimePicker.setCurrentHour(loadInt("ENTRY_HOUR"));
    	entryTimePicker.setCurrentMinute(loadInt("ENTRY_MINUTE"));
    	aq.id(R.id.foodEntryNoteEditText).text(sharedPrefs.getString("ENTRY_NOTE", ""));
    }

    private int saveInt(EditText editText) {
    	if(editText.getText().toString().length() == 0) return 0;
    	return Integer.parseInt(editText.getText().toString());
    }
    
    private int loadInt(String key) {
    	return sharedPrefs.getInt(key, 0);
    }
    
    private String loadIntString(String key) {
    	int num = sharedPrefs.getInt(key, 0);
    	if(num == 0) return "";
    	else return Integer.toString(num);
    }
    
    /**
     * When instanced onSave() should be implemented in order to specify
     * actions on save.
     * @param profile A reference to the profile that was created in
     * the editor.
     */
    public abstract void onSave(FoodEntry foodEntry);

} //End class CalorieEditor