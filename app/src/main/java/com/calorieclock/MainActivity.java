package com.calorieclock;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.calorieclock.datamodel.FoodEntry;
import com.calorieclock.datamodel.Profile;
import com.calorieclock.datamodel.Profile.BasalMetabolicRate;
import com.calorieclock.datamodel.Profile.Gender;

public class MainActivity extends Activity {

	private SharedPreferences sharedPrefs;
	private SharedPreferences.Editor prefsEditor;
	private Timer calorieTimer;
	private Calendar cal;
	private Calendar lastDate = null;
	private AQuery aq;
	
	private int count = 0;
	
	private File file;
	
	private final String PROFILE_EXISTS = "PROFILE_EXISTS";
	private final String APP_RUNNING = "APP_RUNNING";
	
	private Profile profile;
	private BasalMetabolicRate currentBmr;
	private BasalMetabolicRate goalBmr;
	private double activeBmr = 0.0;
	
	private CalorieEditor calorieEditor;
	private ProfileEditor profileEditor;	
	private AlertDialog alert;
	private Time time = new Time(Time.getCurrentTimezone());
	private List<String> targetWeightArray;
	private List<String> activityLevels;
	//harris-benedict activity level basal metabolic rate multipliers
	private final float[] activityLevelMultipliers = { 1.2f, 1.375f, 1.55f, 1.725f, 1.9f };
	
	//---controls
	private EditText caloriesAvailableEditText;
	private Spinner targetWeightSpinner;
	private Spinner activityLevelSpinner;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		aq = new AQuery(this);
		
		//hotfix
		//saveUiPreferences();
		
//		file = new File(getFilesDir(), "test.data");
		
//		try {
//			String str = "<Casi TEST>";
//			PrintWriter pWriter = new PrintWriter(file);			
//			pWriter.write(str);
//			pWriter.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//				
//		try {
//			Scanner scanner = new Scanner(file);		
//			String casi = scanner.nextLine();
//			QuickMenu.alert(this, "SUCCESS!", casi);
//			scanner.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}

		//---initialize
		
		sharedPrefs = this.getSharedPreferences("CALORIE_CLOCK_PREFERENCES", Context.MODE_PRIVATE);
		prefsEditor = sharedPrefs.edit();
		
		cal = lastDate = Calendar.getInstance();
		
		targetWeightArray = new ArrayList<String>();
		targetWeightArray.add("Current Weight (" + sharedPrefs.getInt("CURRENT_WEIGHT", 0) + ")");
		targetWeightArray.add("Goal Weight (" + sharedPrefs.getInt("GOAL_WEIGHT", 0) + ")");
		time.setToNow();
		
		activityLevels = new ArrayList<String>();
		activityLevels.add("Sedentary (" + activityLevelMultipliers[0] + ")");
		activityLevels.add("Low (" + activityLevelMultipliers[1] + ")");
		activityLevels.add("Moderate (" + activityLevelMultipliers[2] + ")");
		activityLevels.add("High (" + activityLevelMultipliers[3] + ")");
		activityLevels.add("Heavy (" + activityLevelMultipliers[4] + ")");
		
		//---get controls
		caloriesAvailableEditText = (EditText)findViewById(R.id.caloriesAvailableEditText);
		targetWeightSpinner = (Spinner)findViewById(R.id.targetWeightSpinner);
		activityLevelSpinner = (Spinner)findViewById(R.id.activityLevelSpinner);
		
		caloriesAvailableEditText.setText(formatTime(time));			
		
		//---set up target weight spinner
		
