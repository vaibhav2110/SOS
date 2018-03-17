package com.cube.arisht.sos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver {
    ;
    public static final String PHONENUMBER = "PhoneNumber";
    SQLiteDatabase db2;


    @Override
    public void onReceive(Context context, Intent intent) {

        db2 = context.openOrCreateDatabase("Emergency2", 0, null);
        Cursor cur = db2.rawQuery("SELECT * From NUMBER4", null);
        cur.moveToFirst();
        String alpha = cur.getString(0);
        Log.w("nomber",alpha);


        Bundle bunSMS = intent.getExtras();
        if (bunSMS != null) {
            Object[] pdus = (Object[]) bunSMS.get("pdus");
            SmsMessage[] msgLocations = new SmsMessage[pdus.length];
            for (int intCounter = 0; intCounter < pdus.length; intCounter++) {
                msgLocations[intCounter] = SmsMessage.createFromPdu((byte[]) pdus[intCounter]);
            }
            for (SmsMessage msgLocation : msgLocations) {
                String strMessage = msgLocation.getMessageBody();
                String strFrom = msgLocation.getOriginatingAddress();
                if (strMessage.equals(alpha)) {
                    Intent itnStart = new Intent(context, LocationService.class);
                    itnStart.putExtra(PHONENUMBER, strFrom);
                    context.startService(itnStart);
                }
            }
        }
    }

}