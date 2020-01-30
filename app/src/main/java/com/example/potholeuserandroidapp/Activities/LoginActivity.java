package com.example.potholeuserandroidapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.potholeuserandroidapp.Helpers.NetworkHelper;
import com.example.potholeuserandroidapp.Helpers.TokenHelper;
import com.example.potholeuserandroidapp.Interfaces.AuthApi;
import com.example.potholeuserandroidapp.Models.ResponseBody;
import com.example.potholeuserandroidapp.Models.User;
import com.example.potholeuserandroidapp.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    EditText emailEditText;
    EditText passwordEditText;

    Button loginButton;

    Button signupButton;
    CheckBox captchaButton;

    String TAG = CaptchaActivity.class.getSimpleName();
    String SITE_KEY = "6Le1-9MUAAAAAJY2k4C6RuTQgWCGEbp7hhfu80pX";
    String SECRET_KEY = "6Le1-9MUAAAAABMYbCTTZja16HVpxOIMvp6-DAX1";
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);



        emailEditText = findViewById(R.id.loginemailedittextid);
        passwordEditText = findViewById(R.id.loginpasswordedittextid);

        loginButton = findViewById(R.id.loginbuttonid);
        loginButton.setEnabled(false);


        signupButton = findViewById(R.id.loginsignupbuttonid);
        captchaButton = findViewById(R.id.button);

        captchaButton.setOnClickListener( this);

        queue = Volley.newRequestQueue(getApplicationContext());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if(!email.equals("") && !password.equals("")){

                    Retrofit retrofit = NetworkHelper.getRetrofitInstance(LoginActivity.this);

                    AuthApi authApi = retrofit.create(AuthApi.class);

                    Call<ResponseBody> loginCall = authApi.login(new User(email,password));

                    loginCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            if(response.isSuccessful()){

                                if(response.body()!=null && response.body().getMsg()!=null){
                                    TokenHelper.putRefreshToken(LoginActivity.this,response.body().getMsg());
                                }

                                SharedPreferences.Editor editor = getSharedPreferences("PREFERENCES",MODE_PRIVATE).edit();

                                editor.putBoolean("isLoggedIn",true);
                                editor.apply();

                                startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                                finish();
                            }else{

                                String message = "Something Went Wrong";


                                if(response.body()!=null && response.body().getMsg() != null){
                                    message = response.body().getMsg();
                                }

                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else{
                    Toast.makeText(LoginActivity.this, "Make sure Email and Password fields are filled", Toast.LENGTH_SHORT).show();
                }

            }
        });


        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,SignupActivity.class));
            }
        });



    }
    @Override
    public void onClick(View view) {
        if (captchaButton.isChecked()) {

            SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                    .addOnSuccessListener(this, new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                        @Override
                        public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                            if (!response.getTokenResult().isEmpty()) {
                                handleSiteVerify(response.getTokenResult());
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (e instanceof ApiException) {
                                ApiException apiException = (ApiException) e;
                                Log.d(TAG, "Error message: " +
                                        CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()));
                            } else {
                                Log.d(TAG, "Unknown type of error: " + e.getMessage());
                            }
                        }
                    });

        }
        else
        {
            loginButton.setEnabled(false);
        }
    }
    protected  void handleSiteVerify(final String responseToken){
        //it is google recaptcha siteverify server
        //you can place your server url
        String url = "https://www.google.com/recaptcha/api/siteverify";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(jsonObject.getBoolean("success")){
                                loginButton.setEnabled(true);

                                //code logic when captcha returns true Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getBoolean("success")),Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getString("error-codes")),Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception ex) {
                            Log.d(TAG, "JSON exception: " + ex.getMessage());

                        }
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "Error message: " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("secret", SECRET_KEY);
                params.put("response", responseToken);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }


}
