package com.example.Moody.Chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.Moody.Activity.MainActivity;
import com.example.Moody.Model.ChatRoomModel;
import com.example.Moody.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ChangeRoomName extends Activity {
    private static final String TAG = "DeleteChatRoom";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private ChatRoomModel chatRoomModels = new ChatRoomModel();

    private String roomID;
    private String uid = currentUser.getUid();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.change_roomname);

        ImageView backBtn = (ImageView)findViewById(R.id.backBtn);
        TextView okBtn = (TextView)findViewById(R.id.change_ok);
        final EditText roomName = (EditText)findViewById(R.id.change_room_name);
        ImageView clearBtn = (ImageView) findViewById(R.id.clear_btn);

        roomID = getIntent().getStringExtra("roomID");
        ChatRoomInfo(roomID,roomName);


        /**
         * 뒤로가기 버튼
         */
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeRoomName.this,MainActivity.class);
                intent.putExtra("fragment","chat");
                startActivity(intent);
                finish();
            }
        });

        /**
         * 확인 버튼
         */
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = roomName.getText().toString();
                Map<String,Object> name = new HashMap<String,Object>();
                name.put(uid,newName);
                database.getReference("ChatRoom").child(roomID).child("users").updateChildren(name); //채팅방이름 업데이트

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("fragment","chat");
                startActivity(intent);
                finish();
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //roomName.setHint(chatRoomModels.getUsers().get(uid).toString());
                roomName.setText(null);
            }
        });
    }

    //채팅방 정보
    public void ChatRoomInfo(String roomid, final EditText et){
        database.getReference("ChatRoom").child(roomid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatRoomModels = snapshot.getValue(ChatRoomModel.class);
                et.setText(chatRoomModels.getUsers().get(uid).toString());//현재 채팅방 이름
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    //뒤로가기 버튼
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ChangeRoomName.this,MainActivity.class);
        intent.putExtra("fragment","chat");
        startActivity(intent);
        finish();
    }

}
