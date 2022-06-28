package com.example.Moody.Friend;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Moody.Activity.MainActivity;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.HashMap;


public class AddFriendActivity extends Activity {
    private static final String TAG = "AddFriendActivity";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private String fid=null;
    private Boolean check = false;

    private static final int REQUEST_CODE = 100;
    private static final int PERMISSIONS_REQUEST_CODE = 101 ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        setContentView(R.layout.activity_add_friend);

        final EditText friendEmail = (EditText)findViewById(R.id.chat_room_search);
        final Button addBtn = (Button)findViewById(R.id.friend_add_btn);
        Button searchBtn = (Button)findViewById(R.id.friend_search_btn);
        Button scanBtn = (Button)findViewById(R.id.friend_scan_btn);
        ImageView backBtn = (ImageView)findViewById(R.id.chatRoom_backBtn);
        final LinearLayout top1 = (LinearLayout)findViewById(R.id.top1);
        final LinearLayout layout1 = (LinearLayout)findViewById(R.id.layout1);
        final RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.layout2);
        final LinearLayout layout3 = (LinearLayout)findViewById(R.id.layout3);

        //엔터키 클릭 시 이메일 검색
        friendEmail.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if((event.getAction() == event.ACTION_DOWN) && (keyCode == event.KEYCODE_ENTER)){
                    //키보드 내리기
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow( friendEmail.getWindowToken(), 0);

                    String inEmail = friendEmail.getText().toString(); //이메일 입력받기
                    UsersInfo(inEmail);

                    return true;
                }
                return false;
            }
        });
/*
        //이메일 검색
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inEmail = friendEmail.getText().toString(); //이메일 입력받기
                UsersInfo(inEmail);
            }
        });*/

        //QR코드 스캔
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });

        //뒤로가기
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //입력받은 이메일로 유저id 가져오기
    public void UsersInfo(final String email) {
        database.getReference("userInfo").orderByChild("email/").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel user = null;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    user = dataSnapshot1.getValue(UserModel.class);
                    if(user.getEmail().equals(email)) {
                        fid = user.getUID();
                        break;
                    }else
                        fid = null;
                }
                if(fid == null) {
                    Toast toast = Toast.makeText(AddFriendActivity.this, "없는 유저입니다.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                    toast.show();
                }else{
                    //검색한 사용자 프로필 나타내기
                    LinearLayout shadow_layout = (LinearLayout) findViewById(R.id.shadow_layout);
                    ImageView friend_profile = (ImageView) findViewById(R.id.chat_image1);
                    TextView friend_name = (TextView) findViewById(R.id.user_sel_name);
                    Button add_btn = (Button) findViewById(R.id.friend_search_btn);

                    shadow_layout.setBackgroundResource(R.drawable.yj_profile_shadow);
                    friend_profile.setBackgroundResource(R.drawable.yj_profile_border_white);

                    shadow_layout.setVisibility(View.VISIBLE);

                    if(user.getRange().equals("all")){
                        Glide.with(friend_profile.getContext()).load(user.getProfile()).apply(new RequestOptions().circleCrop()).into(friend_profile);
                    }
                    else
                        Glide.with(friend_profile.getContext()).load("https://firebasestorage.googleapis.com/v0/b/graduation-project-ebb3e.appspot.com/o/Users%2FKakaoTalk_Photo_2020-10-31-18-19-11.png?alt=media&token=3b1cb276-6a10-47ee-a84a-126d85bf2469").apply(new RequestOptions().circleCrop()).into(friend_profile);
                    friend_name.setText(user.getName());
                    add_btn.setVisibility(View.VISIBLE);
                    add_btn.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View view)
                        {
                            FriendCheck();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

    }

    //친구목록에서 유저 중복 확인
    public void FriendCheck(){
        database.getReference("friend").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    if(dataSnapshot1.getKey().equals(fid)){
                        check = true;
                        break;
                    }
                }
                if(check == true){
                    Toast toast = Toast.makeText(AddFriendActivity.this,"이미 친구추가 되어 있어요",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                    toast.show();
                    Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
                    intent.putExtra("fragment","friend");
                    startActivity(intent);
                    finish();
                }else{
                    HashMap<String,Object> friend = new HashMap<String, Object>();
                    friend.put(fid,false);
                    database.getReference("friend").child(currentUser.getUid()).updateChildren(friend).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast toast = Toast.makeText(AddFriendActivity.this,"친구추가",Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                            toast.show();
                            Intent intent = new Intent(AddFriendActivity.this, MainActivity.class);
                            intent.putExtra("fragment","friend");
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    //QR 코드 스캔
    private void scan(){
        if (!hasPermission()){
            return;
        }
        Intent intent = new Intent(getApplication(), CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    scan();
                }
                break;
        }
    }
    private boolean hasPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
            return false;
        }else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    UsersInfo(result);
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    Toast.makeText(AddFriendActivity.this, "failed", Toast.LENGTH_LONG).show();
                }
            }
        }



    }
}