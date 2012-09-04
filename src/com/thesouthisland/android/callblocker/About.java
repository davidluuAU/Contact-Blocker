/*
 * Copyright (C) 2011 The South Island
 *
 */

package com.thesouthisland.android.callblocker;

import android.app.Activity;
import android.os.Bundle;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class About extends Activity{

	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.about);
		 
		 ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		 //actionBar.setTitle(R.string.about);
		 actionBar.setHomeAction(new IntentAction(this, CallBlockList.createIntent(this), R.drawable.ic_logo));
	 }
	
}
