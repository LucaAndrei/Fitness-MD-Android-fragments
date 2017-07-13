/*********************************************************
 *
 * Copyright (c) 2017 Andrei Luca
 * All rights reserved. You may not copy, distribute, publicly display,
 * create derivative works from or otherwise use or modify this
 * software without first obtaining a license from Andrei Luca
 *
 *********************************************************/

package com.master.aluca.fitnessmd.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;



public class DietActivity extends Activity implements OnItemSelectedListener {

    public static final String LOG_TAG = "Fitness_DietActivity";

    private ArrayList<String> breakfast = new ArrayList<>();
    private ArrayList<String> midMorning = new ArrayList<>();
    private ArrayList<String> lunch = new ArrayList<>();
    private ArrayList<String> snack = new ArrayList<>();
    private ArrayList<String> dinner = new ArrayList<>();

    WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diet);
        Log.d(LOG_TAG, "onCreate");

        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Spinner click listener
        spinner.setOnItemSelectedListener(this);
        // Spinner Drop down elements
        List<String> days = new ArrayList<String>();
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        switch (dayOfWeek) {
            case 2 :
            case 3:
            case 4:
            case 5:
            case 6:
                spinner.setSelection(dayOfWeek - 2);
                break;
            case 1:
            case 7:
                spinner.setSelection(0); // Monday
        }
        spinner.setAdapter(dataAdapter);

        createBreakfast();
        createMidMorning();
        createLunch();
        createSnack();
        createDinner();

        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl("file:///android_asset/monday.html");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.FINISH_ACTIVITY_INTENT);
        getApplicationContext().registerReceiver(mBroadcastReceiver, intentFilter);

    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive : " + intent.getAction());
            if (intent.getAction() == Constants.FINISH_ACTIVITY_INTENT) {
                Log.d(LOG_TAG, "FINISH_ACTIVITY_INTENT received");
                boolean shouldFinish = intent.getBooleanExtra(Constants.FINISH_ACTIVITY_BUNDLE_KEY,false);
                if (shouldFinish) {
                    Intent intentMainActiv = new Intent(getApplicationContext(), NoMeteorConnectionActivity.class);
                    startActivity(intentMainActiv);
                    finish();
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                    WebserverManager mWebserverManager = WebserverManager.getInstance(getApplicationContext());
                    mWebserverManager.destroyMeteor();
                } else {
                    finish();
                }
            }
        }
    };

    private void createBreakfast() {
        String breakfastText =
                "&#8226; Oat meal and skim milk<br/>" +
                "&#8226; Ground flax<br/>" +
                "&#8226; Maple syrup<br/>";
        breakfast.add(breakfastText);
        breakfastText =
                "&#8226; Veg Omelet<br/>" +
                "&#8226; Wheat bread<br/>" +
                "&#8226; Orange<br/>";
        breakfast.add(breakfastText);
        breakfastText =
                "&#8226; Cooked oats<br/>" +
                "&#8226; Almond beverage<br/>" +
                "&#8226; Sliced mango<br/>";
        breakfast.add(breakfastText);
        breakfastText =
                "&#8226; Fruit smoothies<br/>" +
                "&#8226; Almond milk<br/>" +
                "&#8226; Bananas and Blueberries<br/>";
        breakfast.add(breakfastText);
        breakfastText =
                "&#8226; Plain yogurt<br/>" +
                "&#8226; Mixed berries<br/>" +
                "&#8226; Fruits<br/>";
        breakfast.add(breakfastText);
    }

    private void createMidMorning() {
        String midMorningText =
                "&#8226; Apple";
        midMorning.add(midMorningText);
        midMorningText =
                "&#8226; Fat free yogurt";
        midMorning.add(midMorningText);
        midMorningText =
                "&#8226; Cottage cheese";
        midMorning.add(midMorningText);
        midMorningText =
                "&#8226; Nectarine";
        midMorning.add(midMorningText);
        midMorningText =
                "&#8226; Bananas";
        midMorning.add(midMorningText);
    }

    private void createLunch() {
        String lunchText =
                "&#8226; Spinach salad with almonds<br/>" +
                        "&#8226; Oranges<br/>" +
                        "&#8226; Bean sprout with raspberry<br/>";
        lunch.add(lunchText);
        lunchText =
                "&#8226; Tuna or salmon<br/>" +
                        "&#8226; Wheat roll<br/>" +
                        "&#8226; Raw vegetables<br/>";
        lunch.add(lunchText);
        lunchText =
                "&#8226; Wheat bun<br/>" +
                        "&#8226; Carrots<br/>" +
                        "&#8226; Greens<br/>";
        lunch.add(lunchText);
        lunchText =
                "&#8226; Grilled chicken breast<br/>" +
                        "&#8226; Cherry tomatoes<br/>" +
                        "&#8226; Raw broccoli<br/>";
        lunch.add(lunchText);
        lunchText =
                "&#8226; Boiled eggs<br/>" +
                        "&#8226; Carrots<br/>" +
                        "&#8226; Bean sprouts<br/>";
        lunch.add(lunchText);
    }

    private void createSnack() {
        String snackText =
                "&#8226; Protein mix";
        snack.add(snackText);
        snackText =
                "&#8226; Protein mix";
        snack.add(snackText);
        snackText =
                "&#8226; Strawberries";
        snack.add(snackText);
        snackText =
                "&#8226; Melba toast";
        snack.add(snackText);
        snackText =
                "&#8226; Protein mix";
        snack.add(snackText);
    }

    private void createDinner() {
        String dinnerText =
                "&#8226; Baked salmon<br/>" +
                        "&#8226; Steamed vegetables<br/>" +
                        "&#8226; Brown rice<br/>";
        dinner.add(dinnerText);
        dinnerText =
                "&#8226; Steamed broccoli<br/>" +
                        "&#8226; Chicken breast<br/>" +
                        "&#8226; Sweet potato<br/>";
        dinner.add(dinnerText);
        dinnerText =
                "&#8226; Baked herbs<br/>" +
                        "&#8226; Brown rice<br/>" +
                        "&#8226; Mixed greens<br/>";
        dinner.add(dinnerText);
        dinnerText =
                "&#8226; Wheat spaghetti<br/>" +
                        "&#8226; Spinach salad<br/>" +
                        "&#8226; Almonds<br/>";
        dinner.add(dinnerText);
        dinnerText =
                "&#8226; Steamed vegetables<br/>" +
                        "&#8226; Brown rice<br/>" +
                        "&#8226; Tofu cutlet<br/>";
        dinner.add(dinnerText);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        String item = parent.getItemAtPosition(position).toString();
        Log.d(LOG_TAG, "item : " + item);
        // Showing selected spinner item
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
        switch (position) {
            case 0:
                webView.loadUrl("file:///android_asset/monday_diet.html");
            break;
            case 1:
                webView.loadUrl("file:///android_asset/tuesday_diet.html");
                break;
            case 2:
                webView.loadUrl("file:///android_asset/wednesday_diet.html");
                break;
            case 3:
                webView.loadUrl("file:///android_asset/thursday_diet.html");
                break;
            case 4:
                webView.loadUrl("file:///android_asset/friday_diet.html");
                break;
            default:
                webView.loadUrl("file:///android_asset/monday_diet.html");
                break;
        }
    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


        @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }


}