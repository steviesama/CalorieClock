package com.calorieclock.datamodel;

import android.text.format.Time;

public class FoodEntry {

	private Time entryTime;
	private int calories;
	private String note;

	public FoodEntry() {
		entryTime = new Time();
		calories = 0;
		note = "";
	}
	
	public FoodEntry(int calories, String note) {
		entryTime = new Time();
		entryTime.setToNow();
		this.calories = calories;
		this.note = note;
	}
	
	public FoodEntry(Time entryTime, int calories, String note) {
		this.entryTime = entryTime;
		this.calories = calories;
		this.note = note;
	}

	public Time getEntryTime() {
		return entryTime;
	}

	public FoodEntry setEntryTime(Time entryTime) {
		this.entryTime = entryTime;
		return this;
	}
	
	public FoodEntry setEntryTimeToNow() {
		entryTime.setToNow();
		return this;
	}

	public int getCalories() {
		return calories;
	}

	public FoodEntry setCalories(int calories) {
		this.calories = calories;
		return this;
	}

	public String getNote() {
		return note;
	}

	public FoodEntry setNote(String note) {
		this.note = note;
		return this;
	}

} //End class FoodEntry