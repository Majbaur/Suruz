package com.example.suruz.starter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.color.MainActivity;
import com.example.color.R;
import com.example.color.helperActivity.ForgotPasswordActivity;
import com.example.color.sessions.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class SignInActivity extends AppCompatActivity {

    Button signIn;
    TextView signUp, forgetPassword;
    EditText signin_email, signin_password;
    CheckBox rememberMe;

    private FirebaseAuth mAuth;

    SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signIn = findViewById(R.id.signin_button);
        signUp = findViewById(R.id.signup_button);
        forgetPassword = findViewById(R.id.forgot_password);
        signin_email = findViewById(R.id.signin_email);
        signin_password = findViewById(R.id.signin_password);
        rememberMe = findViewById(R.id.remember_me);

        sessionManager = new SessionManager(SignInActivity.this, SessionManager.REMEMBER_ME_SESSION);
        if(sessionManager.checkRememberMe()){
            HashMap<String, String> rememberMeDetails = sessionManager.getRememberMeDetailsFromSession();
            signin_email.setText(rememberMeDetails.get(SessionManager.KEY_REMEMBER_ME_EMAIL));
            signin_password.setText(rememberMeDetails.get(SessionManager.KEY_REMEMBER_ME_PASSWORD));
        }

        mAuth = FirebaseAuth.getInstance();

        signIn.setOnClickListener(v -> {
            userSignIn();
        });

        signUp.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        forgetPassword.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ForgotPasswordActivity.class)));
    }

    private void userSignIn() {

        if (!isConnected(this)) {
            showNoInternetDialog();
        }

        String email = signin_email.getText().toString();
        String password = signin_password.getText().toString();

        if (rememberMe.isChecked()) {
            sessionManager.createRememberMeSession(email, password);
        }
        else {
            sessionManager.logoutUserFromSession();
        }

        if (email.isEmpty()) {
            signin_email.setError("Email cannot be empty");
        }
        if (password.isEmpty()) {
            signin_password.setError("Password cannot be empty");
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                if(mAuth.getCurrentUser().isEmailVerified()){
                                    Toasty.success(getApplicationContext(), "User login successfully", Toast.LENGTH_SHORT, true).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                                else{
                                    Toasty.warning(getApplicationContext(), "Please, verify email first!", Toast.LENGTH_SHORT, true).show();
                                }
                            } else {
                                Toasty.error(getApplicationContext(), "SignIn Failed" + task.getException(), Toast.LENGTH_SHORT, true).show();
                            }
                        }
                    });
        }
    }

    private boolean isConnected(SignInActivity signInActivity) {

        ConnectivityManager connectivityManager = (ConnectivityManager) signInActivity.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return (wifiInfo != null && wifiInfo.isConnected()) || mobileInfo != null && mobileInfo.isConnected();
    }

    private void showNoInternetDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(SignInActivity.this);
        dialog.setMessage("Please connect to the internet to proceed further")
                .setCancelable(false)
                .setPositiveButton("Connect", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null && user.isEmailVerified()){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            this.finish();
        }
    }
}