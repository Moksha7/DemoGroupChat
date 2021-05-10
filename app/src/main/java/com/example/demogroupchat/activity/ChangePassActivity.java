package com.example.demogroupchat.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.demogroupchat.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassActivity extends AppCompatActivity {
    EditText etEmail, etOldPass, etNewPass;
    Button btnUpdate;
    FirebaseUser user;
    AuthCredential credential;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        InitializeField();

        btnUpdate.setOnClickListener(v -> ChangePassword());


    }

    private void ChangePassword() {
        String userEmail = etEmail.getText().toString();
        String userOldPass = etOldPass.getText().toString();
        final String userNewPass = etNewPass.getText().toString();

        credential = EmailAuthProvider.getCredential(userEmail, userOldPass);

        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(userNewPass).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(ChangePassActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ChangePassActivity.this, "Error password not updated", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ChangePassActivity.this,"Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitializeField() {
        etEmail =  findViewById(R.id.change_pass_email);
        etOldPass =  findViewById(R.id.change_pass_password);
        etNewPass =  findViewById(R.id.change_pass_new_password);
        btnUpdate =  findViewById(R.id.change_pass_button);
    }
}
