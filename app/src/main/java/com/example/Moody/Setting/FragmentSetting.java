package com.example.Moody.Setting;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Moody.Activity.LoginActivity;
import com.example.Moody.Friend.FragmentFriend;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class FragmentSetting extends Fragment {
    private static final String TAG = "FragmentSetting";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private String name;
    private String email;

    private String imageUrl;
    int GET_GALLERY_IMAGE = 101;

    public static FragmentSetting newInstance(){
        return new FragmentSetting();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting,container,false);

        //view 선언
        TextView myName = (TextView)view.findViewById(R.id.user_id);
        TextView myEmail = (TextView)view.findViewById(R.id.user_email);
        ImageView myImage = (ImageView) view.findViewById(R.id.profile_image);
        //DB 유저 정보 불러오기
        UserInfo(myName,myEmail,myImage);

        //버튼 선언
        Button profileBtn = (Button)view.findViewById(R.id.profile_btn);
        Button logoutBtn = (Button)view.findViewById(R.id.logout_btn);
        Button change_password_btn = (Button)view.findViewById(R.id.change_password_btn);
        Button app_info_btn = (Button)view.findViewById(R.id.app_info_btn);
        Button qrCode = (Button)view.findViewById(R.id.QR_code);

        //프로필 변경
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentFriend.state = 1;
                startActivity(new Intent(getActivity(),ProfilePageActivity.class));
            }
        });

        //QR코드
        qrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(email)){
                    return;
                }
                Intent intent =  new Intent(getActivity(), QR_code.class);
                intent.putExtra("email",email);
                startActivity(intent);
            }
        });


        //로그아웃
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userUid = currentUser.getUid();

                final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/connection");
                final DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("/userInfo/"+userUid+"/lastOnline");

                myConnectionsRef.setValue(false);
                lastOnlineRef.setValue(ServerValue.TIMESTAMP);

                mAuth.signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        //비밀번호 변경
        change_password_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(getActivity(), change_password.class));
                mAuth.sendPasswordResetEmail(currentUser.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast toast = Toast.makeText(getContext(),"비밀번호 변경 메일을 전송했습니다",Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                        toast.show();
                        mAuth.signOut();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                        getActivity().finish();
                        Toast toast2 = Toast.makeText(getContext(),"다시 로그인 해주세요.",Toast.LENGTH_SHORT);
                        toast2.setGravity(Gravity.CENTER_HORIZONTAL,Gravity.CENTER,0);
                        toast2.show();
                    }
                });

            }
        });

        //앱정보
        app_info_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), app_info.class));
            }
        });

        return view;
    }



    public void UserInfo(final TextView myName, final TextView myEmail, final ImageView myImage){
        database.getReference("userInfo").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                myName.setText(user.getName());
                myEmail.setText(user.getEmail());
                email = user.getEmail();

                if(!user.getProfile().equals("")) {
                    if (user.getRange().equals("all")) {
                        if (!user.getProfile().equals(""))
                            Glide.with(getContext()).load(user.getProfile()).apply(new RequestOptions().circleCrop()).into(myImage);
                    } else if (user.getRange().equals("friend")) {
                        myImage.setBackgroundResource(R.drawable.yj_profile_border);
                        if (!user.getProfile().equals(""))
                            Glide.with(getContext()).load(user.getProfile()).apply(new RequestOptions().circleCrop()).into(myImage);

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }



}