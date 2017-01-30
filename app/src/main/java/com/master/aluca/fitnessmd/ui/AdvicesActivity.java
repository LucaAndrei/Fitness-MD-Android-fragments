package com.master.aluca.fitnessmd.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.master.aluca.fitnessmd.R;
import com.master.aluca.fitnessmd.common.Constants;
import com.master.aluca.fitnessmd.common.datatypes.MessageDetails;
import com.master.aluca.fitnessmd.common.util.SharedPreferencesManager;
import com.master.aluca.fitnessmd.common.webserver.WebserverManager;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryCollection;
import com.master.aluca.fitnessmd.library.db.memory.InMemoryDocument;
import com.master.aluca.fitnessmd.ui.fragments.doctor.AdviceItemAdapter;

import java.util.ArrayList;
import java.util.Date;
/*
import im.delight.android.ddp.db.Collection;
import im.delight.android.ddp.db.Document;


*/
public class AdvicesActivity extends Activity {

    public static final String LOG_TAG = "Fitness_AdvicesActivity";

    private WebserverManager mWebserverManager;

    private SharedPreferencesManager sharedPreferencesManager;

    ListView msgList;
    ArrayList<MessageDetails> details;
    AdapterView.AdapterContextMenuInfo info;
    AdviceItemAdapter mAdapter;

    private ProgressDialog pDialog;
    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advice);

        boolean intentRec = getIntent().getBooleanExtra("my_key",false);
        Log.d(LOG_TAG, "onCreate L " + intentRec);

        msgList = (ListView) findViewById(R.id.MessageList);
        details = new ArrayList<MessageDetails>();


        pDialog = new ProgressDialog(AdvicesActivity.this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ADVICES_SUBSCRIPTION_READY_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);

        mWebserverManager =  WebserverManager.getInstance(this);
        mWebserverManager.subscribeToAdvices();

        MessageDetails Detail;
        Detail = new MessageDetails();
        Detail.setIcon(R.drawable.tab_selector_doctor);
        Detail.setName("Bob");
        Detail.setSub("Dinner");
        Detail.setDesc("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla auctor.");
        Detail.setTime("12/12/2012 12:12");
        details.add(Detail);

        Detail = new MessageDetails();
        Detail.setIcon(R.drawable.tab_selector_doctor);
        Detail.setName("Rob");
        Detail.setSub("Party");
        Detail.setDesc("Dolor sit amet, consectetur adipiscing elit. Nulla auctor.");
        Detail.setTime("13/12/2012 10:12");
        details.add(Detail);

        Detail = new MessageDetails();
        Detail.setIcon(R.drawable.tab_selector_doctor);
        Detail.setName("Mike");
        Detail.setSub("Mail");
        Detail.setDesc("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
        Detail.setTime("13/12/2012 02:12");
        details.add(Detail);

        mAdapter = new AdviceItemAdapter(details,this);

        msgList.setAdapter(mAdapter);

        msgList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView a, View v, int position, long id) {

                String s = (String) ((TextView) v.findViewById(R.id.From)).getText();
                Toast.makeText(AdvicesActivity.this, s, Toast.LENGTH_LONG).show();
            }
        });

    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(LOG_TAG, "action : " + action.toString());
            if (action.equals(Constants.ADVICES_SUBSCRIPTION_READY_INTENT)) {
                if (intent.getBooleanExtra(Constants.ADVICES_SUBSCRIPTION_READY_BUNDLE_KEY, false)) {
                    new LoadData().execute();

                } else {
                    Log.d(LOG_TAG, "ADVICES_SUBSCRIPTION_READY_BUNDLE_KEY false");
                }
            }
        }

    };

    class LoadData extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setIndeterminate(true);
            pDialog.setMessage("Fetch data...");
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            //ProgressDialog mProgressDialog = new ProgressDialog(Advice.this);
            //mProgressDialog.setMessage("Receiving data from server");
            //mProgressDialog.show();
            InMemoryCollection advices = mWebserverManager.getAdvices();
            if (advices != null) {
                String[] documentIds = advices.getDocumentIds();
                Log.d(LOG_TAG, "documentIds.length : " + documentIds.length);
                details.clear();
                for (int i = 0; i < documentIds.length; i++) {
                    InMemoryDocument doc = advices.getDocument(documentIds[i]);
                    String[] fieldNames = doc.getFieldNames();
                    Log.d(LOG_TAG, "fieldNames.length : " + fieldNames.length);
                    for (int j = 0; j < fieldNames.length; j++) {
                        Log.d(LOG_TAG, "field : " + fieldNames[j] + " >>> " + doc.getField(fieldNames[j]));

                    }
                    MessageDetails Detail = new MessageDetails();
                    Detail.setIcon(R.drawable.tab_selector_doctor);
                    Detail.setName(doc.getField("ownerName").toString());
                    Detail.setSub("Advice");
                    Detail.setDesc(doc.getField("message").toString());
                    String timestamp = doc.getField("timestamp").toString();
                    Log.d(LOG_TAG, "timestamp : " + timestamp);
                    String splittedString = timestamp.split("=")[1];
                    String ts = splittedString.substring(0, splittedString.length()-1);
                    long timestampVal  = Long.valueOf(ts).longValue();
                    Log.d(LOG_TAG, "timestampVal : " + timestampVal);
                    Detail.setTime(new Date(timestampVal).toString());
                    details.add(Detail);
                }
            }
            //mAdapter.notifyDataSetChanged();
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all the data
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });

            pDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }

}