package com.calorieclock.datamodel;

import android.os.Parcel;
import android.os.Parcelable;

public class Profile implements Parcelable {
		
	public enum Gender { MALE, FEMALE };
	
	public Weight startingWeight = new Weight();
	public Weight currentWeight = new Weight();
	public Weight goalWeight = new Weight();	
	
	public Profile() {
		//TODO write no-arg constructor code
	}
	
	/**
	 * 	Constructor using parcel as input.
	 * @param in Holds the parcel to be used for construction.
	 */
	public Profile(Parcel in) {
		//TODO write Parcelable constructor
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
	}
	
	public static class BasalMetabolicRate {
		
		//height structure
		public Height h;
		//weight structure
		public Weight w;
		//age in years;		
		public double a;
		//supplemental gender bmr constant
		private double s = 0;
		
		private double heightScalar;
		private double weightScalar;
		private double ageScalar;
		
		private double kgPerLb = 0.453592;
		private double cmPerInch = 2.54;
		
		private Gender gender;
		
		public BasalMetabolicRate(Gender gender) {
			h = new Height();
			w = new Weight();
			setGender(gender);			
		}

		public Gender getGender() {
			return gender;
		}

		//test adjustments for metric system
		public void setGender(Gender gender) {
			this.gender = gender;
			if(gender == Gender.MALE) {
				heightScalar = 12.7f;
				weightScalar = 6.23f;
				ageScalar = 6.8f;
				s = 66;
				//use mifflin value
//				s = 5;
			}
			//else set female constant
			else {
				heightScalar = 4.7f;
				weightScalar = 4.35f;
				ageScalar = 4.7f;
				s = 665;
				//use mifflin value
//				s= -161;
			}
			//---use mifflin values
//			weightScalar = 10.0;
//			heightScalar = 6.25;
//			ageScalar = 5.0;
		}
		
		/**
		 * Calculates and returns BMR based on internal metrics.
		 * Note: this function is adjusted for the metric system
		 * @return
		 */
		public double getBMR() {
			return (heightScalar * h.getTotalInches()/* * cmPerInch*/) + (weightScalar * w.getWeight()/* * kgPerLb*/) - (ageScalar * a) + s;
		}
		
	} //End class BasalMetabolicRate
	
	public static class Weight {
		public int lbs;
		public int oz;
		
		public Weight() {
			lbs = oz = 0;
		}
		
		public double getWeight() {
			return (double)lbs + (double)oz / 16;
		}
	}
	
	public static class Height {
		public int feet;
		public int inches;
		
		public Height() {
			feet = inches = 0;
		}
		
		public int getTotalInches() {
			return feet * 12 + inches;
		}
		public double getTotalFeet() {
			return (double)feet + (double)inches / 12;
		}
	}
	
} //End class Profile