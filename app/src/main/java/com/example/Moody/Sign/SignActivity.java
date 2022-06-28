package com.example.Moody.Sign;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignActivity extends AppCompatActivity {
    private static final String TAG = "SignActivity";
    private FirebaseDatabase database= FirebaseDatabase.getInstance();
    private Boolean check = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign);

        final EditText signEmail = (EditText)findViewById(R.id.sign_input_email);
        final Button nextBtn = (Button)findViewById(R.id.sign_nextBtn);
        final Button backBtn = (Button)findViewById(R.id.sign_backBtn);

        nextBtn.setVisibility(View.VISIBLE);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email  = signEmail.getText().toString();

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    Toast toast =Toast.makeText(SignActivity.this,"이메일 형식이 아닙니다.",Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                    toast.show();
                }else {
                    EmailCheck(email);
                }


            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignActivity.this, LoginActivity.class));
            }
        });

    }

    //키보드 내리기
    public boolean onTouchEvent(MotionEvent event) {
        EditText email = (EditText)findViewById(R.id.sign_input_email);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(email.getWindowToken(), 0);
        return true;
    }

    public void EmailCheck(final String inEmail){
        database.getReference("userInfo").orderByChild("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um;
                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    um=dataSnapshot1.getValue(UserModel.class);
                    if(um.getEmail().equals(inEmail)){
                        check=false;
                        break;
                    }else{
                        check=true;
                    }
                }
                if(check==false) {
                    Toast toast = Toast.makeText(SignActivity.this, "중복된 이메일입니다.", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL, Gravity.CENTER, 0);
                    toast.show();
                }else{
                    Intent intent = new Intent(getApplicationContext(), SignPwActivity.class);
                    intent.putExtra("semail", inEmail);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



}