		//---create and assign an array adapter for the target weight spinner
		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.row, R.id.text, targetWeightArray);		
		targetWeightSpinner.setAdapter(spinnerArrayAdapter);
		//create anonymous OnItemSelectedListener for target weight spinner
		targetWeightSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				calculateActiveBMR();
				updateUi();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {/*unused*/}			
		});
		targetWeightSpinner.setSelection(0);
		
		//---set up activity level spinner
		
		//---create and assign an array adapter for the activity level spinner
		spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.row, R.id.text, activityLevels);		
		activityLevelSpinner.setAdapter(spinnerArrayAdapter);
		
		if(sharedPrefs.getBoolean("ALERT_SHOWING", true)) {			
		
			//---setup alert dialog
			alert = new AlertDialog.Builder(this).create();
			
			alert.setTitle("Create Profile");
			alert.setMessage("You need to create a profile in order to use Calorie Clock.");
			alert.setCancelable(false);		
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "Create", 
			new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {
					profileEditor = new ProfileEditor(MainActivity.this) {
						@Override
						public void onSave(Profile profile) {
							//store a reference to the created profile
							MainActivity.this.profile = profile;
							prefsEditor.putBoolean("PROFILE_EDITOR_SHOWING", false);
							prefsEditor.putBoolean(PROFILE_EXISTS, true);
							prefsEditor.commit();
							targetWeightArray.set(0, "Current Weight (" + sharedPrefs.getInt("CURRENT_WEIGHT", 0) + ")");
							targetWeightArray.set(1, "Goal Weight (" + sharedPrefs.getInt("GOAL_WEIGHT", 0) + ")");
							ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.row, R.id.text, targetWeightArray);		
							targetWeightSpinner.setAdapter(spinnerArrayAdapter);
							activityLevelSpinner.setSelection(sharedPrefs.getInt("ACTIVITY_LEVEL", 0));
							init();
							//invalidate
							profileEditor = null;
						}
					};
					profileEditor.loadState();
					prefsEditor.putBoolean("PROFILE_EDITOR_SHOWING", true);
					profileEditor.show();
					//invalidate
					alert = null;
					prefsEditor.putBoolean("ALERT_SHOWING", false);
					prefsEditor.commit();
				}
			});
			
			alert.show();
		
		} //End if(profileExists == false)
		else if(sharedPrefs.getBoolean("PROFILE_EDITOR_SHOWING", false)) {
			profileEditor = new ProfileEditor(MainActivity.this) {
				@Override
				public void onSave(Profile profile) {
					//store a reference to the created profile
					MainActivity.this.profile = profile;
					prefsEditor.putBoolean("PROFILE_EDITOR_SHOWING", false);
					prefsEditor.putBoolean(PROFILE_EXISTS, true);
					prefsEditor.commit();
					targetWeightArray.set(0, "Current Weight (" + sharedPrefs.getInt("CURRENT_WEIGHT", 0) + ")");
					targetWeightArray.set(1, "Goal Weight (" + sharedPrefs.getInt("GOAL_WEIGHT", 0) + ")");
					ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.row, R.id.text, targetWeightArray);		
					targetWeightSpinner.setAdapter(spinnerArrayAdapter);
					activityLevelSpinner.setSelection(sharedPrefs.getInt("ACTIVITY_LEVEL", 0));
					init();
				}
			};
			profileEditor.loadState();
			profileEditor.show();
		}
		else {
			init();
		}
		
	} //End onCreate()

	private void init() {
		
		targetWeightArray.clear();
		targetWeightArray.add("Current Weight (" + sharedPrefs.getInt("CURRENT_WEIGHT", 0) + ")");
		targetWeightArray.add("Goal Weight (" + sharedPrefs.getInt("GOAL_WEIGHT", 0) + ")");

		ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, R.layout.row, R.id.text, targetWeightArray);		
		targetWeightSpinner.setAdapter(spinnerArrayAdapter);
		
		currentBmr = new BasalMetabolicRate(sharedPrefs.getBoolean("GENDER", false) ? Gender.MALE : Gender.FEMALE);
		currentBmr.w.lbs = sharedPrefs.getInt("CURRENT_WEIGHT", 0);
		currentBmr.a = calculateAge();
		currentBmr.h.feet = sharedPrefs.getInt("HEIGHT_FEET", 0);
		currentBmr.h.inches = sharedPrefs.getInt("HEIGHT_INCHES", 0);		

		goalBmr = new BasalMetabolicRate(sharedPrefs.getBoolean("GENDER", false) ? Gender.MALE : Gender.FEMALE);
		goalBmr.w.lbs = sharedPrefs.getInt("GOAL_WEIGHT", 0);
		goalBmr.a = calculateAge();
		goalBmr.h.feet = sharedPrefs.getInt("HEIGHT_FEET", 0);
		goalBmr.h.inches = sharedPrefs.getInt("HEIGHT_INCHES", 0);		

		aq.id(R.id.currentWeightEditText).text("" + sharedPrefs.getInt("CURRENT_WEIGHT", 0));
		
		//create anonymous OnItemSelectedListener for activity level spinner
		activityLevelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				calculateActiveBMR();
				updateUi();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {/*unused*/}			
		});
		//restore saved state if exists
		activityLevelSpinner.setSelection(sharedPrefs.getInt("ACTIVITY_LEVEL", 0));
		
		//---setup eat button
		aq.id(R.id.eatButton).clicked(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCalorieEditor();
				prefsEditor.putBoolean("CALORIE_EDITOR_SHOWING", true);
				prefsEditor.commit();
			}
		});
		
		prefsEditor.putBoolean(APP_RUNNING, true);
		
//		prefsEditor.putInt("LAST_YEAR", 2014);
//		prefsEditor.putInt("LAST_MONTH", 5);
//		prefsEditor.putInt("LAST_DAY", 17);
		
