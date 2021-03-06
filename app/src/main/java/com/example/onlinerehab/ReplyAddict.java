package com.example.onlinerehab;

import android.content.Intent;
import android.os.Bundle;

import com.example.onlinerehab.Adapter.MessageAdapter;
import com.example.onlinerehab.Model.Chat;
import com.example.onlinerehab.Model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReplyAddict extends AppCompatActivity {
    TextView psyName;
    TextView psyPhone;
    TextView psySpeciality;
    DatabaseReference reference;
    Intent intent;
    ImageButton btn_send;
    EditText msg_send;
    FirebaseUser fuser;

    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_addict);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        psyName = findViewById(R.id.psy_cha);
        psyPhone = findViewById(R.id.psy_phone_cha);
        psySpeciality = findViewById(R.id.psy_spec_cha);

        btn_send = findViewById(R.id.btn_messge);
        msg_send = findViewById(R.id.text_mss);
        recyclerView = findViewById(R.id.recycler_addict);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        intent = getIntent();
        final String receiver = intent.getStringExtra("psy_name");

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msg_send.getText().toString().trim();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(), receiver, msg);
                }else{
                    Toast.makeText(ReplyAddict.this, "You cant send Empty Text",Toast.LENGTH_SHORT).show();
                }
                msg_send.setText("");
            }
        });
        reference = FirebaseDatabase.getInstance().getReference("psyInformation").child(receiver);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                psyName.setText(user.getPsy_name());
                psyPhone.setText(user.getPsy_phone());
                psySpeciality.setText(user.getPsy_speciality());

                readMessages(user.getPsy_name(), receiver);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);

    }
    private void readMessages(final String myId, final String userId){
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChat.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myId) && chat.getSender().equals(userId) ||
                            chat.getReceiver().equals(userId) && chat.getSender().equals(myId)){
                        mChat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(ReplyAddict.this, mChat);
                    recyclerView.setAdapter(messageAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}