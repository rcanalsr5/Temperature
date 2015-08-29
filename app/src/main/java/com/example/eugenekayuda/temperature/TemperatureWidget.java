package com.example.eugenekayuda.temperature;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * Implementation of App Widget functionality.
 */
public class TemperatureWidget extends AppWidgetProvider {
    GetTemperature temperature;
    public SharedPreferences sPref;
    String host, port, username, password;
    String roomTempW, roomHumW, outTempW;
    private class GetTemperature extends AsyncTask<String, Void, String> {

        //String command;
        String result;
        public GetTemperature(){

        }

        @Override
        protected void onPreExecute() {

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
            catch (Exception e){
                //result = e.getMessage();
                result = "error" + " " + e.getMessage();
                e.printStackTrace();
            }
            //result = params[0];
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            //this.result = result;

            /*if (mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);*/
            if (!result.startsWith("error")) {
                result = result.trim();
                String[] results = result.split("\n");
                String[] resultArr = results[0].split(",");
                String[] resultOutTemp = results[2].split("=");
                double outdoorTemp = Double.parseDouble(resultOutTemp[1]) / 1000;

                roomTempW = resultArr[1] + "°C";
                roomHumW = resultArr[0] + "%";
                outTempW = String.format("%.2f", outdoorTemp).replace(',', '.') + "°C";
            }


        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    @Override
    public void onReceive(Context context, Intent intent) {

    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {
        sPref = context.getSharedPreferences("connection_setting", 0);

        host = sPref.getString("host", "");
        port = sPref.getString("port", "");
        username = sPref.getString("username", "");
        password = sPref.getString("password", "");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.temperature_widget);


        boolean wifi = false;
        boolean inet = false;
        WifiManager wm = (WifiManager)  context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        if (ni == null || !ni.isConnected()) {
            // There are no active networks.
            inet = false;
        } else
            inet = true;


        //Toast.makeText(getApplicationContext(), wi.getNetworkId(), Toast.LENGTH_SHORT).show();
        if (wi != null && wi.getNetworkId()!=-1){
            if (inet)
                wifi = true;
            else {
                Toast.makeText(context, "Нет интернета.", Toast.LENGTH_SHORT).show();
                wifi = false;
            }
        }
        else {
            wifi = false;
        }

        if (wifi) {

            temperature = new GetTemperature();
            temperature.execute("ls");
            views.setTextViewText(R.id.roomTempW, roomTempW);
            views.setTextViewText(R.id.roomHumW, roomHumW);
            views.setTextViewText(R.id.outTempW, outTempW);

        }


        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object

        views.setTextViewText(R.id.outTemp, widgetText);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public void getTemperature(){
            temperature = new GetTemperature();
            temperature.execute("ls");
    }






}

