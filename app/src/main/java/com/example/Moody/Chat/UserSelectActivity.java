package com.example.Moody.Chat;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Moody.Activity.MainActivity;
import com.example.Moody.Model.ChatRoomModel;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserSelectActivity extends Activity {
    private static final String TAG = "UserSelectActivity";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private RecyclerView uRecyclerView;
    private UserSelectAdapter uAdapter;

    private ArrayList<UserModel> userModels = new ArrayList<UserModel>();
    private ArrayList<String> fid = new ArrayList<String>();
    private ChatRoomModel room = new ChatRoomModel();

    private String uid;
    private String myName;
    private ArrayList<String> rec = new ArrayList<>();
    private ArrayList<String> name = new ArrayList<>();
    private ArrayList<String> recImage = new ArrayList<>();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        setContentView(R.layout.activity_chat_add);

        FriendListDisplay(); //???????????? ????????????

        database = FirebaseDatabase.getInstance();
        uid = currentUser.getUid();

        ImageView myImageView = (ImageView)findViewById(R.id.my_image);
        TextView myNameView = (TextView)findViewById(R.id.my_name);
        LinearLayout okBtn = (LinearLayout) findViewById(R.id.sel_ok_btn);
        Button backBtn = (Button)findViewById(R.id.chat_add_backBtn);

        myInfo(myNameView,myImageView);//????????? ????????????

        uRecyclerView = (RecyclerView)findViewById(R.id.sel_recyclerview);
        uRecyclerView.setHasFixedSize(true); //?????????????????? ?????? ??????
        uRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //?????????????????? ?????????
        uAdapter = new UserSelectAdapter(userModels);
        uRecyclerView.setAdapter(uAdapter);

        //?????? ?????? ??????
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserSelectActivity.this, MainActivity.class);
                intent.putExtra("fragment","chat");
                startActivity(intent);
                finish();
            }
        });

        //????????? ?????? ?????? ?????????
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Select();
                String roomNames = "";

                //????????? id ??????
                Map<String, Object> map = new HashMap<String,Object>();
                final String roomkey = database.getReference().child("ChatRoom").push().getKey();
                room.setRoomID(roomkey);
                database.getReference().child("ChatRoom").updateChildren(map);

                //?????? ???????????? ?????? ?????????
                HashMap<String,Object>users = new HashMap<String, Object>(); // {??????id,????????? ??????}
                if(rec.size()==1){ //?????? ????????? ??????
                    users.put(uid,name.get(0));
                    users.put(rec.get(0),myName);
                }else{//?????? ????????? ??????
                    for(int i = 0; i<name.size(); i++)
                        roomNames += name.get(i)+", ";
                    roomNames += myName;
                    for(int i =0;i<rec.size();i++)
                        users.put(rec.get(i),roomNames);
                    users.put(uid,roomNames);
                }

                //DB??? roomID??? ?????? ?????? ??????
                final Map<String,Object> objectMap = new HashMap<String, Object>();
                objectMap.put("roomID",roomkey);
                objectMap.put("users",users);
                objectMap.put("lastTime", ServerValue.TIMESTAMP);//???????????? ?????? ??????
                objectMap.put("lastMsg","");
                objectMap.put("group", "false");

                //DB??? ??????
                database.getReference().child("ChatRoom").child(roomkey).setValue(objectMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Toast.makeText(UserSelectActivity.this, "????????? ??????",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
                        if(rec.size()<2){
                            //???????????????
                            intent.putExtra("roomid",roomkey);
                            intent.putExtra("receiver", rec.get(0));
                            intent.putExtra("name", name.get(0));
                            intent.putExtra("recProfile",recImage.get(0));
                            intent.putExtra("check","1");
                            intent.putExtra("group", false);

                            startActivity(intent);
                            finish();

                        }else{
                            //?????? ?????????
                            String roomNames ="";
                            for(int i = 0; i<name.size(); i++){
                                roomNames += name.get(i)+", ";
                                if(i>2){
                                    roomNames += "... ";
                                    break;
                                }
                            }
                            roomNames += myName;
                            intent.putExtra("roomid",roomkey);
                            intent.putExtra("name",roomNames+" ("+(rec.size()+1)+")");
                            intent.putExtra("check","2");
                            intent.putExtra("group", true);
                            database.getReference("ChatRoom").child(roomkey).child("group").setValue(true);

                            startActivity(intent);
                            finish();
                        }

                    }

                });

            }
        });

    }

    public void Select(){

        for(int i = 0;i<userModels.size();i++){
            Boolean check = userModels.get(i).getCheck();
            if(check != null){
                rec.add(userModels.get(i).getUID());
                name.add(userModels.get(i).getName());
                recImage.add(userModels.get(i).getProfile());
                //Log.d(TAG, "Select: name="+name);
            }
        }

    }

    //?????????
    public void myInfo(final TextView name, final ImageView image){
        database.getReference("userInfo").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um = dataSnapshot.getValue(UserModel.class);
                name.setText(um.getName()); //????????? ????????? ????????? ??????
                if (um.getRange().equals("all")) {
                    if (!um.getProfile().equals(""))
                        Glide.with(image.getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).into(image);
                }
                else if (um.getRange().equals("friend")) {
                    image.setBackgroundResource(R.drawable.yj_profile_border);
                    if (!um.getProfile().equals(""))
                        Glide.with(image.getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).into(image);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    public void FriendListDisplay(){
        //?????????????????? ?????? id ????????????
        database.getReference("friend").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    fid.add(dataSnapshot1.getKey());
                    //Log.d(TAG, "onDataChange: dataSnapshot1.getKey()="+dataSnapshot1.getKey());
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        //?????? ?????? ????????????
        database.getReference("userInfo").orderByChild("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userModels.clear();
                String myid = currentUser.getUid();
                for(DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    UserModel users = dataSnapshot1.getValue(UserModel.class);
                    for(int i=0;i<fid.size();i++){
                        if(users.getUID().equals(fid.get(i))){
                            userModels.add(users);
                        }
                    }
                }
                uAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        //????????? ????????????
        database.getReference("userInfo").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um = dataSnapshot.getValue(UserModel.class);
                myName = um.getName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(UserSelectActivity.this,MainActivity.class);
        intent.putExtra("fragment","chat");
        startActivity(intent);
        finish();
    }

    //==============================================================================================================//

    public class UserSelectAdapter extends RecyclerView.Adapter<UserSelectAdapter.ViewHolder>{
        private static final String TAG = "UserSelectAdapter";

        private ArrayList<UserModel> userList; //?????? ?????? ?????????

        public UserSelectAdapter(ArrayList<UserModel> list){
            this.userList = list;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView selImage;
            public TextView selName;
            public CheckBox selBox;

            ViewHolder(View view) {
                super(view);
                selImage = (ImageView)view.findViewById(R.id.chat_image1);
                selName = (TextView)view.findViewById(R.id.user_sel_name);
                selBox = (CheckBox)view.findViewById(R.id.user_sel_checkbox);
            }
        }

        @NonNull
        @Override
        public UserSelectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_user_select,parent,false);

            UserSelectAdapter.ViewHolder vh = new UserSelectAdapter.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull UserSelectAdapter.ViewHolder holder, final int position) {
            holder.selName.setText(userList.get(position).getName());
            //????????? ?????????
            if (userList.get(position).getRange().equals("all")) {
                if (userList.get(position).getProfile() != null && !userList.get(position).getProfile().equals(""))
                    Glide.with(holder.selImage.getContext()).load(userList.get(position).getProfile()).apply(new RequestOptions().circleCrop()).into(holder.selImage);
            } else if (userList.get(position).getRange().equals("friend")) {
                if (userList.get(position).getLiked() != null) {
                    for (String key : userList.get(position).getLiked().keySet()) {
                        if (key.equals(uid)) {
                            holder.selImage.setBackgroundResource(R.drawable.yj_profile_border);
                            if (userList.get(position).getProfile() != null && !userList.get(position).getProfile().equals(""))
                                Glide.with(holder.selImage.getContext()).load(userList.get(position).getProfile()).apply(new RequestOptions().circleCrop()).into(holder.selImage);
                        }
                    }
                }
            }
            holder.selBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox)v;
                    userModels.get(position).setCheck(cb.isChecked());
                    //Toast.makeText(v.getContext(),"????????? : "+userList.get(position).getUID()+", "+cb.isChecked(),Toast.LENGTH_SHORT).show();
                }
            });


        }

        @Override
        public int getItemCount() {
            return userList.size();
        }
    }

}