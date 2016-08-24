package com.yogur.pocketadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class LoginActivity extends AppCompatActivity {

    private EditText etAddress,etPort,etUserName,etPass;
    private Button btn_connect;
    private ProgressDialog progressDialog;
    private int loginAttemptsCount = 0;
    private boolean timeOutSet = false;
    private long timeOutTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etAddress = (EditText) findViewById(R.id.etServerAddress);
        etPort = (EditText) findViewById(R.id.etPortNumber);
        etUserName = (EditText) findViewById(R.id.etUsername);
        etPass = (EditText) findViewById(R.id.etPassword);
        btn_connect = (Button) findViewById(R.id.btn_connect);

        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(loginAttemptsCount < 5)
                    //connect to db
                    connect();
                else if(timeOutSet){
                    long currentTime = new Date().getTime();
                    long timeDiff = currentTime - timeOutTimer;
                    if(timeDiff < (5 * 60000)) {//compare to 5 minutes in milliseconds
                        long remainingTime = 5 * 60000 - timeDiff;
                        int seconds = (int) (remainingTime / 1000) % 60;
                        int minutes = (int) ((remainingTime / (1000 * 60)) % 60);
                        Toast.makeText(LoginActivity.this, "You have made 5 failed login attempts, please wait for "+minutes+" minutes and "+seconds+" seconds"+" and then try again.", Toast.LENGTH_LONG).show();
                    }else{
                        loginAttemptsCount = 0;
                        timeOutSet = false;
                    }
                }
                else {
                    System.out.println("here");
                    timeOutTimer = new Date().getTime();
                    timeOutSet = true;
                }
            }
        });

    }

    public void connect(){

        if(!validate())
            return;

        btn_connect.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this, ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Connecting...");
        progressDialog.show();

        String address = etAddress.getText().toString();
        String port = etPort.getText().toString();
        String userName = etUserName.getText().toString();
        String passwd = etPass.getText().toString();

        new connectToDb().execute(userName,passwd,address,"",port);

        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                //really connect
                status = really_connect();
                //after connect dismiss progress dialog
                progressDialog.dismiss();


            }
        }).start();


        if(status){
            System.out.println("success");
            btn_connect.setEnabled(true);
        } else {
            Toast.makeText(LoginActivity.this,errMessage,Toast.LENGTH_LONG).show();
        } */
    }

   /* public boolean really_connect(){

        try {
            create_connection c = new create_connection();
            Connection conn = c.getConnection();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            //errMessage = e.getMessage();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    } */

    public boolean validate() {
        boolean valid = true;

        String address = etAddress.getText().toString();
        String port = etPort.getText().toString();
        String userName = etUserName.getText().toString();
        String passwd = etPass.getText().toString();


        if (address.isEmpty()) {
            etAddress.setError("Address can't be empty.");
            valid = false;
        } else {
           etAddress.setError(null);
        }

        if (port.isEmpty()) {
            etPort.setError("Enter a port number.");
            valid = false;
        } else {
            etPort.setError(null);
        }

        if (userName.isEmpty()) {
            etUserName.setError("Enter a username.");
            valid = false;
        } else {
            etUserName.setError(null);
        }

        if (passwd.isEmpty()) {
            etPass.setError("Enter a password.");
            valid = false;
        } else {
            etPass.setError(null);
        }

        return valid;
    }

    public class connectToDb extends AsyncTask<String,Void,String> {

        private boolean status = true;
        private String errMessage = "fail";
        private create_connection c;

        @Override
        protected String doInBackground(String... params) {

            try {
                c = new create_connection(params[0],params[1],params[2],params[3],Integer.parseInt(params[4]));
                Connection conn = c.getConnection();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                errMessage = e.getMessage();
                status = false;

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            progressDialog.dismiss();
            btn_connect.setEnabled(true);

            if(status){
                System.out.println("success");
                //set global connection to use with all activities
                ((MyApplication) LoginActivity.this.getApplication()).setConn(c);
                ((MyApplication) LoginActivity.this.getApplication()).setHostName(etAddress.getText().toString());
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                Toast.makeText(LoginActivity.this,errMessage,Toast.LENGTH_LONG).show();
                loginAttemptsCount++;
            }
        }
    }
}