//		prefsEditor.putInt("STARTING_WEIGHT", 370);
//		prefsEditor.putInt("CURRENT_WEIGHT", 370);
//		prefsEditor.putInt("CALORIE_BANK", (int)(3500*0.2));
		
		prefsEditor.commit();
		
		setupCalorieTimer();
		loadUiPreferences();
		updateUi();
		
	} //End init()
	
	private void showCalorieEditor() {
		calorieEditor = new CalorieEditor(MainActivity.this) {
		@Override
		public void onSave(FoodEntry foodEntry) {
				prefsEditor.putInt("CALORIES_EATEN", 
						sharedPrefs.getInt("CALORIES_EATEN", 0) + foodEntry.getCalories());
				prefsEditor.commit();
				updateUi();
				calorieEditor = null;
			}					
		};
		calorieEditor.show();
	}
	
    private int calculateAge() {
    	//---fetch birthday
    	int year = sharedPrefs.getInt("YEAR", 0);
    	int month = sharedPrefs.getInt("MONTH", 0);
    	int day = sharedPrefs.getInt("DAY", 0);
    	
    	Calendar today = Calendar.getInstance();
    	//calculate years
    	int years = today.get(Calendar.YEAR) - year;
    	//if the current month and day are not yet the birthday
    	if(today.get(Calendar.MONTH) < month && today.get(Calendar.DATE) < day)
    		//decrement years
    		years--;
    	
    	return years;
    }
    
    private void calculateActiveBMR() {
    	if(currentBmr == null) return;
    	
    	switch(targetWeightSpinner.getSelectedItemPosition()) {
			case 0:
				activeBmr = currentBmr.getBMR() * activityLevelMultipliers[activityLevelSpinner.getSelectedItemPosition()];
				break;
			case 1:
				activeBmr = goalBmr.getBMR() * activityLevelMultipliers[activityLevelSpinner.getSelectedItemPosition()];
				break;
		}
    	
    	aq.id(R.id.caloriesRemainingEditText).text(String.format("%d", (int)activeBmr - sharedPrefs.getInt("CALORIES_EATEN", 0)));
    	aq.id(R.id.bmrEditText).text( String.format("%.2f", activeBmr) );
    }
	
	private void setupCalorieTimer() {				
		//create handler to post update back to ui thread
		final Handler handler = new Handler();			
		
		calorieTimer = new Timer();
		
		calorieTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				//post to ui thread
				handler.post(new Runnable() {
					@Override
					public void run() {
						updateUi();
					}					
				});		
			}			
		}, 0, 1000);		
	}
	
	private void updateUi() {
		
		lastDate = cal;
		cal = Calendar.getInstance();
		
		//if the current date is greater than the last stored date
		if(compareDates(cal, lastDate) > 0 && count == 0) {
			double bankBmr = currentBmr.getBMR() * 
					activityLevelMultipliers[activityLevelSpinner.getSelectedItemPosition()];
			int caloriesEaten = sharedPrefs.getInt("CALORIES_EATEN", 0);
			prefsEditor.putInt("CALORIES_EATEN", 0);
			prefsEditor.putInt("CALORIE_BANK", sharedPrefs.getInt("CALORIE_BANK", 0) + (int)bankBmr - caloriesEaten);							
			prefsEditor.commit();
			//update lastDate to prevent multiple calorie banking
			lastDate = cal; //not working
			count++;
			Log.e("updateUi()", "count #" + count);
		}
	
		aq.id(R.id.caloriesEatenEditText).text("" + sharedPrefs.getInt("CALORIES_EATEN", 0));
		aq.id(R.id.caloriesRemainingEditText).text(String.format("%d", 
				(int)activeBmr - sharedPrefs.getInt("CALORIES_EATEN", 0)));
		aq.id(R.id.bankEditText).text("" + sharedPrefs.getInt("CALORIE_BANK", 0));
		aq.id(R.id.estimatedWeightEditText).text(String.format("%.2f",
				sharedPrefs.getInt("CURRENT_WEIGHT", 0) - (sharedPrefs.getInt("CALORIE_BANK", 0) / 3500.0) ));
		//update the time
		time.setToNow();
		//update calories available
		caloriesAvailableEditText.setText(
				String.format("%.2f", activeBmr * percentageOfDay(time) -
						sharedPrefs.getInt("CALORIES_EATEN",  0)));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		loadUiPreferences();
	}

	@Override
	protected void onStop() {		
		super.onStop();
		saveUiPreferences();
	}

	/**
	 * Compares 2 date arguments and returns their relative order.
	 * Only month, day, and year are considered, time is ignored.
	 * @param date1 Date to be considered as the l-value.
	 * @param date2 Date to be considered as the r-value.
	 * @return 0 if the dates are equal, -1 if date1 < date2, and
	 * 1 if date1 > date2.
	 */
	private int compareDates(Calendar date1, Calendar date2) {
		//---store short forms of the date values
		int date1Year = date1.get(Calendar.YEAR);
		int date1Month = date1.get(Calendar.MONTH);
		int date1Day = date1.get(Calendar.DATE);
		int date2Year = date2.get(Calendar.YEAR);
		int date2Month = date2.get(Calendar.MONTH);
		int date2Day = date2.get(Calendar.DATE);
		
		//check for date1Year < or > date2Year
		if(date1Year < date2Year) return -1;
		if(date1Year > date2Year) return 1;
		//date1Year == date2Year
		if(date1Month < date2Month) return -1;
		if(date1Month > date2Month) return 1;
		//date1Month == date2Month
		if(date1Day < date2Day) return -1;
		if(date1Day > date2Day) return 1;
		
		//if none of the previous are true, the dates are equal
		return 0;
	}
	
	private String createDateFilename(Calendar date) {
		return String.format("%d.%d.%d_",
				date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
	}
	
	private double percentageOfDay(Time time) {
		//factor the hour
		double percentage = time.hour / 24f;
		//factor the minute
		percentage += time.minute / 1440f;
		//factor the second
		percentage += time.second / 86400f;		
		
		return percentage;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		saveUiPreferences();
		prefsEditor.putBoolean(APP_RUNNING, false);
		prefsEditor.commit();
	}

	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
    public void removeState() {
    	prefsEditor.clear();
    	prefsEditor.commit();
    }
    
	public String formatTime(Time time) {
		StringBuilder string = new StringBuilder();
		
		int hour = 12;
		
		if(time.hour != 0 && time.hour > 12)
			hour = time.hour - 12;
		else if (time.hour != 0)
			hour = time.hour;
		
		string.append(hour);
		string.append(time.format(":%M%p"));
		
		return string.toString();
	}
		
	private void saveUiPreferences() {
		prefsEditor.putInt("LAST_YEAR", cal.get(Calendar.YEAR));
		prefsEditor.putInt("LAST_MONTH", cal.get(Calendar.MONTH));
		prefsEditor.putInt("LAST_DAY", cal.get(Calendar.DATE));
		count = 0;
		prefsEditor.putInt("BASED_ON_SELECTION", aq.id(R.id.targetWeightSpinner).getSelectedItemPosition());
		prefsEditor.putInt("ACTIVITY_LEVEL_SELECTION", aq.id(R.id.activityLevelSpinner).getSelectedItemPosition());		
		prefsEditor.commit();
	}
	
	private void loadUiPreferences() {
		if(sharedPrefs.getInt("LAST_YEAR", 0) == 0)
			cal = Calendar.getInstance();
		else
			cal = new GregorianCalendar(sharedPrefs.getInt("LAST_YEAR", 0),
										sharedPrefs.getInt("LAST_MONTH", 0),
										sharedPrefs.getInt("LAST_DAY", 0));
		aq.id(R.id.targetWeightSpinner)
		  .setSelection(sharedPrefs.getInt("BASED_ON_SELECTION", 0));
		aq.id(R.id.activityLevelSpinner)
		  .setSelection(sharedPrefs.getInt("ACTIVITY_LEVEL_SELECTION", 0));
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		QuickMenu.pause();		
		saveUiPreferences();
		
		//---remove calorieTimer
		calorieTimer.cancel();
		
		//if profileEditor is allocated
		if(profileEditor != null && profileEditor.isShowing()) {
			//save its state
			profileEditor.saveState();
			//dismiss it
			profileEditor.dismiss();
		}
		
		//if alert is allocated
		if(alert != null && alert.isShowing())
			//dismiss it
			alert.dismiss();
		
		if(calorieEditor != null) {
			calorieEditor.saveState();
			calorieEditor.dismiss();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		loadUiPreferences();		
		setupCalorieTimer();
		
		//if alert is allocated
		if(alert != null)
			//show it
			alert.show();
		//else if profileEditor is allocated
		else if(profileEditor != null)
			//show it
			profileEditor.show();
		//else if calorieEditor is allocated
		else if(sharedPrefs.getBoolean("CALORIE_EDITOR_SHOWING", false)) {			
			showCalorieEditor();
			calorieEditor.loadState();
		}
		
		QuickMenu.resume();
		
	}

	//probably not using this
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//TODO save temporary state here
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

} //End class MainActivity