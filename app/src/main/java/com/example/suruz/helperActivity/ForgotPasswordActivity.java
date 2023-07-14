package com.example.suruz.helperActivity;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.color.R;
import com.example.color.starter.SignInActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import es.dmoral.toasty.Toasty;

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailEdt;
    Button sendBtn;

    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEdt = findViewById(R.id.email);
        sendBtn = findViewById(R.id.button);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recoverPassword();
            }
        });
    }

    private void recoverPassword() {
        String email = emailEdt.getText().toString().trim();

        if(email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toasty.error(getApplicationContext(), "Invalid email!", Toasty.LENGTH_SHORT, true).show();
        }
        else {
            Query query = database.getReference("users").orderByChild("email").equalTo(email);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toasty.info(getApplicationContext(), "Check your email", Toasty.LENGTH_SHORT, true).show();
                                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                                        finish();
                                    }
                                });
                    }
                    else{
                        Toasty.warning(getApplicationContext(), "Email don't found!", Toasty.LENGTH_SHORT, true).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
    }
}