package com.example.eugenekayuda.temperature;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity {
    GetTemperature temperature;
    EditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edit = (EditText) findViewById(R.id.editText);

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void connect(View view) {

        String result = "111";
        temperature = new GetTemperature();
        temperature.execute("ls");
        //edit.setText(result);

    }

    private class GetTemperature extends AsyncTask<String, Void, String>{

        //String command;
        String result;
        public GetTemperature(){

        }
        @Override
        protected String doInBackground(String... params) {
            String result= "";

            try {
                String command = params[0];
                JSch jsch = new JSch();
                Session session = jsch.getSession("root", "kayuda91.asuscomm.com", 29);
                session.setPassword("hell1991");

                // Avoid asking for key confirmation
                Properties prop = new Properties();
                prop.put("StrictHostKeyChecking", "no");
                session.setConfig(prop);

                session.connect();

                // SSH Channel
                ChannelExec channelssh = (ChannelExec)
                        session.openChannel("exec");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channelssh.setOutputStream(baos);

                // Execute command
                channelssh.setCommand(command);
                channelssh.connect();
                channelssh.disconnect();
                result = baos.toString();
            }
            catch (Exception e){
                result = e.getMessage();
                e.printStackTrace();
            }
            //result = params[0];
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            //this.result = result;
            edit.setText("End: " + result);
        }

    }


}
