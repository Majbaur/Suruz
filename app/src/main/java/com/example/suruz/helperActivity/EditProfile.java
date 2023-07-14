package com.example.suruz.helperActivity;

import static com.example.color.sessions.SessionManager.USER_LOGIN_SESSION;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import com.chaos.view.PinView;
import com.example.color.R;
import com.example.color.models.UserModel;
import com.example.color.sessions.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import es.dmoral.toasty.Toasty;


public class EditProfile extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    EditText profileName, profileGender, profilePassword, profileAddress;
    Button save;
    TextView profilePhone, profileBirthday, profileUsername, profileEmail;
    ImageView calender, address, phone;

    String dob, codeBySystem;
    String regPhone;
    PinView pinView;
    Dialog dialog;

    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profileName = findViewById(R.id.profileName);
        profileUsername = findViewById(R.id.profileUsername);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        profileBirthday = findViewById(R.id.profileBirthday);
        profileGender = findViewById(R.id.profileGender);
        profileAddress = findViewById(R.id.profileAddress);
        profilePassword = findViewById(R.id.profilePassword);
        save = findViewById(R.id.saveButton);
        calender = findViewById(R.id.calendar);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Phone number verification");

        View view = getLayoutInflater().inflate(R.layout.phone_number_dialog, null);

        final AppCompatButton sendOTPBtn = view.findViewById(R.id.send_otp);
        final AppCompatButton cancelBtn = view.findViewById(R.id.cancel);
        final AppCompatButton verifyOTPBtn = view.findViewById(R.id.verify_otp);
        final CountryCodePicker countryCode = view.findViewById(R.id.country_code);
        final TextInputEditText phoneNumberEdt = view.findViewById(R.id.phone_number);
        pinView = view.findViewById(R.id.pin_view);

        sendOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = phoneNumberEdt.getText().toString();
                regPhone = countryCode.getSelectedCountryCodeWithPlus() + number;
                sendVerificationCodeToUser(regPhone);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pinView.getText().toString().isEmpty()) {
                    Toasty.error(getApplicationContext(), "Blank field can't be processed!", Toast.LENGTH_SHORT, true).show();
                } else if (pinView.getText().toString().length() < 6) {
                    Toasty.error(getApplicationContext(), "Invalid OTP!", Toast.LENGTH_SHORT, true).show();
                } else {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, pinView.getText().toString());
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        builder.setView(view);
        dialog = builder.create();

        calender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new com.example.color.models.DatePicker();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInformation();
            }
        });
    }

    private void saveInformation(){

        String _name = profileName.getText().toString();
        String _username = profileUsername.getText().toString();
        String _email = profileEmail.getText().toString();
        String _phone = profilePhone.getText().toString();
        String _birthday = profileBirthday.getText().toString();
        String _gender = profileGender.getText().toString();
        String _address = profileAddress.getText().toString();
        String _password = profilePassword.getText().toString();

        UserModel user = new UserModel();

        user.setName(_name);
        user.setUsername(_username);
        user.setEmail(_email);
        user.setPhone(_phone);
        user.setBirthday(_birthday);
        user.setGender(_gender);
        user.setAddress(_address);
        user.setPassword(_password);

        FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(FirebaseAuth.getInstance().getUid())
                .setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(EditProfile.this, "User information updated!", Toast.LENGTH_SHORT).show();

                        SessionManager sessionManager = new SessionManager(EditProfile.this, USER_LOGIN_SESSION);
                        sessionManager.createLoginSession(_name, _username, _email, _password, _address, _gender, _phone, _birthday);

                        startActivity(new Intent(getApplicationContext(), UserProfile.class));
                        finish();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        String name = getIntent().getStringExtra("name");
        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String phone = getIntent().getStringExtra("phone");
        String birthday = getIntent().getStringExtra("birthday");
        String gender = getIntent().getStringExtra("gender");
        String address = getIntent().getStringExtra("address");
        String password = getIntent().getStringExtra("password");

        profileName.setText(name);
        profileUsername.setText(username);
        profileEmail.setText(email);
        profilePhone.setText(phone);
        profileBirthday.setText(birthday);
        profileGender.setText(gender);
        profileAddress.setText(address);
        profilePassword.setText(password);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        dob = DateFormat.getDateInstance().format(c.getTime());
        profileBirthday.setText(dob);
    }

    private void sendVerificationCodeToUser(String phone) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    codeBySystem = s;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if (code != null) {
                        pinView.setText(code);
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                }
            };

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toasty.success(getApplicationContext(), "Verification successful.", Toast.LENGTH_SHORT, true).show();
                            profilePhone.setText(regPhone);
                            dialog.dismiss();
                        }
                        else {
                            Toasty.error(getApplicationContext(), "Verification is not successful. Try again!", Toast.LENGTH_SHORT, true).show();
                        }
                    }
                });
    }
}