package com.example.suruz.helperActivity;

import static com.example.color.sessions.SessionManager.USER_LOGIN_SESSION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.color.R;
import com.example.color.models.UserModel;
import com.example.color.sessions.SessionManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import es.dmoral.toasty.Toasty;

public class UserProfile extends AppCompatActivity {

    Button editProfile;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    ImageView profileImg, changeImageBtn;
    TextView titleName, titleUsername, profileName, profileEmail, profilePhone, profileBirthday, profileGender, profileAddress, profilePassword;

    private static final int GALLERY_IMAGE_CODE = 200;
    Uri image_uri = null;

    SessionManager sessionManager;
    HashMap<String, String> userDetails;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        titleName = findViewById(R.id.titleName);
        titleUsername = findViewById(R.id.titleUsername);
        profileImg = findViewById(R.id.profileImg);
        changeImageBtn = findViewById(R.id.changeProfileImage);
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        profileBirthday = findViewById(R.id.profileBirthday);
        profileGender = findViewById(R.id.profileGender);
        profileAddress = findViewById(R.id.profileAddress);
        profilePassword = findViewById(R.id.profilePassword);

        editProfile = findViewById(R.id.editButton);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        sessionManager = new SessionManager(UserProfile.this, USER_LOGIN_SESSION);
        userDetails = sessionManager.getUsersDetailsFromSession();

        changeImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhoto();
            }
        });

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                intent.putExtra("name", profileName.getText().toString().trim());
                intent.putExtra("username", titleUsername.getText().toString().trim());
                intent.putExtra("email", profileEmail.getText().toString().trim());
                intent.putExtra("phone", profilePhone.getText().toString().trim());
                intent.putExtra("birthday", profileBirthday.getText().toString().trim());
                intent.putExtra("gender", profileGender.getText().toString().trim());
                intent.putExtra("address", profileAddress.getText().toString().trim());
                intent.putExtra("password", profilePassword.getText().toString().trim());
                startActivity(intent);
            }
        });
    }

    private void changePhoto() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_IMAGE_CODE) {
                if (data != null) {
                    image_uri = data.getData();
                    profileImg.setImageURI(image_uri);
                    saveProfilePhoto();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveProfilePhoto() {
        final StorageReference reference = storage.getReference().child("profile_photo").child(mAuth.getUid());

        reference.putFile(image_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toasty.success(getApplicationContext(), "Image saved", Toast.LENGTH_SHORT, true).show();
                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        database.getReference().child("users").child(mAuth.getUid()).child("profileImage").setValue(uri.toString());
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        database.getReference().child("users").child(mAuth.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            UserModel user = snapshot.getValue(UserModel.class);

                            Picasso.get()
                                    .load(user.getProfileImage())
                                    .placeholder(R.drawable.ic_launcher_background)
                                    .into(profileImg);
                            profileName.setText(userDetails.get(SessionManager.KEY_FULL_NAME));
                            titleName.setText(userDetails.get(SessionManager.KEY_FULL_NAME));
                            titleUsername.setText(userDetails.get(SessionManager.KEY_USER_NAME));
                            profileEmail.setText(userDetails.get(SessionManager.KEY_EMAIL));
                            profilePassword.setText(userDetails.get(SessionManager.KEY_PASSWORD));
                            profileGender.setText(userDetails.get(SessionManager.KEY_GENDER));
                            profilePhone.setText(userDetails.get(SessionManager.KEY_PHONE));
                            profileAddress.setText(userDetails.get(SessionManager.KEY_ADDRESS));
                            profileBirthday.setText(userDetails.get(SessionManager.KEY_BIRTHDAY));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}