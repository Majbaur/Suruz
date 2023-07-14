package com.example.suruz;

import static com.example.suruz.sessions.SessionManager.USER_LOGIN_SESSION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.suruz.helperActivity.UserProfile;
import com.example.suruz.models.UserModel;
import com.example.suruz.services.AboutActivity;
import com.example.suruz.services.BlogActivity;
import com.example.suruz.services.ClassifyActivity;
import com.example.suruz.services.ColorsActivity;
import com.example.suruz.services.MapActivity;
import com.example.suruz.services.QnAActivity;
import com.example.suruz.sessions.SessionManager;
import com.example.suruz.starter.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    ImageView notification, signOut;
    CircleImageView userImage;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;

    SessionManager sessionManager;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView blog = findViewById(R.id.blog);
        CardView map = findViewById(R.id.map);
        CardView classify = findViewById(R.id.classify);
        CardView colors = findViewById(R.id.colors);
        CardView qna = findViewById(R.id.qna);
        CardView about = findViewById(R.id.about_me);
        userImage = findViewById(R.id.user_image);

        notification = findViewById(R.id.notification);
        signOut = findViewById(R.id.log_out);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        sessionManager = new SessionManager(MainActivity.this, USER_LOGIN_SESSION);

        blog.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), BlogActivity.class)));
        map.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), MapActivity.class)));
        classify.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ClassifyActivity.class)));
        colors.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ColorsActivity.class)));
        qna.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), QnAActivity.class)));
        about.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), AboutActivity.class)));
        userImage.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), UserProfile.class)));

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut() {
        AlertDialog.Builder signOutAlert = new AlertDialog.Builder(MainActivity.this);
        signOutAlert.setTitle("Log out")
                .setMessage("Are you sure to log out?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    mAuth.signOut();
                    sessionManager.logoutUserFromSession();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialogInterface, i) -> {});

        AlertDialog dialog = signOutAlert.create();
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        database.getReference().child("users").child(Objects.requireNonNull(mAuth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            UserModel user = snapshot.getValue(UserModel.class);
                            Picasso.get()
                                    .load(user.getProfileImage())
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(userImage);

                            String _name = user.getName();
                            String _username = user.getUsername();
                            String _email = user.getEmail();
                            String _password = user.getPassword();
                            String _address = user.getAddress();
                            String _phone = user.getPhone();
                            String _birthday = user.getBirthday();
                            String _gender = user.getGender();

                            sessionManager.createLoginSession(_name, _username, _email, _password, _address, _gender, _phone, _birthday);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}