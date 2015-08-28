package com.example.eugenekayuda.temperature;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    public SharedPreferences sPref;

    EditText host, port, username, password;
    CheckBox showPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sPref = getSharedPreferences("connection_setting", MODE_PRIVATE);


        host = (EditText) findViewById(R.id.host);
        port = (EditText) findViewById(R.id.port);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        showPass = (CheckBox) findViewById(R.id.checkBox);
        showPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // checkbox status is changed from uncheck to checked.
                if (!isChecked) {
                    // show password
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // hide password
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        host.setText(sPref.getString("host", ""));
        port.setText(sPref.getString("port", ""));
        username.setText(sPref.getString("username", ""));
        password.setText(sPref.getString("password", ""));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void saveSettings(View view) {
        sPref = getSharedPreferences("connection_setting", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        String port = "22";
        if (this.port.getText().toString()!="")
            port = this.port.getText().toString();
        ed.putString("host", host.getText().toString());
        ed.putString("port", port);
        ed.putString("username", username.getText().toString());
        ed.putString("password", password.getText().toString());
        ed.commit();
        Toast.makeText(SettingsActivity.this, "Настройки сохранены", Toast.LENGTH_SHORT).show();
        finish();
    }
}
