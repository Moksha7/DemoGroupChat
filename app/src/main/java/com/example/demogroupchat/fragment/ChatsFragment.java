package com.example.demogroupchat.fragment;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demogroupchat.R;
import com.example.demogroupchat.activity.ChatActivity;
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


public class ChatsFragment extends Fragment {

    private RecyclerView chatsList;
    private DatabaseReference chatsRef, usersRef;
    String currentUserId, senderName;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        View privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsList = privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                        final String userIds = getRef(position).getKey();
                        final String[] profileImage = {"default"};
                        assert userIds != null;
                        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                senderName = Objects.requireNonNull(snapshot.child("name").getValue()).toString();

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("image")) {
                                    profileImage[0] = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                                    Picasso.get().load(profileImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                }

                                final String userName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                                holder.userName.setText(userName);

                                if (dataSnapshot.child("userState").hasChild("state")) {
                                    String state = Objects.requireNonNull(dataSnapshot.child("userState").child("state").getValue()).toString();
                                    String date = Objects.requireNonNull(dataSnapshot.child("userState").child("date").getValue()).toString();
                                    String time = Objects.requireNonNull(dataSnapshot.child("userState").child("time").getValue()).toString();

                                    if (state.equals("online")) {
                                        holder.userStatus.setText("Online");
                                        holder.userOnlineStatus.setImageResource(R.drawable.online);
                                    } else if (state.equals("offline")) {
                                        holder.userStatus.setText("Last Active\n" + date + " " + time);
                                        holder.userOnlineStatus.setImageResource(R.drawable.offline);
                                    }
                                } else {
                                    holder.userStatus.setText("Offline");
                                }


                                holder.itemView.setOnClickListener(v -> {
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visitUserId", userIds);
                                    chatIntent.putExtra("visitUserName", userName);
                                    chatIntent.putExtra("visitUserImage", profileImage[0]);
                                    chatIntent.putExtra("senderName", senderName);
                                    startActivity(chatIntent);
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        return new ChatsViewHolder(view);
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName;
        ImageView userOnlineStatus;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            userStatus = itemView.findViewById(R.id.users_status);
            userName = itemView.findViewById(R.id.users_profile_name);
            userOnlineStatus = itemView.findViewById(R.id.user_online_status);
        }
    }
}
