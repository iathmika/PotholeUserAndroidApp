package com.example.potholeuserandroidapp.Fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.potholeuserandroidapp.Activities.CaptchaActivity;
import com.example.potholeuserandroidapp.Activities.HomeActivity;
import com.example.potholeuserandroidapp.Activities.LoginActivity;
import com.example.potholeuserandroidapp.Activities.SignupActivity;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener{

    public Context context;

    private EditText firstnameEditText;
    private EditText lastnameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;

    private Button registerButton;

    private Button loginButton;
    CheckBox captchaButton1;

    String TAG = CaptchaActivity.class.getSimpleName();
    String SITE_KEY = "6Le1-9MUAAAAAJY2k4C6RuTQgWCGEbp7hhfu80pX";
    String SECRET_KEY = "6Le1-9MUAAAAABMYbCTTZja16HVpxOIMvp6-DAX1";
    RequestQueue queue;
    AppCompatActivity act=new AppCompatActivity();

    public RegisterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = getContext();


        firstnameEditText = view.findViewById(R.id.registerfirstnameedittextid);
        lastnameEditText = view.findViewById(R.id.registerlastnameedittextid);
        emailEditText = view.findViewById(R.id.registeremailedittextid);
        passwordEditText = view.findViewById(R.id.registerpasswordedittextid);
        confirmPasswordEditText = view.findViewById(R.id.registerconfirmpasswordedittextid);

        registerButton = view.findViewById(R.id.registerbuttonid);
        registerButton.setEnabled(false);


        loginButton = view.findViewById(R.id.registerloginbuttonid);
        captchaButton1 = view.findViewById(R.id.captchacheck);
        queue = Volley.newRequestQueue(context.getApplicationContext());

        captchaButton1.setOnClickListener( this);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstname = firstnameEditText.getText().toString().trim();
                final String lastname = lastnameEditText.getText().toString().trim();
                final String email = emailEditText.getText().toString().trim();
                final String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if(!firstname.equals("") && !lastname.equals("") && !email.equals("") && !password.equals("") && !confirmPassword.equals("")){
                    if(password.equals(confirmPassword)){


                        Retrofit retrofit = NetworkHelper.getRetrofitInstance(context);

                        AuthApi authApi = retrofit.create(AuthApi.class);

                        Call<ResponseBody> otpCall = authApi.getSignupOtp(new User(email));

                        otpCall.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                                if(response.isSuccessful()){
                                    ((SignupActivity) context).replaceFragment(new OtpFragment(firstname,lastname,email,password));
                                }else{
                                    String message = "Something Went Wrong";


                                    if(response.body()!=null && response.body().getMsg() != null){
                                        message = response.body().getMsg();
                                    }

                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Toast.makeText(context, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }else{
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(context, "Make sure all the fields are filled", Toast.LENGTH_SHORT).show();
                }


            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, LoginActivity.class));
            }
        });

    }

    @Override
    public void onClick(View view) {
        if (captchaButton1.isChecked()) {
            context = getContext();

            SafetyNet.getClient(context).verifyWithRecaptcha(SITE_KEY)
                    .addOnSuccessListener(new AppCompatActivity(), new OnSuccessListener<SafetyNetApi.RecaptchaTokenResponse>() {
                        @Override
                        public void onSuccess(SafetyNetApi.RecaptchaTokenResponse response) {
                            if (!response.getTokenResult().isEmpty()) {
                                handleSiteVerify(response.getTokenResult());
                            }
                        }
                    })
                    .addOnFailureListener(act, new OnFailureListener() {
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
            registerButton.setEnabled(false);
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
                                registerButton.setEnabled(true);

                                //code logic when captcha returns true Toast.makeText(getApplicationContext(),String.valueOf(jsonObject.getBoolean("success")),Toast.LENGTH_LONG).show();
                            }
                            else{
                                Toast.makeText(context,String.valueOf(jsonObject.getString("error-codes")),Toast.LENGTH_LONG).show();
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
