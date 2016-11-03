package com.calorieclock;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.calorieclock.datamodel.Profile;

public abstract class ProfileEditor extends Dialog 
{
	private Profile profile;
	private SharedPreferences sharedPrefs;
	private SharedPreferences.Editor prefsEditor;
	
	private RadioButton optMale, optFemale;
	private NXDatePicker birthdayDatePicker;
	private EditText heightFeetEditText, heightInchesEditText;
	private EditText startingWeightEditText;
	private EditText goalWeightEditText;    	
    private Button saveButton;
	private Spinner activityLevelSpinner;
    
	private List<String> activityLevels;
	private float[] activityLevelMultipliers = { 1.2f, 1.375f, 1.55f, 1.725f, 1.9f };
    
    public ProfileEditor(Context context)
    {
    	super(context);
        setTitle("Profile Editor");
        View v = getLayoutInflater().inflate(R.layout.dialog_profile_editor, null);
        setContentView(v);
        setCancelable(false);
        
        fetchControls();     
        init();
        
    } //End ProfileEditor(Context)

	private void init() {
		
		sharedPrefs = getContext().getSharedPreferences("CALORIE_CLOCK_PREFERENCES", Context.MODE_PRIVATE);
        prefsEditor = sharedPrefs.edit();
        
		activityLevels = new ArrayList<String>();
		activityLevels.add("Sedentary (" + activityLevelMultipliers[0] + ")");
		activityLevels.add("Low (" + activityLevelMultipliers[1] + ")");
		activityLevels.add("Moderate (" + activityLevelMultipliers[2] + ")");
		activityLevels.add("High (" + activityLevelMultipliers[3] + ")");
		activityLevels.add("Heavy (" + activityLevelMultipliers[4] + ")");
		
		//---set up activity level spinner
		
		//---create and assign an array adapter for the activity level spinner
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.getContext(), R.layout.row, R.id.text, activityLevels);		
		activityLevelSpinner.setAdapter(spinnerArrayAdapter);
		
		//create anonymous OnItemSelectedListener for activity level spinner
		activityLevelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				//TODO on item selected code here
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {/*unused*/}			
		});
    	//later will default to disabled until valid data is completely entered
    	//saveButton.setEnabled(false);
    	
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
            	if(!isValidInput()) return;
            	
            	profile = new Profile();
            	//test data passing
            	profile.startingWeight.lbs = Integer.parseInt(startingWeightEditText.getText().toString());
            	profile.goalWeight.lbs = Integer.parseInt(goalWeightEditText.getText().toString());
            	saveState();
            	
            	//close dialog
                dismiss();
                //pass profile to calling object
                onSave(profile);
            }
        });
        
	} //End init()

	private void fetchControls() {
		optMale = (RadioButton)findViewById(R.id.optMale);  
        optFemale = (RadioButton)findViewById(R.id.optFemale);
        birthdayDatePicker = (NXDatePicker)findViewById(R.id.birthdayDatePicker);
		activityLevelSpinner = (Spinner)findViewById(R.id.activityLevelSpinner);
		heightFeetEditText = (EditText)findViewById(R.id.inFeetEditText);
		heightInchesEditText = (EditText)findViewById(R.id.inInchesEditText);
        startingWeightEditText = (EditText)findViewById(R.id.startingWeightEditText);
        goalWeightEditText = (EditText)findViewById(R.id.goalWeightEditText);    
    	saveButton = (Button)findViewById(R.id.saveButton);
	}
    
    public boolean isValidInput() {
    	if(heightFeetEditText.length() == 0 ||
    	   heightInchesEditText.length() == 0 ||
    	   startingWeightEditText.length() == 0 ||
    	   goalWeightEditText.length() == 0) {
    		QuickMenu.alert(getContext(), "Error", "You must completely fill out the profile before saving.");
    		return false;
    	}
    	return true;
    }
    
    public void saveState() {
    	//true : male, false : female
    	prefsEditor.putBoolean("GENDER", optMale.isChecked());
    	prefsEditor.putInt("YEAR", birthdayDatePicker.getYear());
    	prefsEditor.putInt("MONTH", birthdayDatePicker.getMonth());
    	prefsEditor.putInt("DAY", birthdayDatePicker.getDayOfMonth());
    	prefsEditor.putInt("ACTIVITY_LEVEL", activityLevelSpinner.getSelectedItemPosition());
    	prefsEditor.putInt("HEIGHT_FEET", saveInt(heightFeetEditText));
    	prefsEditor.putInt("HEIGHT_INCHES", saveInt(heightInchesEditText));
    	prefsEditor.putInt("STARTING_WEIGHT", saveInt(startingWeightEditText));
    	prefsEditor.putInt("CURRENT_WEIGHT", saveInt(startingWeightEditText));
    	if(sharedPrefs.getInt("CURRENT_WEIGHT", 0) == 0)
    		prefsEditor.putInt("CURRENT_WEIGHT", saveInt(startingWeightEditText));
    	prefsEditor.putInt("GOAL_WEIGHT", saveInt(goalWeightEditText));
    	prefsEditor.commit();
    }
        
    public void loadState() {
    	//true : male, false : female
    	if(sharedPrefs.getBoolean("GENDER", optMale.isChecked())) optMale.setChecked(true);
    	else optFemale.setChecked(true);
    	birthdayDatePicker.updateDate(
			sharedPrefs.getInt("YEAR", birthdayDatePicker.getYear()),
			sharedPrefs.getInt("MONTH", birthdayDatePicker.getMonth()),
			sharedPrefs.getInt("DAY", birthdayDatePicker.getDayOfMonth())
    	);
    	activityLevelSpinner.setSelection(sharedPrefs.getInt("ACTIVITY_LEVEL", activityLevelSpinner.getSelectedItemPosition()));
    	heightFeetEditText.setText(loadInt("HEIGHT_FEET"));
    	heightInchesEditText.setText(loadInt("HEIGHT_INCHES"));
    	startingWeightEditText.setText(loadInt("STARTING_WEIGHT"));
    	goalWeightEditText.setText(loadInt("GOAL_WEIGHT"));
    }

    private int saveInt(EditText editText) {
    	if(editText.getText().toString().length() == 0) return 0;
    	return Integer.parseInt(editText.getText().toString());
    }
    
    private String loadInt(String key) {
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
    public abstract void onSave(Profile profile);

} //End class ProfileEditor