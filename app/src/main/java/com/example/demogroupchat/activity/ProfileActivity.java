package com.example.demogroupchat.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.demogroupchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, senderUserId;
    private CircleImageView civUserProfileImage;
    private TextView tvUserProfileName, tvUserProfileStatus;
    private Button btnSendMessageRequest;
    private DatabaseReference userRef;
    private DatabaseReference chatRequestRef;
    private DatabaseReference notificationRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        senderUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        receiverUserId = getIntent().getExtras().get("visitUserId").toString();


        civUserProfileImage = findViewById(R.id.visit_profile_image);
        tvUserProfileName = findViewById(R.id.visit_user_name);
        tvUserProfileStatus = findViewById(R.id.visit_profile_status);
        btnSendMessageRequest = findViewById(R.id.send_message_request_button);
        btnSendMessageRequest.setOnClickListener(v -> SendChatRequest());

        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {

        if (senderUserId.equals(receiverUserId)) {
            btnSendMessageRequest.setEnabled(false);
            btnSendMessageRequest.setVisibility(View.INVISIBLE);
        }

        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(civUserProfileImage);
                }

                String userName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String userStatus = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();

                tvUserProfileName.setText(userName);
                tvUserProfileStatus.setText(userStatus);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void SendChatRequest() {
        if (btnSendMessageRequest.getText().equals("Cancel Invited")) {
            chatRequestRef.child(senderUserId).child(receiverUserId)
                    .removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    chatRequestRef.child(receiverUserId).child(senderUserId)
                            .removeValue();
                }
            });
            btnSendMessageRequest.setText(R.string.add_friend);
            return;
        }
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .child("requestType").setValue("sent")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatRequestRef.child(receiverUserId).child(senderUserId)
                                .child("requestType").setValue("received")
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                                        chatNotificationMap.put("from", senderUserId);
                                        chatNotificationMap.put("type", "request");
                                        notificationRef.child(receiverUserId).push()
                                                .setValue(chatNotificationMap);
                                        btnSendMessageRequest.setText(R.string.cancel_invite);
                                    }
                                });
                    }
                });
    }
}
