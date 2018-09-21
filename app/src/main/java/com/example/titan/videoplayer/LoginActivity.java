package com.example.titan.videoplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    // UI references.

    private EditText employee_id, password;
    private ProgressBar progressBar;
    private TextInputLayout textInputEmployeeCode, textInputPassword;
    private TextView forgot_password;

    static String token;
    //String userName, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        employee_id = findViewById(R.id.edittext_employee_code);
        password = findViewById(R.id.edittext_password);

        textInputEmployeeCode = findViewById(R.id.employee_code);
        textInputPassword = findViewById(R.id.password);

        progressBar = findViewById(R.id.login_progress);



        Button login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // Forcing the App to crash to test Firebase crash reporting
                //Crashlytics.getInstance().crash();

                hideKeyboard(view);

                    String employeeId = employee_id.getText().toString().trim();
//                int final_eid;
//                try {
//                    final_eid = Integer.parseInt(employeeId);
//                } catch (NumberFormatException nfe) {
//                    final_eid = 0;
//                }

                String pwd = password.getText().toString().trim();

                if (!employeeId.isEmpty() && !pwd.isEmpty())
                {
//                    Log.d("Em length",employeeId.length()+"");
//                    if ( employeeId.length() < 4)
//                        employee_id.setError("Enter a valid Emp Code");
//                    else
//                    {
//                        if(employeeId.length()< 7)
//
//                        {   String zeros="";
//                            int length= 7-employee_id.length();
//                            for( int i =0; i< length; i++){
//                                zeros= zeros+"0";
//                            }
//                            employeeId = zeros+employeeId;
//                            Log.d("Employee id",employeeId);
//
//                        }
//                        new SyncData().execute(confirmInput(employeeId));
//                    }

                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();


                   // progressBar.setVisibility(view.VISIBLE);
                    //Toast.makeText(LoginActivity.this, "You're logged in", Toast.LENGTH_LONG).show();

                } else {
                    if (employeeId.isEmpty())
                        employee_id.setError("This field cannot be empty");
                    if (pwd.isEmpty())
                        password.setError("This field cannot be empty");
                }

             }
        });





    }


    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }





}

