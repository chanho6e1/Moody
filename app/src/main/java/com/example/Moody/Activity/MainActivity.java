package com.example.Moody.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.Moody.Background.DBHelper;
import com.example.Moody.Chat.FragmentChatting;
import com.example.Moody.Feed.FragmentFeed;
import com.example.Moody.Friend.FragmentFriend;
import com.example.Moody.Setting.FragmentSetting;
import com.example.Moody.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private EditText search_btn;
    private MenuItem sitem;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()){
                case R.id.menu_friend:
                    replaceFragment(FragmentFriend.newInstance());
                    return true;
                case R.id.menu_chatting:
                    replaceFragment(FragmentChatting.newInstance());
                    return true;
                case R.id.menu_feed:
                    replaceFragment(FragmentFeed.newInstance());
                    return true;
                case R.id.menu_setting:
                    replaceFragment(FragmentSetting.newInstance());
                    return true;
            }
            return true;
        }

    };


    //??????????????? ????????? ??????
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content,fragment).commit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        String file=mAuth.getUid()+".db";
        LoginActivity.dbHelper = new DBHelper(MainActivity.this, file, null, 1);

        //????????? ?????????
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }


        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        search_btn=(EditText) findViewById(R.id.chat_room_search);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //??????????????? ?????? ??????
        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.navigation_bar);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //????????? ???????????? ??????????????? ??????
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        String fragment = getIntent().getStringExtra("fragment");
        if(fragment == null){
            fragmentTransaction.add(R.id.content,FragmentFriend.newInstance()).commit();
        }else{
            switch (fragment){
                case "friend":
                    sitem = navigationView.getMenu().getItem(0);
                    sitem.setChecked(true);
                    fragmentTransaction.add(R.id.content, FragmentFriend.newInstance()).commit();
                    break;
                case "chat":
                    sitem = navigationView.getMenu().getItem(1);
                    sitem.setChecked(true);
                    fragmentTransaction.add(R.id.content,FragmentChatting.newInstance()).commit();
                    break;
                case "feed":
                    sitem = navigationView.getMenu().getItem(2);
                    sitem.setChecked(true);
                    fragmentTransaction.add(R.id.content,FragmentFeed.newInstance()).commit();
                    break;
                case "setting":

                default:
                    sitem = navigationView.getMenu().getItem(3);
                    sitem.setChecked(true);
                    fragmentTransaction.add(R.id.content,FragmentSetting.newInstance()).commit();
                    break;
            }
        }

    }

    //????????? ????????? currentUser??? ?????? ?????? ????????? ??????, ????????? ????????? ???????????? ??????
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        checkOnline(); //user connect ??????
    }

    //????????? connection ??????
    public void checkOnline(){
        final String TAG = "MainActivity";
        String userUid = currentUser.getUid();

        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/connection");
        final DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/lastOnline");

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");

        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {//??????????????? ??????
                    Log.d(TAG, "connected");

                    myConnectionsRef.setValue(true);
                    myConnectionsRef.onDisconnect().setValue(false);
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                } else {
                    Log.d(TAG, "not connected");
                    // ?????? ?????? ?????????
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Listener was cancelled");
            }
        });

    }

    //????????? ?????????
    /*public boolean onTouchEvent(MotionEvent event) {
        EditText email = (EditText)findViewById(R.id.chat_room_search);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
        return true;
    }*/
    //???????????? ??????

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Toast.makeText(MainActivity.this,"???????????????. :)",Toast.LENGTH_SHORT).show();
        finish();
    }
}

