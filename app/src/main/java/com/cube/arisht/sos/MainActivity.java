package com.cube.arisht.sos;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ImageView send;
    private ImageView contacts;
    private ImageView cancel;
    private TextView loc;
    private LocationManager locationManager;
    private LocationListener listener;
    String lat = "", lng = "";
    int ti = 0;

    EditText phn1, phn2, phn3;

    SQLiteDatabase db;
    int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    BroadcastReceiver smsSentReceiver, smsDeliveredReceiver;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        send = (ImageView) findViewById(R.id.send);
        contacts = (ImageView) findViewById(R.id.cont) ;

        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });


        //databse and tables
        db = openOrCreateDatabase("Emergency", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS NUMBER1 (PHONE1 VARCHAR(10), PHONE2 VARCHAR(10));");
        db.execSQL("CREATE TABLE IF NOT EXISTS NUMBER2 (PHONE1 VARCHAR(10), PHONE2 VARCHAR(10));");
        db.execSQL("CREATE TABLE IF NOT EXISTS NUMBER3 (PHONE1 VARCHAR(10), PHONE2 VARCHAR(10));");





        //setting pending intent for message service
        sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //   Toast.makeText(MainActivity.this, "location change", Toast.LENGTH_SHORT).show();
                lng = String.valueOf(location.getLongitude());
                lat = String.valueOf(location.getLatitude());

                ti++;
                send(lat, lng);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /*
				 * The following method, "handleShakeEvent(count):" is a stub //
				 * method you would use to setup whatever you want done once the
				 * device has been shook.
				 */
                //handleShakeEvent(count);
                // first check for permissions
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                                , 10);
                    }
                    return;
                }
                locationManager.requestLocationUpdates("gps", 100, 0, listener);
                Toast.makeText(MainActivity.this, "Your message is being sent", Toast.LENGTH_SHORT).show();
                ti = 0;
                //Toast.makeText(MainActivity.this, "SHAKE", Toast.LENGTH_SHORT).show();
                // ti = 0;
            }
        });



    }

    //gps
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }


    //database
    //inserting 1st emergency contact in database
    public void insert1(View v) {

        // phn1 = (EditText) findViewById(R.id.phn1);
        try {
            //overwrites any pre existing number
            db.execSQL("DELETE FROM NUMBER1;");
            String number1 = String.valueOf(phn1.getText()).substring(0, 5);
            String number2 = String.valueOf(phn1.getText()).substring(5);
            if (number1 != null && number2 != null) {
                db.execSQL("INSERT INTO NUMBER1(PHONE1, PHONE2) VALUES(" + number1 + "," + number2 + ");");
                Toast.makeText(MainActivity.this, "1st NUMBER STORED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "NOTHING IN NUMBER FIELD", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "PLEASE ENTER A VALID NUMBER", Toast.LENGTH_SHORT).show();

        }

    }

    //inserting 2nd emergency contact in database
    public void insert2(View v) {

        //  phn2 = (EditText) findViewById(R.id.phn1);
        try {


            //overwrites any pre existing number
            db.execSQL("DELETE FROM NUMBER2;");
            String number1 = String.valueOf(phn2.getText()).substring(0, 5);
            String number2 = String.valueOf(phn2.getText()).substring(5);
            if (number1 != null && number2 != null) {
                db.execSQL("INSERT INTO NUMBER2(PHONE1, PHONE2) VALUES(" + number1 + "," + number2 + ");");
                Toast.makeText(MainActivity.this, "2nd NUMBER STORED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "NOTHING IN NUMBER FIELD", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "PLEASE ENTER A VALID NUMBER", Toast.LENGTH_SHORT).show();

        }

    }

    //inserting 3rd emergency contact in database
    public void insert3(View v) {

        //   phn3 = (EditText) findViewById(R.id.phn3);
        try {
            //overwrites any pre existing number
            db.execSQL("DELETE FROM NUMBER3;");
            String number1 = String.valueOf(phn3.getText()).substring(0, 5);
            String number2 = String.valueOf(phn3.getText()).substring(5);
            Toast.makeText(MainActivity.this, number1 + "   " + number2, Toast.LENGTH_SHORT).show();

            if (number1 != null && number2 != null) {
                db.execSQL("INSERT INTO NUMBER3(PHONE1, PHONE2) VALUES(" + number1 + "," + number2 + ");");
                Toast.makeText(MainActivity.this, "3rd NUMBER STORED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "NOTHING IN NUMBER FIELD", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "PLEASE ENTER A VALID NUMBER", Toast.LENGTH_SHORT).show();

        }

    }

    //for displaying number stored in table.
    public void display1(View v) {

        // disp = (TextView) findViewById(R.id.disp);
        Cursor c;
        int temp1, temp2;
        String data1, data2;
        data1 = "";
        data2 = "";
        c = db.rawQuery("SELECT * FROM NUMBER1", null);
        c.moveToFirst();


        try {
            for (int i = 0; c.moveToPosition(i); i++) {
                temp1 = c.getInt(0);
                data1 += Integer.toString(temp1);
                temp2 = c.getInt(1);
                data2 += Integer.toString(temp2);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "NOTHING IN DATABASE", Toast.LENGTH_SHORT).show();
        }


        if (data1 != null && data2 != null) {
            phn1.setText(data1 + data2);
        } else {
            Toast.makeText(MainActivity.this, "NOTHING IN DATABASE", Toast.LENGTH_SHORT).show();
        }

    }

    //for displaying number stored in table.
    public void display2(View v) {

        // disp = (TextView) findViewById(R.id.disp);
        Cursor c;
        int temp1, temp2;
        String data1, data2;
        data1 = "";
        data2 = "";
        c = db.rawQuery("SELECT * FROM NUMBER2", null);
        c.moveToFirst();

        try {
            for (int i = 0; c.moveToPosition(i); i++) {
                temp1 = c.getInt(0);
                data1 += Integer.toString(temp1);
                temp2 = c.getInt(1);
                data2 += Integer.toString(temp2);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "NOTHING IN DATABASE", Toast.LENGTH_SHORT).show();
        }


        if (data1 != null && data2 != null) {
            phn2.setText(data1 + data2);
        } else {
            Toast.makeText(MainActivity.this, "NOTHING IN DATABASE", Toast.LENGTH_SHORT).show();
        }

    }

    //for displaying number stored in table.
    public void display3(View v) {

        //   disp = (TextView) findViewById(R.id.disp);
        Cursor c;
        int temp1, temp2;
        String data1, data2;
        data1 = "";
        data2 = "";
        c = db.rawQuery("SELECT * FROM NUMBER3", null);
        c.moveToFirst();


        try {
            for (int i = 0; c.moveToPosition(i); i++) {
                temp1 = c.getInt(0);
                data1 += Integer.toString(temp1);
                temp2 = c.getInt(1);
                data2 += Integer.toString(temp2);
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "NOTHING IN DATABASE", Toast.LENGTH_SHORT).show();
        }


        if (data1 != null && data2 != null) {
            phn3.setText(data1 + data2);
        } else {
            Toast.makeText(MainActivity.this, "NOTHING IN DATABASE", Toast.LENGTH_SHORT).show();
        }

    }

    //for deleting records.
    public void delete1(View v) {
        db.execSQL("DELETE FROM NUMBER1;");
        phn1.setText("");
        Toast.makeText(MainActivity.this, "1st NUMBER DELETED", Toast.LENGTH_SHORT).show();
    }

    //for deleting records.
    public void delete2(View v) {
        db.execSQL("DELETE FROM NUMBER2;");
        phn2.setText("");
        Toast.makeText(MainActivity.this, "2nd NUMBER DELETED", Toast.LENGTH_SHORT).show();
    }

    //for deleting records.
    public void delete3(View v) {
        db.execSQL("DELETE FROM NUMBER3;");
        phn3.setText("");
        Toast.makeText(MainActivity.this, "3rd NUMBER DELETED", Toast.LENGTH_SHORT).show();
    }


    //displays various message related to message status.
    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI);

        smsSentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {

                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS SENT!!!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "GENERIC FAILURE", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.this, "NO SERVICE", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.this, "NULL PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.this, "RADIO OFF", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                        break;

                }

            }
        };

        smsDeliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS DELIVERED!!!", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "SMS NOT DELIVERED ", Toast.LENGTH_SHORT).show();
                        break;
                }

            }
        };

        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));

    }

    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(mShakeDetector);
        unregisterReceiver(smsSentReceiver);
        unregisterReceiver(smsDeliveredReceiver);
    }


    //message and gps
    void configure_button() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE, Manifest.permission.RECEIVE_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission


                locationManager.requestLocationUpdates("gps", 100, 0, listener);
                Toast.makeText(MainActivity.this, "Your message is being sent", Toast.LENGTH_SHORT).show();
                ti = 0;




            }
        });


    }

    public void send(String lat, String lng) {

        if (ti == 1) {
            //       Toast.makeText(MainActivity.this, "inside function", Toast.LENGTH_SHORT).show();


            //database
            Cursor c1;
            int temp1a, temp2a;
            String data1a, data2a;
            data1a = "";
            data2a = "";
            c1 = db.rawQuery("SELECT * FROM NUMBER1", null);
            c1.moveToFirst();

            for (int i = 0; c1.moveToPosition(i); i++) {
                temp1a = c1.getInt(0);
                data1a += Integer.toString(temp1a);
                temp2a = c1.getInt(1);
                data2a += Integer.toString(temp2a);


            }

            String tel1 = "";
            tel1 = data1a + data2a;
            //   Toast.makeText(MainActivity.this, tel1, Toast.LENGTH_SHORT).show();


            Cursor c2;
            int temp1b, temp2b;
            String data1b, data2b;
            data1b = "";
            data2b = "";
            c2 = db.rawQuery("SELECT * FROM NUMBER2", null);
            c2.moveToFirst();

            for (int i = 0; c2.moveToPosition(i); i++) {
                temp1b = c2.getInt(0);
                data1b += Integer.toString(temp1b);
                temp2b = c2.getInt(1);
                data2b += Integer.toString(temp2b);


            }

            String tel2 = "";
            tel2 = data1b + data2b;
            //  Toast.makeText(MainActivity.this, tel2, Toast.LENGTH_SHORT).show();


            Cursor c3;
            int temp1c, temp2c;
            String data1c, data2c;
            data1c = "";
            data2c = "";
            c3 = db.rawQuery("SELECT * FROM NUMBER3", null);
            c3.moveToFirst();

            for (int i = 0; c3.moveToPosition(i); i++) {
                temp1c = c3.getInt(0);
                data1c += Integer.toString(temp1c);
                temp2c = c3.getInt(1);
                data2c += Integer.toString(temp2c);


            }

            String tel3 = "";
            tel3 = data1c + data2c;
            // Toast.makeText(MainActivity.this, tel3, Toast.LENGTH_SHORT).show();


            //message string.
            String message = "I AM IN DANGER. I NEED HELP. MY CURRENT LOCATION IS http://maps.google.com/?ll=" + lat + "," + lng;
            //String telno = phn.getText().toString();


            try {


                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(tel1, null, message, sentPI, deliveredPI);

                    //          Toast.makeText(MainActivity.this, "sending ." + lat, Toast.LENGTH_SHORT).show();

                }

            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "UNABLE TO SEND MESSAGE TO " + tel1, Toast.LENGTH_SHORT).show();
            }
            try {

                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(tel2, null, message, sentPI, deliveredPI);


                    //      Toast.makeText(MainActivity.this, "sending 2..." + lng, Toast.LENGTH_SHORT).show();

                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "UNABLE TO SEND MESSAGE TO " + tel2, Toast.LENGTH_SHORT).show();
            }

            try {


                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                } else {
                    SmsManager sms = SmsManager.getDefault();
                    sms.sendTextMessage(tel3, null, message, sentPI, deliveredPI);


                    //     Toast.makeText(MainActivity.this, "sending 3..." + lat, Toast.LENGTH_SHORT).show();

                }
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "UNABLE TO SEND MESSAGE TO " + tel3, Toast.LENGTH_SHORT).show();
            }

            //  Toast.makeText(MainActivity.this, "exiting function", Toast.LENGTH_SHORT).show();
        } else {
            //     Toast.makeText(MainActivity.this, "no need to run this", Toast.LENGTH_SHORT).show();
        }

    }

    public void police(View v) {



        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
return;}
        else{     Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:100"));
startActivity(callIntent);}
}

    public void ambulance(View v){



        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
            return;}
        else{     Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:102"));
            startActivity(callIntent);}
    }

    public void fire(View v){



        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
            return;}
        else{     Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:101"));
            startActivity(callIntent);}

    }
    public void wamen(View v){



        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                    MY_PERMISSIONS_REQUEST_CALL_PHONE);
            return;}
        else{     Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:181"));
            startActivity(callIntent);}

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, setcode.class));
                return true;
            case R.id.action_help:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You can also shake your device to send help Message.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}