package com.cube.arisht.sos;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class setcode extends AppCompatActivity {
    public static String code;
    SQLiteDatabase db2;
    public static String b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setcode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        db2 = openOrCreateDatabase("Emergency2", MODE_PRIVATE, null);
        db2.execSQL("CREATE TABLE IF NOT EXISTS NUMBER4 (PHONE1 TEXT(10));");


        final EditText editText = (EditText) findViewById(R.id.editText);

            Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db2.execSQL("DELETE FROM NUMBER4;");

                code = String.valueOf(editText.getText());
                db2.execSQL("INSERT INTO NUMBER4 (PHONE1)"+" VALUES('"+code+"');");
                Log.w("alpha",code);
                Toast.makeText(setcode.this,"Code "+code+" successfully saved..!",Toast.LENGTH_LONG).show();

                startActivity(new Intent(setcode.this, MainActivity.class));
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu2,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This code will be used by your loved ones to gain access to your location. " +"\n"+
                        "All they have to do is send this code to your device via SMS and then they will receive a text message containing link to your current location")
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
