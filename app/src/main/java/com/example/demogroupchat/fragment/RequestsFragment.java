package com.example.demogroupchat.fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demogroupchat.R;
import com.example.demogroupchat.pojo.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {

    private RecyclerView mRequestsList;
    private DatabaseReference chatRequestsRef, userRef, contactsRef;
    private String currentUserId;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        View requestsFragmentView = inflater.inflate(R.layout.fragment_requests, container, false);
        mRequestsList = requestsFragmentView.findViewById(R.id.chat_requests_list);
        mRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return requestsFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatRequestsRef.child(currentUserId), Contacts.class)
                        .build();

        final FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.requests_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.requests_cancel_btn).setVisibility(View.VISIBLE);

                        final String listUserId = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("requestType").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    String type = Objects.requireNonNull(dataSnapshot.getValue()).toString();
                                    if (type.equals("received")) {
                                        assert listUserId != null;
                                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestProfileImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }

                                                final String requestUserName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(R.string.wanna_connect);

                                                //test onClick Button accept or decline
                                                holder.itemView.findViewById(R.id.requests_accept_btn)
                                                        .setOnClickListener(v -> contactsRef.child(currentUserId).child(listUserId).child("Contact")
                                                                .setValue("Saved").addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()) {
                                                                        contactsRef.child(listUserId).child(currentUserId).child("Contact")
                                                                                .setValue("Saved").addOnCompleteListener(task1 -> {
                                                                            if (task1.isSuccessful()) {
                                                                                chatRequestsRef.child(currentUserId).child(listUserId)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(task11 -> {
                                                                                            if (task11.isSuccessful()) {
                                                                                                chatRequestsRef.child(listUserId).child(currentUserId)
                                                                                                        .removeValue().addOnCompleteListener(task111 -> Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show());
                                                                                            }
                                                                                        });
                                                                            }
                                                                        });

                                                                    }
                                                                }));

                                                holder.itemView.findViewById(R.id.requests_cancel_btn).setOnClickListener(v -> chatRequestsRef.child(currentUserId).child(listUserId)
                                                        .removeValue().addOnCompleteListener(task -> {
                                                            if (task.isSuccessful()) {
                                                                chatRequestsRef.child(listUserId).child(currentUserId)
                                                                        .removeValue().addOnCompleteListener(task12 -> {
                                                                    if (task12.isSuccessful()) {
                                                                        Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }));


                                                holder.itemView.setOnClickListener(v -> {
                                                    CharSequence[] options1 = new CharSequence[]{
                                                            "Accept",
                                                            "Cancel"
                                                    };
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle(requestUserName + " Chat Request");
                                                    builder.setItems(options1, (dialog, which) -> {
                                                        if (which == 0) {
                                                            contactsRef.child(currentUserId).child(listUserId).child("Contact")
                                                                    .setValue("Saved").addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    contactsRef.child(listUserId).child(currentUserId).child("Contact")
                                                                            .setValue("Saved").addOnCompleteListener(task13 -> {
                                                                        if (task13.isSuccessful()) {
                                                                            chatRequestsRef.child(currentUserId).child(listUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(task131 -> {
                                                                                        if (task131.isSuccessful()) {
                                                                                            chatRequestsRef.child(listUserId).child(currentUserId)
                                                                                                    .removeValue().addOnCompleteListener(task1311 -> Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show());
                                                                                        }
                                                                                    });
                                                                        }
                                                                    });

                                                                }
                                                            });

                                                        } else if (which == 1) {
                                                            chatRequestsRef.child(currentUserId).child(listUserId)
                                                                    .removeValue().addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    chatRequestsRef.child(listUserId).child(currentUserId)
                                                                            .removeValue().addOnCompleteListener(task14 -> {
                                                                        if (task14.isSuccessful()) {
                                                                            Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                    builder.show();
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        Button requestSendBtn = holder.itemView.findViewById(R.id.requests_accept_btn);
                                        requestSendBtn.setText(R.string.req_sent);
                                        holder.itemView.findViewById(R.id.requests_cancel_btn).setVisibility(View.INVISIBLE);
                                        assert listUserId != null;
                                        userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                            @SuppressLint("SetTextI18n")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {
                                                    final String requestProfileImage = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                                    Picasso.get().load(requestProfileImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }

                                                final String requestUserName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText(R.string.sent_req_to + requestUserName);

                                                holder.itemView.setOnClickListener(v -> {
                                                    CharSequence[] options12 = new CharSequence[]{
                                                            "Cancel Chat Request"
                                                    };
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("Already sent request");
                                                    builder.setItems(options12, (dialog, which) -> {
                                                        if (which == 0) {
                                                            chatRequestsRef.child(currentUserId).child(listUserId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(task -> {
                                                                        if (task.isSuccessful()) {
                                                                            chatRequestsRef.child(listUserId)
                                                                                    .child(currentUserId).removeValue().addOnCompleteListener(task15 -> {
                                                                                if (task15.isSuccessful()) {
                                                                                    Toast.makeText(getContext(), "You have cancelled  the chat request", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }

                                                                    });
                                                        }
                                                    });
                                                    builder.show();
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new RequestsViewHolder(view);
                    }
                };
        mRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.users_profile_name);
            userStatus = itemView.findViewById(R.id.users_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.requests_accept_btn);
            cancelButton = itemView.findViewById(R.id.requests_cancel_btn);

        }
    }
}
