package com.example.demogroupchat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demogroupchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;


public class RegisterActivity extends AppCompatActivity {

    private Button   btnCreateAccount;
    private EditText etUserEmail, etUserPassword;
    private TextView tvAlreadyHaveAccount;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ProgressDialog loadingBar;
    String deviceToken;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        InitializeFields();


        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        tvAlreadyHaveAccount.setOnClickListener(v -> SendUserToLoginActivity());

        btnCreateAccount.setOnClickListener(v -> CreateNewAccount());
    }

    private void InitializeFields() {
        btnCreateAccount = findViewById(R.id.signup_button);
        etUserEmail = findViewById(R.id.signup_email);
        etUserPassword = findViewById(R.id.signup_password);
        tvAlreadyHaveAccount = findViewById(R.id.already_have_account);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void CreateNewAccount() {
        String email = etUserEmail.getText().toString();
        String password = etUserPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_LONG).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_LONG).show();
        }
        else {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait, while we are creating new account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task12 -> {
                                if (!task12.isSuccessful()) {
                                    Log.w("TAG", "Fetching FCM registration token failed", task12.getException());
                                    return;
                                }
                                deviceToken = task12.getResult();
                            });
                            String currentUserID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
                            rootRef.child("Users").child(currentUserID).setValue("");
                            rootRef.child("Users").child(currentUserID).child("deviceToken")
                            .setValue(deviceToken);


                            SendUserToMainActivity();
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully...", Toast.LENGTH_LONG).show();
                        }
                        else {
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(RegisterActivity.this, "Error : " + message, Toast.LENGTH_LONG).show();
                        }
                        loadingBar.dismiss();
                    });
        }
    }


}
