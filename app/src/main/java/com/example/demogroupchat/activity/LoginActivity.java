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
import com.example.demogroupchat.notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private Button btnLogin;
    private EditText etUserEmail, etUserPassword;
    private TextView tvNeedNewAccountLink;
    private TextView tvForgotPasswordLink;
    private ProgressDialog loadingBar;

    private DatabaseReference userRef;
    String deviceToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");


        InitializeFields();

        tvNeedNewAccountLink.setOnClickListener(v -> SendUserToRegisterActivity());

        tvForgotPasswordLink.setOnClickListener(v -> ResetPassword());

        btnLogin.setOnClickListener(v -> AllowUserToLogin());


    }

    private void ResetPassword() {
        if (etUserEmail.getText().toString().matches(""))
            Toast.makeText(LoginActivity.this, "Please enter the account's email!", Toast.LENGTH_SHORT).show();
        else {
            mAuth.sendPasswordResetEmail(etUserEmail.getText().toString())
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(LoginActivity.this, "Failed to reset password", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void AllowUserToLogin() {
        String email = etUserEmail.getText().toString();
        String password = etUserPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_LONG).show();
        } else {
            loadingBar.setTitle(R.string.logging_in);
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task12 -> {
                                if (!task12.isSuccessful()) {
                                    Log.w("TAG", "Fetching FCM registration token failed", task12.getException());
                                    return;
                                }
                                deviceToken = task12.getResult();

                            });

                            userRef.child(currentUserId).child("deviceToken")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            SendUserToMainActivity();
                                            Toast.makeText(LoginActivity.this, "Logged", Toast.LENGTH_LONG).show();
                                            loadingBar.dismiss();
                                        }

                                    });


                            SendUserToMainActivity();
                            Toast.makeText(LoginActivity.this,
                                    "Logged in Successful...", Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();

                        } else {
                            String message = Objects.requireNonNull(task.getException()).toString();
                            Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_LONG).show();
                            loadingBar.dismiss();
                        }
                    });
        }

    }

    private void InitializeFields() {
        btnLogin = findViewById(R.id.login_button);
        etUserEmail = findViewById(R.id.login_email);
        etUserPassword = findViewById(R.id.login_password);
        tvNeedNewAccountLink = findViewById(R.id.need_new_account_link);
        tvForgotPasswordLink = findViewById(R.id.forgot_password_link);
        loadingBar = new ProgressDialog(this);
    }

    private void updateToken(String token) {
        Token token1 = new Token(token);
        userRef.child(currentUser.getUid()).setValue(token1);

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser != null) {
            SendUserToMainActivity();
        }
    }


    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
