package com.master.aluca.fitnessmd.common.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

/**
 * Created by aluca on 11/16/16.
 */
public class AlertMessage {
    static Context mContext;
    ProgressDialog progress;

    public AlertMessage(Context context) {
        this.mContext=context;
    }
    public AlertMessage() {}

    public void alertbox() {
        Log.d("Fitness_ALERT","alertbox");
        progress = ProgressDialog.show(mContext, "Connecting...", "Please wait!!!");  //show a progress dialog
        /*AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Blah")
                .setCancelable(false)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();*/
    }

    public void alertBoxAdvice() {
        Log.d("Fitness_ALERT","alertBoxAdvice");
        progress = ProgressDialog.show(mContext, "Fetching data...", "Please wait!!!");  //show a progress dialog
    }

    public void dismiss() {
        Log.d("Fitness_ALERT","dismiss");
        progress.dismiss();
    }
}
