package com.cube.arisht.sos;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.intentfilter.androidpermissions.PermissionManager;

import static java.util.Collections.singleton;

public class LocationService extends Service {
    private String strAddress = "";
    private String strTo;
    private LocationManager lmgSMS;
    public static int MY_PERMISSION_ACCESS_COURSE_LOCATION = 13;

    private final LocationListener lolSMS = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
// TODO Auto-generated method stub
        }

        @Override
        public void onProviderEnabled(String provider) {
// TODO Auto-generated method stub
        }

        @Override
        public void onProviderDisabled(String provider) {
// TODO Auto-generated method stub
        }

        @Override
        public void onLocationChanged(Location location) {
// TODO Auto-generated method stub
            if (location != null) {
                sendLocation(location);
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
// TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this,"hello",Toast.LENGTH_SHORT).show();
        Bundle bunSMS = intent.getExtras();
        strTo = bunSMS.getString(SMSReceiver.PHONENUMBER);
        Log.w("number",strTo);

        lmgSMS = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        PermissionManager permissionManager = PermissionManager.getInstance(getApplicationContext());
        permissionManager.checkPermissions(singleton(Manifest.permission.ACCESS_FINE_LOCATION), new PermissionManager.PermissionRequestListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(getApplicationContext(), "Permissions Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied() {
                Toast.makeText(getApplicationContext(), "Permissions Denied", Toast.LENGTH_SHORT).show();
            }
        });
        lmgSMS.requestLocationUpdates("gps", 1000, 1, lolSMS);
        Log.w("Here","here");
        return START_STICKY;
    }

    public void onCreate() {
        super.onCreate();
    }

    private void parseLocation(Location locCurrent) {
        if (locCurrent != null) {
            Log.w("working","yes");
            double dblLatitude = locCurrent.getLatitude();
            double dblLongitude = locCurrent.getLongitude();
            strAddress = "http://maps.google.com/?ll=" + dblLatitude + "," + dblLongitude;


        }

    }

    private void sendLocation(Location locCurrent) {
        if (locCurrent != null) {
            parseLocation(locCurrent);
            if (strAddress.length() > 0) {
                SmsManager smsLocation = SmsManager.getDefault();
                smsLocation.sendTextMessage(strTo, null, strAddress, null, null);
                lmgSMS.removeUpdates(lolSMS);
                stopSelf();
            }
        }
    }
}