package com.thesouthisland.android.callblocker.datetime;

import java.util.Date;


public class DateTime {
	
	public static String getRelativeTime(long milliseconds)
	{
		final long currentTimeMilli = System.currentTimeMillis();
		final long differenceMilli = currentTimeMilli - milliseconds;
		
		final long relativeSeconds = differenceMilli / 1000;
		final long relativeMinutes = relativeSeconds / 60;
		final long relativeHours = relativeMinutes / 60;
		final long relativeDays = relativeHours / 24;
		
		final Date date = new Date(milliseconds);
		
		int dayOfMonth = date.getDate();
		int month = date.getMonth();
		int year = date.getYear() + 1900;
		
		String relativeDateTime = null;
		
		if (relativeSeconds <= 1)
		{
			relativeDateTime = "1 second ago";
		}
		else if (relativeSeconds < 60)
		{
			relativeDateTime = relativeSeconds + " seconds ago";
		}
		else if (relativeMinutes == 1)
		{
			relativeDateTime = "1 minute ago";
		}
		else if (relativeMinutes < 60)
		{
			relativeDateTime = relativeMinutes + " minutes ago";
		}
		else if (relativeHours == 1)
		{
			relativeDateTime = "1 hour ago";
		}
		else if (relativeHours < 24)
		{
			relativeDateTime = relativeHours + " hours ago";
		}
		else if (relativeDays == 1)
		{
			relativeDateTime = "Yesterday at " + date.getHours() + ":" + date.getMinutes();
		}
		else if (relativeDays < 5)
		{
			relativeDateTime = relativeDays + " days ago";
		}
		else
		{
			relativeDateTime = dayOfMonth + getDaySuffix(dayOfMonth) + " " + getMonth(month) + ", " + year;
		}
		
		return relativeDateTime;
	}
	
	public static String getDayOfWeek(int day)
	{
		switch (day)
		{
			case 1:
				return "Monday";
			case 2:
				return "Tuesday";
			case 3:
				return "Wednesday";
			case 4:
				return "Thursday";
			case 5:
				return "Friday";
			case 6:
				return "Saturday";
			case 7:
				return "Sunday";
		}
		
		return "";
	}
	
	public static String getMonth(int month)
	{
		switch (month)
		{
			case 1:
				return "January";
			case 2:
				return "February";
			case 3:
				return "March";
			case 4:
				return "April";
			case 5:
				return "May";
			case 6:
				return "June";
			case 7:
				return "July";
			case 8:
				return "August";
			case 9:
				return "September";
			case 10:
				return "October";
			case 11:
				return "November";
			case 12:
				return "December";
		}
		
		return "";
	}
	
	public static String getDaySuffix(int day)
	{
		switch (day)
		{
			case 1: case 21: case 31: return "st";
			case 2: case 22: return "nd";
			case 3: case 23: return "rd";
		}
		
		return "th";
	}
}
