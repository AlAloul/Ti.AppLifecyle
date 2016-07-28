/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2010 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package de.appwerft.applifecycle;

import java.util.Timer;

import org.appcelerator.titanium.TiProperties;

import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

@Kroll.module(name = "Applifecycle", id = "de.appwerft.applifecycle")
public class ApplifecycleModule extends KrollModule {
	private static final String LCAT = "ALifeCycle";
	static Timer cronJob = new Timer();
	static Boolean wasInForeGround = true;
	static String lastPackageName = "";

	public Boolean screenOn = true;
	public Boolean wasScreenOn = true;
	static TiApplication mApp;
	private static BroadcastReceiver mReceiver = null;
	private static TiProperties appProperties = TiApplication.getInstance()
			.getAppProperties();
	private static int testIntervalForeground = appProperties.getInt(
			"LIFECYCLE_TESTINTERVAL", 100);
	private static int testIntervalBackground = appProperties.getInt(
			"LIFECYCLE_TESTINTERVAL_BACKGROUND", 500);

	public ApplifecycleModule() {
		super();
	}

	public static void onScreenChanged(Boolean screenstate) {
		KrollDict dict = new KrollDict();
		String key = (screenstate == true) ? "screenon" : "screenoff";
		mApp.fireAppEvent(key, dict);
	}

	public static void onAppStop(final TiApplication app) {
		cronJob.cancel();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(final TiApplication app) {
		mApp = app;
		Context context = TiApplication.getInstance().getApplicationContext();

		/* Preparing of broadcatsReceiver for screenchanging */
		final IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_SCREEN_ON);
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		mReceiver = new ScreenReceiver();
		context.registerReceiver(mReceiver, intentFilter);

		cronJob.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				boolean isInFront = false;
				TaskTestResult result = isInForeground();
				isInFront = result.getIsForeground();
				if (isInFront != wasInForeGround
						|| lastPackageName != result.getPackageName()) {
					String key = (isInFront == true) ? "resumed" : "paused";
					KrollDict dict = new KrollDict();
					dict.put("packageName", result.getPackageName());
					mApp.fireAppEvent(key, dict);
					wasInForeGround = isInFront;
				}
			}
		}, 0, testIntervalForeground);
	}

	static public TaskTestResult isInForeground() {
		try {
			TaskTestResult result = new ForegroundCheckTask().execute(
					TiApplication.getInstance().getApplicationContext()).get();
			return result;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

}
