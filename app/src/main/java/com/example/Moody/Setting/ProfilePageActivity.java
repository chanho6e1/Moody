package com.example.Moody.Setting;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.Moody.Activity.MainActivity;
import com.example.Moody.Friend.FragmentFriend;
import com.example.Moody.Model.UserModel;
import com.example.Moody.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfilePageActivity extends AppCompatActivity {
    private static final String TAG = "ProfilePageActivity";

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser = mAuth.getCurrentUser();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();

    private int GET_GALLERY_IMAGE = 103;
    private String imageUrl;

    Spinner s;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit_page);

        //????????????, ????????????
        ImageView photo = (ImageView) findViewById(R.id.profile_my_image);
        final EditText name = (EditText) findViewById(R.id.my_name);
        final EditText birth = (EditText) findViewById(R.id.my_birth);

        UserInfo(photo,name,birth); //?????? ?????? ???????????? ????????????

        //??? ??????
        ImageView backBtn = (ImageView) findViewById(R.id.profileEdit_backBtn);
        Button saveBtn = (Button) findViewById(R.id.save_btn);
        Button gallery = (Button) findViewById(R.id.profile_gallery_btn);

        //???????????? ??????
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfilePageActivity.this, MainActivity.class);
                if(FragmentFriend.state == 0)
                    intent.putExtra("fragment", "friend");
                if(FragmentFriend.state == 1)
                    intent.putExtra("fragment", "setting");
                startActivity(intent);
                finish();
            }
        });

        //????????? ??????
        s = (Spinner)findViewById(R.id.rangeSpinner);
        ArrayAdapter SpinAdapter = ArrayAdapter.createFromResource(ProfilePageActivity.this, R.array.profileMode, R.layout.range_spinner_design);
        s.setAdapter(SpinAdapter);
        final Object[] range = new Object[1];

        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                range[0] = parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        //???????????? ??????
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String changeName = name.getText().toString();
                String changeBirth = birth.getText().toString();

                //????????? ?????? DB??? ??????
                Map<String, Object> taskMap = new HashMap<String, Object>();
                if(range[0].equals("?????? ??????")) {
                    taskMap.put("range", "all");
                    database.getReference("userInfo").child(currentUser.getUid()).updateChildren(taskMap);
                }
                else if(range[0].equals("?????? ????????? ??????")) {
                    taskMap.put("range", "friend");
                    database.getReference("userInfo").child(currentUser.getUid()).updateChildren(taskMap);
                }
                //????????? ????????? ??????
                if(changeName != null && changeBirth == null){
                    HashMap<String, Object> info = new HashMap<String, Object>();
                    info.put("name", changeName);
                    database.getReference("userInfo").child(currentUser.getUid()).updateChildren(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(ProfilePageActivity.this, MainActivity.class);
                            if(FragmentFriend.state == 0)
                                intent.putExtra("fragment", "friend");
                            if(FragmentFriend.state == 1)
                                intent.putExtra("fragment", "setting");
                            startActivity(intent);
                            finish();
                        }
                    });
                    //??????????????? ????????? ??????
                }else if(changeName == null && changeBirth != null){
                    HashMap<String, Object> info = new HashMap<String, Object>();
                    info.put("birth", changeBirth);
                    database.getReference("userInfo").child(currentUser.getUid()).updateChildren(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(ProfilePageActivity.this, MainActivity.class);
                            if(FragmentFriend.state == 0)
                                intent.putExtra("fragment", "friend");
                            if(FragmentFriend.state == 1)
                                intent.putExtra("fragment", "setting");
                            startActivity(intent);
                            finish();
                        }
                    });
                    //????????? ?????? ????????????
                }else if(changeName == null && changeBirth == null){
                    Intent intent = new Intent(ProfilePageActivity.this, MainActivity.class);
                    if(FragmentFriend.state == 0)
                        intent.putExtra("fragment", "friend");
                    if(FragmentFriend.state == 1)
                        intent.putExtra("fragment", "setting");
                    startActivity(intent);
                    finish();
                }
                //????????? ????????? ??????
                else{
                    HashMap<String, Object> info = new HashMap<String, Object>();
                    info.put("name", changeName);
                    info.put("birth", changeBirth);
                    database.getReference("userInfo").child(currentUser.getUid()).updateChildren(info).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(ProfilePageActivity.this, MainActivity.class);
                            if(FragmentFriend.state == 0)
                                intent.putExtra("fragment", "friend");
                            if(FragmentFriend.state == 1)
                                intent.putExtra("fragment", "setting");
                            startActivity(intent);
                            finish();
                        }
                    });
                }



            }
        });

        //????????? ?????? ?????????
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });

    }

    //????????? ??????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView photo = (ImageView) findViewById(R.id.profile_my_image);
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData(); //????????? ?????? ??????
            String imagePath = "Users/" + currentUser.getUid(); //???????????? ?????? ??? ??????
            UploadFiles(selectedImageUri, imagePath); //?????? ?????????

            Glide.with(ProfilePageActivity.this).load(selectedImageUri).apply(new RequestOptions().circleCrop()).into(photo);
        }

    }

    //????????????????????? ?????? ?????????
    public void UploadFiles(Uri uri, final String path) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        final StorageReference storageRef = storage.getReference();

        final StorageReference riversRef = storageRef.child(path);
        UploadTask uploadTask = riversRef.putFile(uri);
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return riversRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();
                    //DB??? ??????
                    DatabaseReference ref = database.getReference("userInfo").child(currentUser.getUid());
                    HashMap<String, Object> photo = new HashMap<String, Object>();
                    photo.put("profile", imageUrl);
                    ref.updateChildren(photo); //DB??? ??????
                }
            }
        });
    }

    //????????? ?????? ????????????
    public void UserInfo(final ImageView profile, final EditText name, final EditText birth){
        database.getReference("userInfo").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel um = dataSnapshot.getValue(UserModel.class);

                if(!um.getProfile().equals("")) {
                    if (um.getRange().equals("all")) {
                        if (!um.getProfile().equals(""))
                            Glide.with(profile.getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).into(profile);
                    } else if (um.getRange().equals("friend")) {
                        profile.setBackgroundResource(R.drawable.yj_profile_border);
                        if (!um.getProfile().equals(""))
                            Glide.with(profile.getContext()).load(um.getProfile()).apply(new RequestOptions().circleCrop()).into(profile);

                    }
                }

                name.setText(um.getName());
                birth.setText(um.getBirth());

                String item = um.getRange();
                if(item!=null) {
                    if (item.equals("all"))
                        s.setSelection(0);
                    if (item.equals("friend"))
                        s.setSelection(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    //?????? ?????? ?????? ?????????
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
