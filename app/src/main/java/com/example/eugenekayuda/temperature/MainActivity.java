package com.example.eugenekayuda.temperature;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {
    GetTemperature temperature;
    TextView roomTemp, roomHum, outTemp;
    ProgressBar progressBar;
    TextView dateTime;

    public SharedPreferences sPref;
    String host, port, username, password;

    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.orange, R.color.green);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTemperature();
            }
        });*/

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        dateTime = (TextView) findViewById(R.id.dateTime);
        sPref = getSharedPreferences("last_results", MODE_PRIVATE);

        roomTemp = (TextView)findViewById(R.id.roomTemp);
        roomHum = (TextView)findViewById(R.id.roomHum);
        outTemp = (TextView) findViewById(R.id.outTemp);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        String roomTemp = sPref.getString("roomTemp", "0");
        String roomHum = sPref.getString("roomHum", "0");
        String outTemp = sPref.getString("outTemp", "0");
        String dateTimeS = sPref.getString("dateTime", "");
        Calendar currentCal = Calendar.getInstance();
        Calendar lastCal = Calendar.getInstance();
        Date currentDate = new Date();
        currentCal.setTime(currentDate);

        Date lastDate = new Date(sPref.getLong("lastDate", 0));
        //lastCal.set(Calendar.DATE, 30);
        lastCal.setTime(lastDate);

        int datesDiff = currentCal.get(Calendar.DATE) - lastCal.get(Calendar.DATE);

        long diff = (System.currentTimeMillis() - lastDate.getTime());
        String [] updatedDate = dateTimeS.split("\n");
        //dateTime.setText(sPref.getString("lastDate", ""));
        if (datesDiff == 0)
            dateTime.setText(getString(R.string.updated_text) + ": " + updatedDate[0] + "\n" + getString(R.string.today_text));
        else if (datesDiff == 1)
            dateTime.setText(getString(R.string.updated_text) + ": " + updatedDate[0] + "\n" + getString(R.string.yesterday_text));
        else if (dateTimeS.equals(""))
            dateTime.setText("");
        else
            dateTime.setText(getString(R.string.updated_text) + ": " + dateTimeS);

        this.roomTemp.setText(roomTemp + "°C");
        this.roomHum.setText(roomHum + "%");
        this.outTemp.setText(outTemp + "°C");


        sPref = getSharedPreferences("connection_setting", MODE_PRIVATE);

        host = sPref.getString("host", "");
        port = sPref.getString("port", "");
        username = sPref.getString("username", "");
        password = sPref.getString("password", "");

        if (host == "" || port == "" || username == "" || password == ""){
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }
        else {
            if (roomTemp.equals("0") || roomHum.equals("0") || outTemp.equals("0") || diff == currentDate.getTime())
                getTemperature();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        vibrator.vibrate(50);
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        switch (id){
            case R.id.action_update: getTemperature(); break;
            case R.id.action_settings: openSettings(); break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getTemperature(){

        boolean wifi = isWiFiConnected();
        if (wifi) {

            temperature = new GetTemperature();
            temperature.execute("ls");


        }
        else
            Toast.makeText(getApplicationContext(), "Нет подключения к WiFi.", Toast.LENGTH_SHORT).show();

    }

    public void updateResults(View view) {
        getTemperature();
    }

    private boolean isWiFiConnected(){
        WifiManager wm = (WifiManager)  getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        //Toast.makeText(getApplicationContext(), wi.getNetworkId(), Toast.LENGTH_SHORT).show();
        if (wi != null && wi.getNetworkId()!=-1){
            if (isNetworkConnected())
                return true;
            else {
                Toast.makeText(getApplicationContext(), "Нет интернета.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        else {
            return false;
        }
    }

   private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

            if (ni == null || !ni.isConnected()) {
                // There are no active networks.
                return false;
            } else
                return true;



    }

    public void openSettings() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void updateResults(MenuItem item) {
        getTemperature();

    }
    private class GetTemperature extends AsyncTask<String, Void, String>{

        //String command;
        String result;
        public GetTemperature(){

        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            String result= "";


            try {
                String command = params[0];
                JSch jsch = new JSch();

                    Session session = jsch.getSession(username, host, Integer.parseInt(port));
                    session.setPassword(password);

                    // Avoid asking for key confirmation
                    Properties prop = new Properties();
                    prop.put("StrictHostKeyChecking", "no");
                    session.setConfig(prop);
                    session.setTimeout(6000);

                    session.connect();

                    // SSH Channel
                    ChannelExec channelssh = (ChannelExec)
                            session.openChannel("exec");


                    //ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    //channelssh.setOutputStream(baos);

                    // Execute command
                    channelssh.setCommand("sudo /home/pi/temperature/dht11; cat /sys/bus/w1/devices/28-000004e718c3/w1_slave");
                    channelssh.connect();

                    InputStream inputStream = channelssh.getInputStream();

                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder stringBuilder = new StringBuilder();

                    String line;

                    while ((line = bufferedReader.readLine()) != null) {

                        stringBuilder.append(line);
                        stringBuilder.append('\n');

                    }

                    result = stringBuilder.toString();
                    channelssh.disconnect();
                    //result = "no error but not connect";

            }
            catch (JSchException e){
                //result = e.getMessage();
                result = "error" + " " + e.getMessage();
                e.printStackTrace();
            } catch (IOException e) {
                result = "error" + " " + e.getMessage();
                e.printStackTrace();
            }
            //result = params[0];
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            //this.result = result;

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            sPref = getSharedPreferences("last_results", MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();

            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("HH:mm\ndd.MM.yyyy", Locale.getDefault());
            String dateTimeS = format.format(date);

            /*if (mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);*/
            if (!result.startsWith("error")) {
                dateTime.setText(getString(R.string.updated_text) + ": " + dateTimeS.split("\n")[0] + "\n" + getString(R.string.today_text));
                result = result.trim();
                String[] results = result.split("\n");
                String[] resultArr = results[0].split(",");
                String[] resultOutTemp = results[2].split("=");
                double outdoorTemp = Double.parseDouble(resultOutTemp[1]) / 1000;

                roomTemp.setText(resultArr[1] + "°C");
                roomHum.setText(resultArr[0] + "%");
                outTemp.setText(String.format("%.2f", outdoorTemp).replace(',', '.') + "°C");
                ed.putString("roomTemp", resultArr[1]);
                ed.putString("roomHum", resultArr[0]);
                ed.putString("outTemp", String.format("%.2f", outdoorTemp).replace(',', '.'));
                ed.putString("dateTime", dateTimeS);
                ed.putLong("lastDate", date.getTime());
                ed.apply();
            }
            else
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();

        }

    }


}
